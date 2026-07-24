package com.careersail.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("career_bookmark")
public class CareerBookmark implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long careerId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
