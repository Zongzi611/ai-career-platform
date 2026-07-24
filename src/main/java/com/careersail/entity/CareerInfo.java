package com.careersail.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 职业知识库
 */
@Data
@TableName("career_info")
public class CareerInfo implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 岗位名称 */
    private String positionName;

    /** 所属行业 */
    private String industry;

    /** 匹配专业(逗号分隔) */
    private String majorMatch;

    /** 最低月薪(K) */
    private Integer salaryMin;

    /** 最高月薪(K) */
    private Integer salaryMax;

    /** 所需技能 */
    private String skillsRequired;

    /** 职业晋升路径 */
    private String careerPath;

    /** 岗位详细描述 */
    private String description;

    /** 市场需求: HIGH/MEDIUM/LOW */
    private String demandLevel;

    @TableLogic(value = "1", delval = "0")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
