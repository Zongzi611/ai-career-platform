package com.careersail.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.careersail.ai.ReportAiService;
import com.careersail.common.BusinessException;
import com.careersail.common.Constants;
import com.careersail.common.ErrorCode;
import com.careersail.dto.AssessmentSubmitDTO;
import com.careersail.entity.*;
import com.careersail.mapper.*;
import com.careersail.service.AssessmentService;
import com.careersail.vo.AssessmentResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl extends ServiceImpl<AssessmentRecordMapper, AssessmentRecord> implements AssessmentService {

    private final AssessmentTypeMapper assessmentTypeMapper;
    private final AssessmentQuestionMapper assessmentQuestionMapper;
    private final AssessmentOptionMapper assessmentOptionMapper;
    private final AssessmentRecordMapper assessmentRecordMapper;
    private final AssessmentAnswerMapper assessmentAnswerMapper;
    private final AssessmentResultMapper assessmentResultMapper;
    private final ReportAiService reportAiService;

    @Override
    public List<AssessmentType> listTypes() {
        return assessmentTypeMapper.selectList(
                new LambdaQueryWrapper<AssessmentType>().eq(AssessmentType::getStatus, Constants.STATUS_ENABLED));
    }

    @Override
    public List<AssessmentQuestion> getQuestionsWithOptions(Long typeId) {
        List<AssessmentQuestion> questions = assessmentQuestionMapper.selectByTypeId(typeId);
        for (AssessmentQuestion q : questions) {
            q.setOptions(assessmentOptionMapper.selectByQuestionId(q.getId()));
        }
        return questions;
    }

    @Override
    @Transactional
    public Long startAssessment(Long typeId, Long userId) {
        // Check if there's an in-progress record (use selectList + take first to avoid TooManyResults)
        List<AssessmentRecord> existingList = assessmentRecordMapper.selectList(
                new LambdaQueryWrapper<AssessmentRecord>()
                        .eq(AssessmentRecord::getUserId, userId)
                        .eq(AssessmentRecord::getTypeId, typeId)
                        .eq(AssessmentRecord::getStatus, Constants.ASSESSMENT_IN_PROGRESS)
                        .orderByDesc(AssessmentRecord::getStartTime)
                        .last("LIMIT 1"));
        if (!existingList.isEmpty()) {
            // Complete old duplicates
            List<AssessmentRecord> allExisting = assessmentRecordMapper.selectList(
                    new LambdaQueryWrapper<AssessmentRecord>()
                            .eq(AssessmentRecord::getUserId, userId)
                            .eq(AssessmentRecord::getTypeId, typeId)
                            .eq(AssessmentRecord::getStatus, Constants.ASSESSMENT_IN_PROGRESS));
            if (allExisting.size() > 1) {
                for (int i = 1; i < allExisting.size(); i++) {
                    AssessmentRecord old = allExisting.get(i);
                    old.setStatus(Constants.ASSESSMENT_COMPLETED);
                    old.setEndTime(LocalDateTime.now());
                    assessmentRecordMapper.updateById(old);
                }
            }
            return existingList.get(0).getId();
        }

        AssessmentRecord record = new AssessmentRecord();
        record.setUserId(userId);
        record.setTypeId(typeId);
        record.setStatus(Constants.ASSESSMENT_IN_PROGRESS);
        record.setStartTime(LocalDateTime.now());
        assessmentRecordMapper.insert(record);
        return record.getId();
    }

    @Override
    @Transactional
    public void submitAnswer(AssessmentSubmitDTO dto, Long userId) {
        AssessmentRecord record = assessmentRecordMapper.selectById(dto.getRecordId());
        if (record == null) throw new BusinessException(ErrorCode.ASSESSMENT_RECORD_NOT_FOUND);
        if (!record.getUserId().equals(userId)) throw new BusinessException(ErrorCode.FORBIDDEN);
        if (Constants.ASSESSMENT_COMPLETED.equals(record.getStatus()))
            throw new BusinessException(ErrorCode.ASSESSMENT_ALREADY_COMPLETED);

        // Upsert: delete existing answer for this question, then insert
        assessmentAnswerMapper.delete(
                new LambdaQueryWrapper<AssessmentAnswer>()
                        .eq(AssessmentAnswer::getRecordId, dto.getRecordId())
                        .eq(AssessmentAnswer::getQuestionId, dto.getQuestionId()));

        AssessmentAnswer answer = new AssessmentAnswer();
        answer.setRecordId(dto.getRecordId());
        answer.setQuestionId(dto.getQuestionId());
        answer.setOptionId(dto.getOptionId());
        assessmentAnswerMapper.insert(answer);
    }

    @Override
    @Transactional
    public AssessmentResultVO completeAssessment(Long recordId, Long userId) {
        AssessmentRecord record = assessmentRecordMapper.selectById(recordId);
        if (record == null) throw new BusinessException(ErrorCode.ASSESSMENT_RECORD_NOT_FOUND);
        if (!record.getUserId().equals(userId)) throw new BusinessException(ErrorCode.FORBIDDEN);
        if (Constants.ASSESSMENT_COMPLETED.equals(record.getStatus()))
            throw new BusinessException(ErrorCode.ASSESSMENT_ALREADY_COMPLETED);

        record.setStatus(Constants.ASSESSMENT_COMPLETED);
        record.setEndTime(LocalDateTime.now());
        assessmentRecordMapper.updateById(record);

        AssessmentType type = assessmentTypeMapper.selectById(record.getTypeId());

        // Calculate dimension scores
        Map<String, Integer> dimensionScores = calculateDimensionScores(recordId);
        String resultType = determineResultType(type.getCode(), dimensionScores);

        // Save result
        AssessmentResult result = new AssessmentResult();
        result.setRecordId(recordId);
        result.setUserId(userId);
        result.setTypeId(record.getTypeId());
        result.setDimensionScores(JSONUtil.toJsonStr(dimensionScores));
        result.setResultType(resultType);
        result.setIsAiGenerated(0);
        assessmentResultMapper.insert(result);

        return buildResultVO(result, type, dimensionScores);
    }

    @Override
    public AssessmentResultVO getResult(Long resultId) {
        AssessmentResult result = assessmentResultMapper.selectById(resultId);
        if (result == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        AssessmentType type = assessmentTypeMapper.selectById(result.getTypeId());
        Map<String, Integer> scores = parseDimensionScores(result.getDimensionScores());
        return buildResultVO(result, type, scores);
    }

    @Override
    public List<AssessmentResultVO> myResults(Long userId) {
        List<AssessmentResult> results = assessmentResultMapper.selectList(
                new LambdaQueryWrapper<AssessmentResult>()
                        .eq(AssessmentResult::getUserId, userId)
                        .orderByDesc(AssessmentResult::getCreateTime));
        return results.stream().map(r -> {
            AssessmentType type = assessmentTypeMapper.selectById(r.getTypeId());
            Map<String, Integer> scores = parseDimensionScores(r.getDimensionScores());
            return buildResultVO(r, type, scores);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AssessmentResultVO generateAiReport(Long resultId, Long userId) {
        AssessmentResult result = assessmentResultMapper.selectById(resultId);
        if (result == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        if (!result.getUserId().equals(userId)) throw new BusinessException(ErrorCode.FORBIDDEN);

        AssessmentType type = assessmentTypeMapper.selectById(result.getTypeId());
        Map<String, Integer> scores = parseDimensionScores(result.getDimensionScores());

        // Call AI to generate report
        String report = reportAiService.generateReport(type, result.getResultType(), scores, userId);
        result.setReportText(report);
        result.setIsAiGenerated(1);
        assessmentResultMapper.updateById(result);

        return buildResultVO(result, type, scores);
    }

    // ========== Scoring Logic ==========

    private Map<String, Integer> calculateDimensionScores(Long recordId) {
        List<AssessmentAnswer> answers = assessmentAnswerMapper.selectList(
                new LambdaQueryWrapper<AssessmentAnswer>().eq(AssessmentAnswer::getRecordId, recordId));

        Map<String, Integer> scores = new LinkedHashMap<>();
        for (AssessmentAnswer answer : answers) {
            AssessmentOption option = assessmentOptionMapper.selectById(answer.getOptionId());
            AssessmentQuestion question = assessmentQuestionMapper.selectById(answer.getQuestionId());
            if (option != null && question != null) {
                String dimension = option.getDimension() != null ? option.getDimension() : question.getDimension();
                if (dimension != null) {
                    scores.merge(dimension, option.getScoreValue(), Integer::sum);
                }
            }
        }
        return scores;
    }

    private String determineResultType(String assessmentCode, Map<String, Integer> scores) {
        if (Constants.ASSESSMENT_MBTI.equals(assessmentCode)) {
            return determineMbtiType(scores);
        } else if (Constants.ASSESSMENT_HOLLAND.equals(assessmentCode)) {
            return determineHollandType(scores);
        }
        return "UNKNOWN";
    }

    /**
     * MBTI: Compare each dimension pair — E/I, S/N, T/F, J/P
     * Higher score wins, producing a 4-letter code like "INTJ"
     */
    private String determineMbtiType(Map<String, Integer> scores) {
        StringBuilder type = new StringBuilder();
        type.append(getHigher(scores, "E", "I"));
        type.append(getHigher(scores, "S", "N"));
        type.append(getHigher(scores, "T", "F"));
        type.append(getHigher(scores, "J", "P"));
        return type.toString();
    }

    /**
     * Holland: Rank 6 types (R,I,A,S,E,C) by score, take top 3
     */
    private String determineHollandType(Map<String, Integer> scores) {
        return scores.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.joining());
    }

    private String getHigher(Map<String, Integer> scores, String a, String b) {
        int scoreA = scores.getOrDefault(a, 0);
        int scoreB = scores.getOrDefault(b, 0);
        return scoreA >= scoreB ? a : b;
    }

    // ========== Helpers ==========

    @SuppressWarnings("unchecked")
    private Map<String, Integer> parseDimensionScores(String json) {
        if (json == null || json.isBlank()) return Map.of();
        try {
            return JSONUtil.toBean(json, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }

    private AssessmentResultVO buildResultVO(AssessmentResult result, AssessmentType type, Map<String, Integer> scores) {
        AssessmentResultVO vo = new AssessmentResultVO();
        // Exclude dimensionScores from copy — VO uses Map<String,Integer>, entity uses String(JSON)
        BeanUtil.copyProperties(result, vo, "dimensionScores");
        vo.setTypeName(type != null ? type.getName() : "");
        vo.setTypeCode(type != null ? type.getCode() : "");
        vo.setDimensionScores(scores);
        return vo;
    }
}
