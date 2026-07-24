package com.careersail.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统用户
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String realName;

    private String email;

    private String phone;

    private String avatar;

    /** 0=未知 1=男 2=女 */
    private Integer gender;

    /** 年级，如 2024级 */
    private String grade;

    /** 专业 */
    private String major;

    /** 学院 */
    private String college;

    /** 班级名称 */
    private String className;

    /** 学号 */
    private String studentNo;

    /** 0=禁用 1=启用 */
    @TableLogic(value = "1", delval = "0")
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
