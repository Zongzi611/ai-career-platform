package com.careersail.ai;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.careersail.entity.AiTrainingTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoringAiService {

    private final ChatModel chatModel;
    private final PromptTemplateService promptTemplateService;

    public record ScoreResult(int score, String feedback) {}

    public ScoreResult scoreAnswer(AiTrainingTask task, String userAnswer) {
        String systemPrompt = promptTemplateService.getScoringPrompt();

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("请对以下实训任务的答案进行评分：\n\n");
        userPrompt.append("【任务标题】").append(task.getTitle()).append("\n");
        userPrompt.append("【任务描述】").append(task.getDescription()).append("\n");
        if (task.getReferenceAnswer() != null) {
            userPrompt.append("【参考答案】").append(task.getReferenceAnswer()).append("\n");
        }
        if (task.getScoringCriteria() != null) {
            userPrompt.append("【评分标准】").append(task.getScoringCriteria()).append("\n");
        }
        userPrompt.append("\n【学生答案】\n").append(userAnswer);
        userPrompt.append("\n\n请严格按照JSON格式返回评分结果：{\"score\": 数字, \"feedback\": \"评语\"}");

        try {
            String response = chatModel.call(
                    new Prompt(List.of(
                            new SystemMessage(systemPrompt),
                            new UserMessage(userPrompt.toString())
                    ))
            ).getResult().getOutput().getText();

            // Parse JSON response
            JSONObject json = JSONUtil.parseObj(response);
            int score = json.getInt("score", 0);
            String feedback = json.getStr("feedback", "评分完成");
            return new ScoreResult(score, feedback);
        } catch (Exception e) {
            log.error("AI scoring failed", e);
            return new ScoreResult(60, "AI评分服务暂时不可用，默认给予60分。请手动评审。");
        }
    }
}
