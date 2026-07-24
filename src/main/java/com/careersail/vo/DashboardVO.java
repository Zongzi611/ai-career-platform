package com.careersail.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardVO {
    private long totalStudents;
    private long totalAssessments;
    private long totalTrainings;
    private double avgTrainingScore;
    private List<Map<String, Object>> mbtiDistribution;
    private List<Map<String, Object>> hollandDistribution;
    private List<Map<String, Object>> trainingScoreByCategory;
    private List<Map<String, Object>> studentProgress;
    private List<Map<String, Object>> careerDemand;
    private List<Map<String, Object>> recentActivities;
}
