package com.careersail.ai;

import com.careersail.entity.ResumeRecord;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeAiService {

    private final ChatModel chatModel;
    private final RagService ragService;
    private final SysUserMapper sysUserMapper;

    private static final String SYSTEM_PROMPT = """
        You are a professional resume coach with 10 years of HR recruitment experience at top tech companies.
        Your task is to optimize student resumes to be more professional, impactful, and ATS-friendly.

        Rules:
        1. Use STAR method (Situation-Task-Action-Result) for project descriptions
        2. Quantify achievements with numbers (e.g. improved performance by 30%, served 500+ users)
        3. Use strong action verbs (designed, implemented, optimized, led, delivered)
        4. Keep it concise - remove filler words and redundant phrases
        5. Match keywords to the target job description
        6. Format with clear section headers: Education, Skills, Projects, Experience
        7. Return the COMPLETE optimized resume, not just suggestions
        8. Keep the original truthful information, don't fabricate
        9. Add a brief summary section at the top (2-3 lines)
        10. Output in Chinese, with the resume content clearly formatted
        """;

    public String optimize(ResumeRecord record, Long userId) {
        SysUser user = sysUserMapper.selectById(userId);

        // Get relevant career data for keyword matching
        String careerContext = "";
        if (record.getTargetPosition() != null && !record.getTargetPosition().isBlank()) {
            try {
                List<CareerVO> matches = ragService.searchSimilar(record.getTargetPosition(), 3);
                if (!matches.isEmpty()) {
                    careerContext = "Target position requirements:\n" + matches.stream()
                            .map(c -> "- " + c.getPositionName() + ": " + (c.getSkillsRequired() != null ? c.getSkillsRequired() : ""))
                            .collect(Collectors.joining("\n"));
                }
            } catch (Exception e) { /* ignore */ }
        }

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("Please optimize the following student resume.\n\n");
        if (user != null) {
            userPrompt.append("Student info: ");
            if (user.getMajor() != null) userPrompt.append("Major=").append(user.getMajor()).append(", ");
            if (user.getGrade() != null) userPrompt.append("Grade=").append(user.getGrade()).append(", ");
            userPrompt.append("\n");
        }
        if (record.getTargetPosition() != null && !record.getTargetPosition().isBlank()) {
            userPrompt.append("Target position: ").append(record.getTargetPosition()).append("\n");
        }
        if (!careerContext.isEmpty()) {
            userPrompt.append("\n").append(careerContext).append("\n");
        }
        userPrompt.append("\n--- Original Resume ---\n");
        userPrompt.append(record.getOriginalText());
        userPrompt.append("\n--- End ---\n\n");
        userPrompt.append("Return the complete optimized resume with proper formatting and section headers.");

        try {
            String response = chatModel.call(
                    new Prompt(List.of(
                            new SystemMessage(SYSTEM_PROMPT),
                            new UserMessage(userPrompt.toString())
                    ))
            ).getResult().getOutput().getText();
            log.info("Resume optimized: userId={}, len={}->{}", userId, record.getOriginalText().length(), response.length());
            return response;
        } catch (Exception e) {
            log.error("Resume optimization failed", e);
            return "[AI service temporarily unavailable. Please try again later.]";
        }
    }
}
