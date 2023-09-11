package com.dong.djudge.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Author: Himit_ZH
 * @Date: 2022/3/9 15:17
 * @Description:
 */
@Getter
@AllArgsConstructor
public enum ResultStatus {

    /**
     * 成功
     */
    SUCCESS(200,"成功"),
    /**
     * 失败
     */
    FAIL(400,"失败"),
    /**
     * 访问受限
     */
    ACCESS_DENIED(401,"访问受限"),
    /**
     * 拒绝访问
     */
    FORBIDDEN(403,"拒绝访问"),
    /**
     * 数据不存在
     */
    NOT_FOUND(404,"数据不存在"),
    /**
     * 系统错误
     */
    SYSTEM_ERROR(500,"系统错误");


    private int status;

    private String description;
}