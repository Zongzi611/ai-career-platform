package com.careersail.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统角色
 */
@Data
@TableName("sys_role")
public class SysRole implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色显示名称 */
    private String roleName;

    /** 角色编码: ROLE_STUDENT / ROLE_TEACHER / ROLE_ADMIN */
    private String roleCode;

    private String description;

    @TableLogic(value = "1", delval = "0")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
