package com.dong.djudge.enums;

import lombok.Getter;

/**
 * 响应返回状态码
 */
@Getter
public enum ResultStatus {
    /**
     * 评测结果状态码
     */
    SUCCESS(200, "成功"),
    FAIL(400, "请求失败"),
    NOT_FOUND(404, "资源未找到"),
    BAD_REQUEST(400, "错误请求"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    METHOD_NOT_ALLOWED(405, "方法不允许"),
    NOT_ACCEPTABLE(406, "无法接受"),
    REQUEST_TIMEOUT(408, "请求超时"),
    CONFLICT(409, "冲突"),
    GONE(410, "已经不存在"),
    LENGTH_REQUIRED(411, "需要长度"),
    PRECONDITION_FAILED(412, "先决条件失败"),
    PAYLOAD_TOO_LARGE(413, "负载过大"),
    UNSUPPORTED_MEDIA_TYPE(415, "不支持的媒体类型"),
    TOO_MANY_REQUESTS(429, "请求过多"),
    EXCEPTION_ERROR(500, "内部服务器错误"),
    SYSTEM_ERROR(501, "系统错误"),
    COMPILE_ERROR(502, "编译错误"),
    RUNTIME_ERROR(503, "运行时错误"),
    SUBMIT_ERROR(504, "提交错误"),
    DOWNLOAD_ERROR(505, "下载错误");

    private final Integer code;
    private final String message;

    ResultStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ResultStatus getByCode(Integer code) {
        for (ResultStatus value : ResultStatus.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

}