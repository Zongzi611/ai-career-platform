package com.careersail.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeacherTaskDTO {
    private String title;
    private String description;
    private String taskType;
    private Long refId;
    private String targetClass;
    private LocalDateTime deadline;
}
