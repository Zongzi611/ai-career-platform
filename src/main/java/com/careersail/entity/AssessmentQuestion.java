package com.careersail.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("assessment_question")
public class AssessmentQuestion implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long typeId;
    private String questionText;
    private String questionType;
    private String dimension;
    private Integer sortOrder;
    private LocalDateTime createTime;

    @TableField(exist = false)
    private List<AssessmentOption> options;
}
