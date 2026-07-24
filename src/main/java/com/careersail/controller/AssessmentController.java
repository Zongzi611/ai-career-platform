package com.careersail.controller;

import com.careersail.common.Result;
import com.careersail.dto.AssessmentSubmitDTO;
import com.careersail.entity.AssessmentQuestion;
import com.careersail.entity.AssessmentType;
import com.careersail.entity.SysUser;
import com.careersail.mapper.SysUserMapper;
import com.careersail.service.AssessmentService;
import com.careersail.vo.AssessmentResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assessment")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final SysUserMapper sysUserMapper;

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return sysUserMapper.selectByUsername(auth.getName()).orElseThrow().getId();
    }

    @GetMapping("/types")
    public Result<List<AssessmentType>> listTypes() {
        return Result.ok(assessmentService.listTypes());
    }

    @GetMapping("/{typeId}/questions")
    public Result<List<AssessmentQuestion>> getQuestions(@PathVariable Long typeId) {
        return Result.ok(assessmentService.getQuestionsWithOptions(typeId));
    }

    @PostMapping("/start")
    public Result<?> start(@RequestParam Long typeId) {
        Long recordId = assessmentService.startAssessment(typeId, getCurrentUserId());
        return Result.ok(java.util.Map.of("recordId", recordId));
    }

    @PostMapping("/submit")
    public Result<?> submit(@RequestBody AssessmentSubmitDTO dto) {
        assessmentService.submitAnswer(dto, getCurrentUserId());
        return Result.ok("保存成功");
    }

    @PostMapping("/complete/{recordId}")
    public Result<AssessmentResultVO> complete(@PathVariable Long recordId) {
        return Result.ok(assessmentService.completeAssessment(recordId, getCurrentUserId()));
    }

    @GetMapping("/result/{resultId}")
    public Result<AssessmentResultVO> getResult(@PathVariable Long resultId) {
        return Result.ok(assessmentService.getResult(resultId));
    }

    @GetMapping("/my-results")
    public Result<List<AssessmentResultVO>> myResults() {
        return Result.ok(assessmentService.myResults(getCurrentUserId()));
    }

    @PostMapping("/generate-report/{resultId}")
    public Result<AssessmentResultVO> generateReport(@PathVariable Long resultId) {
        return Result.ok(assessmentService.generateAiReport(resultId, getCurrentUserId()));
    }
}
