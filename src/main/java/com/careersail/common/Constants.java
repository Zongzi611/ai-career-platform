package com.careersail.common;

/**
 * 系统常量
 */
public final class Constants {

    private Constants() {}

    // JWT
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_HEADER = "Authorization";
    public static final String REDIS_TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    public static final String REDIS_TOKEN_REFRESH_PREFIX = "token:refresh:";

    // Redis 缓存前缀
    public static final String REDIS_CONFIG_PREFIX = "config:";
    public static final String REDIS_CAREER_CACHE = "career:list";
    public static final String REDIS_CHAT_SESSION_PREFIX = "chat:session:";
    public static final String REDIS_ASSESSMENT_PREFIX = "assessment:";

    // 角色编码
    public static final String ROLE_STUDENT = "ROLE_STUDENT";
    public static final String ROLE_TEACHER = "ROLE_TEACHER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    // 测评类型编码
    public static final String ASSESSMENT_MBTI = "MBTI";
    public static final String ASSESSMENT_HOLLAND = "HOLLAND";

    // 测评状态
    public static final String ASSESSMENT_IN_PROGRESS = "IN_PROGRESS";
    public static final String ASSESSMENT_COMPLETED = "COMPLETED";

    // 实训状态
    public static final String TRAINING_SUBMITTED = "SUBMITTED";
    public static final String TRAINING_SCORED = "SCORED";

    // 教师任务状态
    public static final String TASK_ACTIVE = "ACTIVE";
    public static final String TASK_CLOSED = "CLOSED";

    // 通用状态
    public static final int STATUS_ENABLED = 1;
    public static final int STATUS_DISABLED = 0;

    // 分页
    public static final long DEFAULT_PAGE = 1;
    public static final long DEFAULT_SIZE = 10;
    public static final long MAX_SIZE = 100;
}
