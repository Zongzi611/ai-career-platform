package com.careersail.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("class_student")
public class ClassStudent implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String className;
    private Long studentId;
    private Long teacherId;
    private LocalDateTime createTime;
}
