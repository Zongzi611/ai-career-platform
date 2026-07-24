package com.careersail.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.careersail.entity.*;
import com.careersail.mapper.*;
import com.careersail.service.DashboardService;
import com.careersail.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final SysUserMapper sysUserMapper;
    private final ClassStudentMapper classStudentMapper;
    private final AssessmentRecordMapper assessmentRecordMapper;
    private final AiTrainingRecordMapper trainingRecordMapper;
    private final AssessmentResultMapper assessmentResultMapper;
    private final CareerInfoMapper careerInfoMapper;

    @Override
    public DashboardVO getOverview(Long teacherId) {
        DashboardVO vo = new DashboardVO();

        // Get students linked to this teacher
        List<ClassStudent> links = classStudentMapper.selectList(
                new LambdaQueryWrapper<ClassStudent>().eq(ClassStudent::getTeacherId, teacherId));
        Set<Long> studentIds = links.stream().map(ClassStudent::getStudentId).collect(Collectors.toSet());

        if (studentIds.isEmpty()) {
            vo.setTotalStudents(0L);
            vo.setTotalAssessments(0L);
            vo.setTotalTrainings(0L);
            vo.setAvgTrainingScore(0.0);
            vo.setMbtiDistribution(List.of());
            vo.setCareerDemand(getCareerDemand());
            return vo;
        }

        vo.setTotalStudents((long) studentIds.size());

        // Assessment count for these students
        Long assessCount = assessmentRecordMapper.selectCount(
                new LambdaQueryWrapper<AssessmentRecord>().in(AssessmentRecord::getUserId, studentIds));
        vo.setTotalAssessments(assessCount);

        // Training count
        Long trainCount = trainingRecordMapper.selectCount(
                new LambdaQueryWrapper<AiTrainingRecord>().in(AiTrainingRecord::getUserId, studentIds));
        vo.setTotalTrainings(trainCount);

        // Avg training score
        List<AiTrainingRecord> trainRecords = trainingRecordMapper.selectList(
                new LambdaQueryWrapper<AiTrainingRecord>().in(AiTrainingRecord::getUserId, studentIds));
        vo.setAvgTrainingScore(trainRecords.stream()
                .filter(r -> r.getAiScore() != null)
                .mapToInt(AiTrainingRecord::getAiScore)
                .average().orElse(0));

        // MBTI distribution for these students
        List<AssessmentResult> results = assessmentResultMapper.selectList(
                new LambdaQueryWrapper<AssessmentResult>()
                        .in(AssessmentResult::getUserId, studentIds)
                        .eq(AssessmentResult::getTypeId, 1L));
        List<Map<String, Object>> mbtiDist = new ArrayList<>();
        Map<String, Long> mbtiCount = new LinkedHashMap<>();
        for (AssessmentResult r : results) {
            if (r.getResultType() != null) mbtiCount.merge(r.getResultType(), 1L, Long::sum);
        }
        mbtiCount.forEach((k, v) -> mbtiDist.add(Map.of("name", k, "value", v)));
        vo.setMbtiDistribution(mbtiDist);

        // Recent activities from students
        List<AssessmentResult> recentAssess = assessmentResultMapper.selectList(
                new LambdaQueryWrapper<AssessmentResult>()
                        .in(AssessmentResult::getUserId, studentIds)
                        .orderByDesc(AssessmentResult::getCreateTime)
                        .last("LIMIT 8"));
        List<Map<String, Object>> activities = new ArrayList<>();
        for (AssessmentResult ar : recentAssess) {
            SysUser stu = sysUserMapper.selectById(ar.getUserId());
            Map<String, Object> act = new LinkedHashMap<>();
            act.put("studentName", stu != null ? stu.getRealName() : "学生");
            act.put("action", "完成了测评，结果：" + (ar.getResultType() != null ? ar.getResultType() : "待查看"));
            act.put("time", ar.getCreateTime() != null ? ar.getCreateTime().toString().replace("T", " ") : "");
            activities.add(act);
        }
        vo.setRecentActivities(activities);

        vo.setCareerDemand(getCareerDemand());
        return vo;
    }

    private List<Map<String, Object>> getCareerDemand() {
        List<Map<String, Object>> demand = new ArrayList<>();
        List<CareerInfo> careers = careerInfoMapper.selectList(null);
        Map<String, Long> demandCount = new LinkedHashMap<>();
        for (CareerInfo c : careers) {
            if (c.getDemandLevel() != null) demandCount.merge(c.getDemandLevel(), 1L, Long::sum);
        }
        demandCount.forEach((k, v) -> demand.add(Map.of("name", k, "value", v)));
        return demand;
    }

    @Override
    public DashboardVO getClassDashboard(Long teacherId, String className) {
        DashboardVO vo = new DashboardVO();
        List<ClassStudent> links = classStudentMapper.selectList(
                new LambdaQueryWrapper<ClassStudent>()
                        .eq(ClassStudent::getTeacherId, teacherId)
                        .eq(ClassStudent::getClassName, className));
        Set<Long> studentIds = links.stream().map(ClassStudent::getStudentId).collect(Collectors.toSet());
        vo.setTotalStudents((long) studentIds.size());
        if (!studentIds.isEmpty()) {
            vo.setTotalAssessments(assessmentRecordMapper.selectCount(
                    new LambdaQueryWrapper<AssessmentRecord>().in(AssessmentRecord::getUserId, studentIds)));
            vo.setTotalTrainings(trainingRecordMapper.selectCount(
                    new LambdaQueryWrapper<AiTrainingRecord>().in(AiTrainingRecord::getUserId, studentIds)));
        }
        return vo;
    }
}
