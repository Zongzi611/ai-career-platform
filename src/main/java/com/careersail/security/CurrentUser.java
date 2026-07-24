package com.careersail.security;

import java.lang.annotation.*;

/**
 * 当前登录用户注解 — 用于 Controller 方法参数注入
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
