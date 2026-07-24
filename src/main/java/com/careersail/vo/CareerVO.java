package com.careersail.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CareerVO {
    private Long id;
    private String positionName;
    private String industry;
    private String majorMatch;
    private Integer salaryMin;
    private Integer salaryMax;
    private String skillsRequired;
    private String careerPath;
    private String description;
    private String demandLevel;
    private Double similarityScore;
    private LocalDateTime createTime;
}
