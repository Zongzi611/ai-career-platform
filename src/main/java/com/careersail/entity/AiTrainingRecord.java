package com.careersail.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("ai_training_record")
public class AiTrainingRecord implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long taskId;
    private String userAnswer;
    private Integer aiScore;
    private String aiFeedback;
    private String status;
    private LocalDateTime submitTime;
    private LocalDateTime scoreTime;
}
