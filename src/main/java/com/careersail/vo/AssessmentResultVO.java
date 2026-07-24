package com.careersail.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class AssessmentResultVO {
    private Long id;
    private Long recordId;
    private Long typeId;
    private String typeName;
    private String typeCode;
    private Map<String, Integer> dimensionScores;
    private String resultType;
    private String reportText;
    private Integer isAiGenerated;
    private LocalDateTime createTime;
}
