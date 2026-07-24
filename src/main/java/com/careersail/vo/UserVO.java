package com.careersail.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户视图对象（不含密码）
 */
@Data
public class UserVO {

    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private String avatar;
    private Integer gender;
    private String grade;
    private String major;
    private String college;
    private String className;
    private String studentNo;
    private Integer status;
    private List<String> roles;
    private LocalDateTime createTime;
}
