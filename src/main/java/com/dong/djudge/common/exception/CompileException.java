package com.dong.djudge.common.exception;

import lombok.Data;

/**
 * @Author: Himit_ZH
 * @Date: 2021/1/31 00:16
 * @Description:
 */
@Data
public class CompileException extends Exception {
    private String message;
    private String stdout;
    private String stderr;

    public CompileException(String message, String stdout, String stderr) {
        super(message);
        this.message = message;
        this.stdout = stdout;
        this.stderr = stderr;
    }
}