package com.dong.djudge.util;


import com.dong.djudge.enums.ResultStatus;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 响应数据
 *
 * @since 1.0.0
 */
@Data
public class ResponseResult<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 响应状态码
     */
    private int code;
    /**
     * 消息内容
     */
    private String msg;
    /**
     * 响应数据
     */
    private T data;

    public ResponseResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public ResponseResult() {
    }


    public static <T> ResponseResult<T> exceptionError(String msg) {
        return new ResponseResult<T>(ResultStatus.EXCEPTION_ERROR.getCode(), msg, null);
    }

    public static <T> ResponseResult<T> ok(T data) {
        return new ResponseResult<>(ResultStatus.SUCCESS.getCode(), "成功", data);
    }

    public static <T> ResponseResult<T> successResponse(String msg) {
        return new ResponseResult<T>(ResultStatus.SUCCESS.getCode(), msg, null);
    }


    public static <T> ResponseResult<T> failResponse(String msg) {
        return new ResponseResult<T>(ResultStatus.FAIL.getCode(), msg, null);
    }

    public static <T> ResponseResult<T> failResponse(T data) {
        return new ResponseResult<T>(ResultStatus.FAIL.getCode(), "失败", data);
    }

    public static <T> ResponseResult<T> failResponse(String message, T data) {
        return new ResponseResult<T>(ResultStatus.FAIL.getCode(), message, data);
    }


    public static <T> ResponseResult<T> systemError(String message, T data) {
        return new ResponseResult<T>(ResultStatus.SYSTEM_ERROR.getCode(), message, data);
    }
}
