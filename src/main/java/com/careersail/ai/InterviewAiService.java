package com.careersail.ai;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.careersail.entity.*;
import com.careersail.mapper.AssessmentResultMapper;
import com.careersail.mapper.SysUserMapper;
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
public class InterviewAiService {

    private final ChatModel chatModel;
    private final SysUserMapper sysUserMapper;
    private final AssessmentResultMapper assessmentResultMapper;

    public record InterviewStep(String question, String feedback, int score, boolean isComplete) {}

    private static final String INTERVIEWER_PROMPT = """
        You are a professional technical interviewer at a top tech company. Conduct a mock interview.
        You must respond ONLY in JSON format: {"question":"...","feedback":"...","score":0,"isComplete":false}

        Rules:
        - Start with an opening question (isComplete=false)
        - After the candidate answers, give brief feedback and a score (0-100) in the SAME JSON
        - Then ask the next question (isComplete=false) until the round limit
        - After 5 rounds, set isComplete=true and provide a summary
        - Adapt questions based on the candidate's previous answers
        - Score strictly: 85+ excellent, 70-84 good, 60-69 pass, <60 needs improvement
        - Feedback should be specific: what was good, what could be improved
        - Questions should be realistic interview questions for the target role
        - Keep the interview in Chinese
        - Total output per turn: 300 Chinese characters max
        """;

    public InterviewStep startOrContinue(InterviewSession session, List<InterviewQA> history, String userAnswer) {
        String userPrompt = buildPrompt(session, history, userAnswer);
        try {
            String response = chatModel.call(new Prompt(List.of(
                    new SystemMessage(INTERVIEWER_PROMPT),
                    new UserMessage(userPrompt)
            ))).getResult().getOutput().getText();

            // Extract JSON from response
            String json = response;
            int start = response.indexOf("{");
            int end = response.lastIndexOf("}");
            if (start >= 0 && end > start) json = response.substring(start, end + 1);

            JSONObject obj = JSONUtil.parseObj(json);
            return new InterviewStep(
                    obj.getStr("question", ""),
                    obj.getStr("feedback", ""),
                    obj.getInt("score", 0),
                    obj.getBool("isComplete", false)
            );
        } catch (Exception e) {
            log.error("Interview AI failed: {}", e.getMessage());
            return new InterviewStep("请稍后重试", "AI服务暂时不可用", 0, true);
        }
    }

    public String generateSummary(InterviewSession session, List<InterviewQA> history) {
        StringBuilder sb = new StringBuilder();
        sb.append("Generate a mock interview summary report in Chinese.\n");
        sb.append("Position: ").append(session.getTargetPosition() != null ? session.getTargetPosition() : "General").append("\n");
        sb.append("Rounds: ").append(history.size()).append("\n");
        sb.append("Questions and scores:\n");
        for (InterviewQA qa : history) {
            sb.append("- Q").append(qa.getRoundNum()).append(": ").append(qa.getQuestion().substring(0, Math.min(50, qa.getQuestion().length())))
                    .append(" ... Score: ").append(qa.getScore()).append("\n");
        }
        sb.append("Provide: overall score, strengths, weaknesses, and 3 actionable tips. 300 words max, Chinese.");
        try {
            return chatModel.call(new Prompt(new UserMessage(sb.toString()))).getResult().getOutput().getText();
        } catch (Exception e) {
            return "面试总结暂时无法生成。";
        }
    }

    private String buildPrompt(InterviewSession session, List<InterviewQA> history, String userAnswer) {
        StringBuilder sb = new StringBuilder();
        sb.append("Interview type: ").append(session.getInterviewType()).append("\n");
        sb.append("Target position: ").append(session.getTargetPosition() != null ? session.getTargetPosition() : "General").append("\n");
        sb.append("Current round: ").append(history.size() + 1).append(" of 5\n\n");

        if (!history.isEmpty()) {
            sb.append("Previous rounds:\n");
            for (InterviewQA qa : history) {
                sb.append("Q").append(qa.getRoundNum()).append(": ").append(qa.getQuestion()).append("\n");
                sb.append("A").append(qa.getRoundNum()).append(": ").append(qa.getUserAnswer()).append("\n");
                sb.append("Score: ").append(qa.getScore()).append("\n\n");
            }
        }

        if (history.isEmpty()) {
            sb.append("The candidate is ready. Ask the first interview question.");
        } else if (history.size() >= 4) {
            sb.append("The candidate just answered: ").append(userAnswer).append("\n");
            sb.append("This is the last round. After feedback, set isComplete=true.");
        } else {
            sb.append("The candidate just answered: ").append(userAnswer).append("\n");
            sb.append("Give feedback and the next question.");
        }

        return sb.toString();
    }
}
