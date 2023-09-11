

package com.dong.djudge.util;


import com.dong.djudge.common.ResultStatus;
import lombok.Data;

import java.io.Serializable;

/**
 * 响应数据
 *
 * @since 1.0.0
 */
@Data
public class Result<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 编码：0表示成功，其他值表示失败
     */
    private int code = 0;
    /**
     * 消息内容
     */
    private String msg = "success";
    /**
     * 响应数据
     */
    private T data;

    public Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public Result() {
    }

    public Result<T> ok(T data) {
        this.setData(data);
        return this;
    }

    public static <T> Result<T> successResponse(String msg) {
        return new Result<T>(ResultStatus.FAIL.getStatus(), msg, null);
    }

    public static <T> Result<T> successResponse(T data) {
        return new Result<T>(ResultStatus.SUCCESS.getStatus(), "成功", data);
    }

    public static <T> Result<T> errorResponse(String msg) {
        return new Result<T>(ResultStatus.FAIL.getStatus(), msg, null);
    }

    public static <T> Result<T> errorResponse(String msg, ResultStatus resultStatus) {
        return new Result<T>(resultStatus.getStatus(), msg, null);
    }

    public <T> Result<T>  error(T data) {
        return new Result<T>(ResultStatus.FAIL.getStatus(), "失败", data);
    }

    public <T> Result<T>  error(String message, T data) {
        return new Result<T>(ResultStatus.FAIL.getStatus(), message, data);
    }

    public <T> Result<T>  systemError(String message, T data) {
        return new Result<T>(ResultStatus.SYSTEM_ERROR.getStatus(), message, data);
    }
}
