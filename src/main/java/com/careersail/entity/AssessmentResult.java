package com.careersail.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("assessment_result")
public class AssessmentResult implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recordId;
    private Long userId;
    private Long typeId;
    private String dimensionScores;  // JSON string
    private String resultType;
    private String reportText;
    private Integer isAiGenerated;
    private LocalDateTime createTime;
}
