package com.careersail.dto;

import lombok.Data;

/**
 * 分页请求参数
 */
@Data
public class PageDTO {

    private long page = 1;
    private long size = 10;
    private String keyword;
    private String sortField;
    private String sortOrder;  // asc / desc
}
