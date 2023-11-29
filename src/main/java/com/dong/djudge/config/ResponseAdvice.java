package com.dong.djudge.config;

import com.dong.djudge.exception.CompileException;
import com.dong.djudge.exception.SubmitException;
import com.dong.djudge.exception.SystemException;
import com.dong.djudge.exception.CodeRunException;
import com.dong.djudge.enums.ResultStatus;
import com.dong.djudge.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ResponseAdvice  {
    @ExceptionHandler(value = Exception.class)
    public ResponseResult<String> exceptionHandler(Exception e) {
        log.error("全局异常捕获：{}", e.getMessage());
        log.error(e.getCause().getMessage());
        return ResponseResult.exceptionError(e.getMessage());
    }

    @ExceptionHandler(value = SystemException.class)
    public ResponseResult<String> systemExceptionHandler(SystemException e) {
        log.error("系统异常捕获：{}", e.getMessage());
        return new ResponseResult<>(ResultStatus.SYSTEM_ERROR.getCode(), e.getMessage(), null);
    }

    @ExceptionHandler(value = CompileException.class)
    public ResponseResult<String> compileExceptionHandler(CompileException e) {
        log.error("编译异常捕获：{}", e.getMessage());
        return new ResponseResult<>(ResultStatus.COMPILE_ERROR.getCode(), e.getMessage(), null);
    }

    @ExceptionHandler(value = SubmitException.class)
    public ResponseResult<String> submitExceptionHandler(SubmitException e) {
        log.error("提交异常捕获：{}", e.getMessage());
        return new ResponseResult<>(ResultStatus.SUBMIT_ERROR.getCode(), e.getMessage(), null);
    }

    @ExceptionHandler(value = CodeRunException.class)
    public ResponseResult<String> runtimeExceptionHandler(CodeRunException e) {
        log.error("运行异常捕获：{}", e.getMessage());
        return new ResponseResult<>(ResultStatus.RUNTIME_ERROR.getCode(), e.getMessage(), null);
    }

}
