package com.careersail.ai;

import com.careersail.entity.AiTrainingRecord;
import com.careersail.entity.AssessmentResult;
import com.careersail.entity.CareerInfo;
import com.careersail.entity.SysUser;
import com.careersail.mapper.*;
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
public class SkillGapService {

    private final ChatModel chatModel;
    private final CareerInfoMapper careerInfoMapper;
    private final AssessmentResultMapper assessmentResultMapper;
    private final AiTrainingRecordMapper trainingRecordMapper;
    private final SysUserMapper sysUserMapper;

    public String analyze(Long userId, Long careerId) {
        SysUser user = sysUserMapper.selectById(userId);
        CareerInfo career = careerInfoMapper.selectById(careerId);
        if (career == null) return "Career not found.";

        List<AssessmentResult> assessments = assessmentResultMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AssessmentResult>()
                        .eq(AssessmentResult::getUserId, userId)
                        .orderByDesc(AssessmentResult::getCreateTime)
                        .last("LIMIT 3"));
        List<AiTrainingRecord> trainings = trainingRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiTrainingRecord>()
                        .eq(AiTrainingRecord::getUserId, userId)
                        .orderByDesc(AiTrainingRecord::getSubmitTime)
                        .last("LIMIT 10"));

        String systemPrompt = """
            You are a career skills analyst. Analyze the gap between a student's current profile and a target job.
            Output in Chinese, structured with:
            1. Current strengths (what they already have)
            2. Key gaps (what they need to develop)
            3. Learning resources (specific courses, projects, certifications)
            Keep it concise, actionable, and encouraging. 500 words max.
            """;

        StringBuilder userPrompt = new StringBuilder();
        userPrompt.append("Target Job:\n");
        userPrompt.append("- Position: ").append(career.getPositionName()).append("\n");
        userPrompt.append("- Required Skills: ").append(career.getSkillsRequired() != null ? career.getSkillsRequired() : "N/A").append("\n");
        userPrompt.append("- Career Path: ").append(career.getCareerPath() != null ? career.getCareerPath() : "N/A").append("\n");
        userPrompt.append("\nStudent Profile:\n");
        userPrompt.append("- Major: ").append(user != null && user.getMajor() != null ? user.getMajor() : "Unknown").append("\n");
        userPrompt.append("- Grade: ").append(user != null && user.getGrade() != null ? user.getGrade() : "Unknown").append("\n");

        if (!assessments.isEmpty()) {
            userPrompt.append("- Assessment Results: ");
            userPrompt.append(assessments.stream()
                    .filter(a -> a.getResultType() != null)
                    .map(a -> a.getResultType())
                    .collect(Collectors.joining(", "))).append("\n");
        }

        if (!trainings.isEmpty()) {
            userPrompt.append("- Completed Trainings: ").append(trainings.size()).append(" tasks, avg score: ");
            double avg = trainings.stream().filter(t -> t.getAiScore() != null).mapToInt(AiTrainingRecord::getAiScore).average().orElse(0);
            userPrompt.append(String.format("%.0f", avg)).append("\n");
        }

        userPrompt.append("\nAnalyze the skill gap and provide specific, actionable advice in Chinese.");

        try {
            return chatModel.call(new Prompt(List.of(
                    new SystemMessage(systemPrompt),
                    new UserMessage(userPrompt.toString())
            ))).getResult().getOutput().getText();
        } catch (Exception e) {
            log.error("Skill gap analysis failed", e);
            return "AI analysis temporarily unavailable. Please try again later.";
        }
    }
}
