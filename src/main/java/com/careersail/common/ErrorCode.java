package com.careersail.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一错误码枚举
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 通用错误
    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或 Token 已过期"),
    FORBIDDEN(403, "无权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "不支持的请求方法"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 业务错误 (1xxx)
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_PASSWORD_ERROR(1002, "用户名或密码错误"),
    USER_ACCOUNT_DISABLED(1003, "账号已被禁用"),
    USER_USERNAME_EXISTS(1004, "用户名已存在"),
    USER_STUDENT_NO_EXISTS(1005, "学号已存在"),

    // 角色错误 (2xxx)
    ROLE_NOT_FOUND(2001, "角色不存在"),

    // 职业知识库错误 (3xxx)
    CAREER_NOT_FOUND(3001, "岗位信息不存在"),
    CAREER_IMPORT_FAILED(3002, "岗位数据导入失败"),

    // 测评错误 (4xxx)
    ASSESSMENT_TYPE_NOT_FOUND(4001, "测评类型不存在"),
    ASSESSMENT_RECORD_NOT_FOUND(4002, "测评记录不存在"),
    ASSESSMENT_ALREADY_COMPLETED(4003, "测评已完成，不可重复提交"),
    ASSESSMENT_IN_PROGRESS(4004, "已有进行中的测评，请先完成"),

    // AI 错误 (5xxx)
    AI_SERVICE_ERROR(5001, "AI 服务调用失败"),
    AI_RAG_RETRIEVAL_ERROR(5002, "知识库检索失败"),
    AI_REPORT_GENERATE_ERROR(5003, "报告生成失败"),
    AI_SCORING_ERROR(5004, "AI 评分失败"),

    // 实训错误 (6xxx)
    TRAINING_TASK_NOT_FOUND(6001, "实训任务不存在"),
    TRAINING_ALREADY_SUBMITTED(6002, "已提交过该任务"),
    TRAINING_NOT_SCORED(6003, "尚未完成评分"),

    // 教师管理错误 (7xxx)
    TEACHER_TASK_NOT_FOUND(7001, "教学任务不存在"),
    CLASS_STUDENT_EXISTS(7002, "该学生已在班级中"),
    REPORT_NOT_FOUND(7003, "报告不存在"),

    // 文件错误 (8xxx)
    FILE_UPLOAD_FAILED(8001, "文件上传失败"),
    FILE_FORMAT_ERROR(8002, "文件格式不支持"),
    FILE_SIZE_EXCEEDED(8003, "文件大小超出限制"),

    // 系统配置错误 (9xxx)
    CONFIG_NOT_FOUND(9001, "系统配置不存在"),
    CONFIG_KEY_EXISTS(9002, "配置键已存在");

    private final int code;
    private final String message;
}
