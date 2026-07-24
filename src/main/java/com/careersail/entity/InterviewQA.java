package com.careersail.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("interview_qa")
public class InterviewQA implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long sessionId;
    private Integer roundNum;
    private String question;
    private String userAnswer;
    private String aiFeedback;
    private Integer score;
    private LocalDateTime createTime;
}
