package com.careersail.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;

@Data
@TableName("assessment_option")
public class AssessmentOption implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long questionId;
    private String optionText;
    private Integer scoreValue;
    private String dimension;
    private Integer sortOrder;
}
