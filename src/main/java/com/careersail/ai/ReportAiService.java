package com.careersail.ai;

import com.careersail.entity.AssessmentType;
import com.careersail.entity.SysUser;
import com.careersail.mapper.SysUserMapper;
import com.careersail.vo.CareerVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportAiService {

    private final ChatModel chatModel;
    private final PromptTemplateService promptTemplateService;
    private final RagService ragService;
    private final SysUserMapper sysUserMapper;

    public String generateReport(AssessmentType type, String resultType,
                                  Map<String, Integer> scores, Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        String ragContext = buildRagContext(resultType, user);
        String userPrompt = buildUserPrompt(type, resultType, scores, user, ragContext);
        String systemPrompt = promptTemplateService.getReportPrompt();

        try {
            String response = chatModel.call(
                    new Prompt(List.of(
                            new SystemMessage(systemPrompt),
                            new UserMessage(userPrompt)
                    ))
            ).getResult().getOutput().getText();

            log.info("AI report: userId={}, type={}, result={}, len={}",
                    userId, type.getCode(), resultType, response.length());
            return response;
        } catch (Exception e) {
            log.error("AI report failed, using smart fallback", e);
            return generateSmartFallback(type, resultType, scores, user, ragContext);
        }
    }

    private String buildUserPrompt(AssessmentType type, String resultType,
                                    Map<String, Integer> scores, SysUser user, String ragContext) {
        StringBuilder sb = new StringBuilder();
        sb.append("Please generate a detailed career assessment report for this student.\n\n");
        sb.append("[Student Info]\n");
        sb.append("- Name: ").append(user != null && user.getRealName() != null ? user.getRealName() : "Student").append("\n");
        sb.append("- Major: ").append(user != null && user.getMajor() != null ? user.getMajor() : "Unknown").append("\n");
        if (user != null && user.getGrade() != null) {
            sb.append("- Grade: ").append(user.getGrade()).append("\n");
        }
        sb.append("\n[Assessment Info]\n");
        sb.append("- Type: ").append(type.getName()).append("\n");
        sb.append("- Result Code: ").append(resultType).append("\n");
        sb.append("- Dimension Scores:\n");
        for (Map.Entry<String, Integer> e : scores.entrySet()) {
            String bar = "#".repeat(Math.max(1, e.getValue() / 2));
            sb.append("  ").append(e.getKey()).append(": ").append(bar).append(" ").append(e.getValue()).append("\n");
        }
        sb.append("\n");
        if (!ragContext.isEmpty()) {
            sb.append("[Matching Career Data]\n").append(ragContext).append("\n");
        }
        sb.append("Generate the report with these sections: 1. Personality Analysis 2. Career Interest 3. Recommended Jobs 4. Development Advice 5. Study Roadmap. Use 800-1200 words, Markdown format, warm and professional tone.");
        return sb.toString();
    }

    private String buildRagContext(String resultType, SysUser user) {
        try {
            String query = resultType;
            if (user != null && user.getMajor() != null) {
                query = user.getMajor() + " " + resultType;
            }
            List<CareerVO> results = ragService.searchSimilar(query, 5);
            if (results.isEmpty()) return "";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < results.size(); i++) {
                CareerVO c = results.get(i);
                sb.append(i + 1).append(". ").append(c.getPositionName())
                        .append(" (").append(c.getIndustry() != null ? c.getIndustry() : "General").append(")\n");
                if (c.getSkillsRequired() != null) {
                    sb.append("   Skills: ").append(c.getSkillsRequired()).append("\n");
                }
                if (c.getCareerPath() != null) {
                    sb.append("   Career Path: ").append(c.getCareerPath()).append("\n");
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    // ====== Smart Fallback Report ======

    private String generateSmartFallback(AssessmentType type, String resultType,
                                          Map<String, Integer> scores, SysUser user, String ragContext) {
        StringBuilder r = new StringBuilder();
        r.append("# ").append(type.getName()).append(" Assessment Report\n\n");
        r.append("> Generated: ").append(java.time.LocalDateTime.now().toString().replace("T", " ")).append("\n\n---\n\n");

        // Student info table
        r.append("## Student Profile\n\n| Field | Value |\n|-------|-------|\n");
        r.append("| Name | ").append(user != null && user.getRealName() != null ? user.getRealName() : "Student").append(" |\n");
        r.append("| Major | ").append(user != null && user.getMajor() != null ? user.getMajor() : "N/A").append(" |\n");
        if (user != null && user.getGrade() != null) {
            r.append("| Grade | ").append(user.getGrade()).append(" |\n");
        }
        r.append("| Assessment | ").append(type.getName()).append(" |\n");
        r.append("| Result | **").append(resultType).append("** |\n\n---\n\n");

        // MBTI interpretation
        if ("MBTI".equals(type.getCode())) {
            r.append("## Personality Analysis\n\n");
            r.append(getMbtiInterpretation(resultType));
            r.append("\n---\n\n");
        }

        // Holland interpretation
        if ("HOLLAND".equals(type.getCode())) {
            r.append("## Career Interest Analysis\n\n");
            r.append(getHollandInterpretation(resultType));
            r.append("\n---\n\n");
        }

        // Scores
        r.append("## Dimension Scores\n\n");
        for (Map.Entry<String, Integer> e : scores.entrySet()) {
            String bar = "#".repeat(Math.max(1, e.getValue()));
            r.append("- **").append(e.getKey()).append("**: ").append(bar).append(" ").append(e.getValue()).append("\n");
        }
        r.append("\n---\n\n");

        // Career matches from RAG
        if (!ragContext.isEmpty()) {
            r.append("## Recommended Careers\n\n").append(ragContext).append("\n---\n\n");
        }

        // Study roadmap
        r.append("## Development Advice & Study Roadmap\n\n");
        r.append("### University Timeline\n\n");
        r.append("| Year | Focus |\n|------|-------|\n");
        r.append("| Freshman | Explore interests, build foundation, join clubs |\n");
        r.append("| Sophomore | Choose career path, start projects, first internship |\n");
        r.append("| Junior | Deep professional learning, core internship, prepare for job/graduate school |\n");
        r.append("| Senior | Complete thesis, finalize direction, transition to career |\n\n");
        r.append("### Key Actions\n\n");
        r.append("1. Build a strong portfolio with 2-3 substantial projects\n");
        r.append("2. Complete at least 1 internship before graduation\n");
        r.append("3. Develop both technical skills and soft skills (communication, teamwork)\n");
        r.append("4. Review and adjust your career plan every semester\n\n");
        r.append("---\n\n*Report generated by CareerSail AI. For a more detailed analysis, click Regenerate.*");

        return r.toString();
    }

    // ====== MBTI Interpretations (simplified, without special characters) ======

    private String getMbtiInterpretation(String code) {
        if (code == null) return "Personality type not determined.";
        return switch (code) {
            case "INTJ" -> "**INTJ - The Architect**: Strategic thinker, independent, decisive, high standards. You excel at systematic thinking and long-term planning. *Strengths:* Strategic vision, independent problem-solving, execution excellence. *Growth areas:* Develop emotional awareness, accept imperfection, stay open-minded. *Jobs:* Tech architect, data analyst, researcher, strategy consultant.";
            case "INTP" -> "**INTP - The Logician**: Innovative thinker, curious, analytical, theory-driven. You enjoy solving complex problems and pursuing knowledge. *Strengths:* Logical analysis, creative thinking, independent learning. *Growth areas:* Improve execution, strengthen communication, manage time. *Jobs:* Algorithm engineer, researcher, software architect, professor.";
            case "ENTJ" -> "**ENTJ - The Commander**: Natural leader, decisive, confident, goal-oriented. You have strong leadership and execution skills. *Strengths:* Leadership, strategic planning, decisive action, efficiency. *Growth areas:* Develop patience and empathy, allow space for others, avoid over-control. *Jobs:* CEO/entrepreneur, product director, project manager, management consultant.";
            case "ENTP" -> "**ENTP - The Debater**: Quick thinker, articulate, creative, challenge-loving. You enjoy brainstorming and exploring new possibilities. *Strengths:* Innovation, eloquence, adaptability, entrepreneurial spirit. *Growth areas:* Focus on execution, attend to details, finish what you start. *Jobs:* Entrepreneur, product manager, marketing strategist, investment analyst.";
            case "INFJ" -> "**INFJ - The Advocate**: Insightful idealist, deep thinker, empathetic, purpose-driven. You focus on deeper values and human welfare. *Strengths:* Deep insight, genuine helpfulness, creativity, strong values. *Growth areas:* Protect your energy, accept imperfect reality, express your needs. *Jobs:* Counselor, educator, HR, social organization manager.";
            case "INFP" -> "**INFP - The Mediator**: Idealistic healer, empathetic, creative, values-driven. You seek authentic self-expression and meaningful work. *Strengths:* Creativity, sincerity, adaptability, deep thinking. *Growth areas:* Boost action-taking, learn pragmatism, accept criticism. *Jobs:* UI/UX designer, content creator, counselor, education tech.";
            case "ENFJ" -> "**ENFJ - The Protagonist**: Charismatic guide, warm, communicative, growth-focused. You naturally inspire and motivate others. *Strengths:* Communication, leadership charisma, empathy, organization. *Growth areas:* Learn to say no, accept necessary conflict, attend to your own needs. *Jobs:* Trainer, HR manager, PR, marketing, education management.";
            case "ENFP" -> "**ENFP - The Campaigner**: Enthusiastic explorer, warm, creative, social. You are passionate about life and discovering possibilities. *Strengths:* Social skills, creativity, enthusiasm, adaptability. *Growth areas:* Improve focus, pursue depth over breadth, manage time. *Jobs:* Marketing planner, brand manager, creative director, PR, social media.";
            case "ISTJ" -> "**ISTJ - The Logistician**: Reliable executor, responsible, organized, detail-oriented. You are the most dependable pillar of any team. *Strengths:* Reliability, organization, practicality, integrity. *Growth areas:* Learn flexibility, embrace change, cultivate innovation. *Jobs:* Accountant, auditor, legal, quality management, civil servant.";
            case "ISFJ" -> "**ISFJ - The Defender**: Warm caregiver, attentive, responsible, helpful. You care deeply for others and quietly protect those around you. *Strengths:* Carefulness, strong responsibility, loyalty, detail observation. *Growth areas:* Express your own needs, adapt to change, avoid perfectionism. *Jobs:* Healthcare worker, teacher, admin, customer service, librarian.";
            case "ESTJ" -> "**ESTJ - The Executive**: Efficient manager, practical, organized, responsible. You excel at organizing and executing tasks. *Strengths:* Execution ability, organization, decision-making, reliability. *Growth areas:* Cultivate flexibility, attend to others feelings, accept different views. *Jobs:* Project manager, operations manager, finance manager, administrator.";
            case "ESFJ" -> "**ESFJ - The Consul**: Warm coordinator, sociable, cooperative, harmony-focused. You create positive team atmospheres. *Strengths:* Social skills, empathy, organization, service mindset. *Growth areas:* Accept constructive criticism, make independent decisions, focus on self-growth. *Jobs:* HR, customer relations, PR, hotel management, social work.";
            case "ISTP" -> "**ISTP - The Virtuoso**: Cool pragmatist, calm, hands-on, curious about how things work. You are a practical problem solver. *Strengths:* Hands-on skills, calm analysis, flexibility, independence. *Growth areas:* Improve long-term planning, strengthen communication, value teamwork. *Jobs:* Mechanical engineer, cybersecurity, pilot, surgeon.";
            case "ISFP" -> "**ISFP - The Adventurer**: Gentle artist, aesthetic, low-key, present-focused. You have unique aesthetic sensibilities. *Strengths:* Aesthetic ability, adaptability, sincerity, gentle stability. *Growth areas:* Improve long-term planning, learn to express needs, showcase your talents. *Jobs:* Designer, photographer, florist, rehabilitation therapist.";
            case "ESTP" -> "**ESTP - The Entrepreneur**: Energetic doer, bold, social, quick-thinking. You love excitement and challenges. *Strengths:* Execution, adaptability, social skills, risk-taking. *Growth areas:* Cultivate patience, strengthen long-term planning, assess risks. *Jobs:* Sales elite, entrepreneur, sports coach, emergency responder.";
            case "ESFP" -> "**ESFP - The Entertainer**: Vibrant performer, outgoing, fun-loving, infectious. You are the life of any group. *Strengths:* Social charm, infectious energy, optimism, hands-on. *Growth areas:* Improve focus, develop long-term planning, control impulse spending. *Jobs:* Performer/host, event planner, tour guide, fitness coach, sales.";
            default -> "Your personality type is **" + code + "**. Each type has unique strengths and growth areas. Understanding yourself is the first step in career planning.";
        };
    }

    private String getHollandInterpretation(String code) {
        if (code == null || code.isEmpty()) return "Career interest type not determined.";
        StringBuilder sb = new StringBuilder();
        sb.append("Your Holland Code is **").append(code).append("**.\n\n");
        for (char c : code.toCharArray()) {
            sb.append("- ").append(switch (c) {
                case 'R' -> "**Realistic** - Prefers hands-on work, mechanics, and outdoor activities.";
                case 'I' -> "**Investigative** - Enjoys thinking, research, and solving complex problems.";
                case 'A' -> "**Artistic** - Likes creating, expressing, and designing.";
                case 'S' -> "**Social** - Enjoys helping, teaching, and serving others.";
                case 'E' -> "**Enterprising** - Likes leading, persuading, and managing.";
                case 'C' -> "**Conventional** - Prefers structure, organization, and data processing.";
                default -> "Unknown type: " + c;
            }).append("\n");
        }
        sb.append("\nYour code combination suggests you would excel in roles that combine these strengths.");
        return sb.toString();
    }
}
