package com.careersail.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.careersail.ai.InterviewAiService;
import com.careersail.common.Result;
import com.careersail.entity.InterviewQA;
import com.careersail.entity.InterviewSession;
import com.careersail.mapper.InterviewQAMapper;
import com.careersail.mapper.InterviewSessionMapper;
import com.careersail.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewSessionMapper sessionMapper;
    private final InterviewQAMapper qaMapper;
    private final InterviewAiService interviewAiService;
    private final SysUserMapper sysUserMapper;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return sysUserMapper.selectByUsername(auth.getName()).orElseThrow().getId();
    }

    @PostMapping("/start")
    public Result<?> start(@RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        String type = body.getOrDefault("interviewType", "technical");
        String position = body.getOrDefault("targetPosition", "");

        InterviewSession session = new InterviewSession();
        session.setUserId(userId);
        session.setInterviewType(type);
        session.setTargetPosition(position);
        session.setTitle((position.isBlank() ? "通用" : position) + "模拟面试");
        session.setStatus("IN_PROGRESS");
        sessionMapper.insert(session);

        // AI asks first question
        var step = interviewAiService.startOrContinue(session, List.of(), "");

        InterviewQA qa = new InterviewQA();
        qa.setSessionId(session.getId());
        qa.setRoundNum(1);
        qa.setQuestion(step.question());
        qaMapper.insert(qa);

        return Result.ok(Map.of(
                "sessionId", session.getId(),
                "roundNum", 1,
                "question", step.question(),
                "totalRounds", 5
        ));
    }

    @PostMapping("/answer")
    public Result<?> answer(@RequestBody Map<String, Object> body) {
        Long sessionId = Long.valueOf(body.get("sessionId").toString());
        String userAnswer = body.get("userAnswer").toString();

        InterviewSession session = sessionMapper.selectById(sessionId);
        if (session == null || !"IN_PROGRESS".equals(session.getStatus()))
            return Result.fail(400, "面试已结束");

        List<InterviewQA> history = qaMapper.selectList(
                new LambdaQueryWrapper<InterviewQA>().eq(InterviewQA::getSessionId, sessionId).orderByAsc(InterviewQA::getRoundNum));

        // Save answer to latest question
        InterviewQA current = history.get(history.size() - 1);
        current.setUserAnswer(userAnswer);

        // AI evaluates and asks next
        var step = interviewAiService.startOrContinue(session, history, userAnswer);
        current.setAiFeedback(step.feedback());
        current.setScore(step.score());
        qaMapper.updateById(current);

        if (step.isComplete() || history.size() >= 5) {
            // Generate summary
            String summary = interviewAiService.generateSummary(session, history);
            int avgScore = (int) history.stream().filter(q -> q.getScore() != null).mapToInt(InterviewQA::getScore).average().orElse(0);

            session.setStatus("COMPLETED");
            session.setTotalScore(avgScore);
            session.setSummaryReport(summary);
            sessionMapper.updateById(session);

            return Result.ok(Map.of(
                    "isComplete", true,
                    "feedback", step.feedback(),
                    "score", step.score(),
                    "totalScore", avgScore,
                    "summary", summary
            ));
        }

        // Next question
        InterviewQA nextQa = new InterviewQA();
        nextQa.setSessionId(sessionId);
        nextQa.setRoundNum(history.size() + 1);
        nextQa.setQuestion(step.question());
        qaMapper.insert(nextQa);

        return Result.ok(Map.of(
                "isComplete", false,
                "roundNum", history.size() + 1,
                "question", step.question(),
                "feedback", step.feedback(),
                "score", step.score()
        ));
    }

    @GetMapping("/sessions")
    public Result<?> getSessions() {
        Long userId = getCurrentUserId();
        return Result.ok(sessionMapper.selectList(
                new LambdaQueryWrapper<InterviewSession>()
                        .eq(InterviewSession::getUserId, userId)
                        .orderByDesc(InterviewSession::getCreateTime)));
    }

    @GetMapping("/{sessionId}")
    public Result<?> getDetail(@PathVariable Long sessionId) {
        InterviewSession session = sessionMapper.selectById(sessionId);
        List<InterviewQA> qas = qaMapper.selectList(
                new LambdaQueryWrapper<InterviewQA>().eq(InterviewQA::getSessionId, sessionId).orderByAsc(InterviewQA::getRoundNum));
        return Result.ok(Map.of("session", session, "qas", qas));
    }

    @DeleteMapping("/{sessionId}")
    public Result<?> delete(@PathVariable Long sessionId) {
        qaMapper.delete(new LambdaQueryWrapper<InterviewQA>().eq(InterviewQA::getSessionId, sessionId));
        sessionMapper.deleteById(sessionId);
        return Result.ok("已删除");
    }
}
