package com.careersail.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度3-50位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度6-32位")
    private String password;

    private String realName;

    private String email;

    private String phone;

    private String grade;

    private String major;

    private String college;

    private String className;

    private String studentNo;

    /** 注册角色: student / teacher，默认 student */
    private String role = "student";
}
