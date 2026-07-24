package com.careersail.dto;

import lombok.Data;

/**
 * 用户查询条件
 */
@Data
public class UserQueryDTO {

    private String username;
    private String realName;
    private String major;
    private String className;
    private String studentNo;
    private Integer status;
    private Long roleId;
}

