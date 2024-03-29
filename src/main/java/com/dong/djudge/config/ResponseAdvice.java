package com.dong.djudge.config;

import com.dong.djudge.enums.ResultStatus;
import com.dong.djudge.exception.CodeRunException;
import com.dong.djudge.exception.CompileException;
import com.dong.djudge.exception.SubmitException;
import com.dong.djudge.exception.SystemException;
import com.dong.djudge.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ResponseAdvice {
    @ExceptionHandler(value = Exception.class)
    public ResponseResult<String> exceptionHandler(Exception e) {
        log.error("全局异常捕获：{}", e.getMessage());
        if (e.getCause() != null) {
            log.error("全局异常捕获原因：{}", e.getCause().getMessage());
        } else {
            log.error("全局异常捕获原因：null");
        }
        return ResponseResult.exceptionError(e.getMessage());
    }

    @ExceptionHandler(value = SystemException.class)
    public ResponseResult<String> systemExceptionHandler(SystemException e) {
        log.warn("系统异常捕获：{}", e.getMessage());
        return new ResponseResult<>(ResultStatus.SYSTEM_ERROR.getCode(), e.getMessage(), null);
    }

    @ExceptionHandler(value = CompileException.class)
    public ResponseResult<String> compileExceptionHandler(CompileException e) {
        log.warn("编译异常捕获：{}", e.getMessage());
        if(e.getStderr()==null||e.getStderr().isEmpty()||e.getStderr().equalsIgnoreCase("0")){
            return new ResponseResult<>(ResultStatus.COMPILE_ERROR.getCode(), e.getMessage(), null);
        }
        return new ResponseResult<>(ResultStatus.COMPILE_ERROR.getCode(), e.getStderr(), null);
    }

    @ExceptionHandler(value = SubmitException.class)
    public ResponseResult<String> submitExceptionHandler(SubmitException e) {
        log.warn("提交异常捕获：{}", e.getMessage());
        if(e.getStderr()==null||e.getStderr().isEmpty()||e.getStderr().equalsIgnoreCase("0")){
            new ResponseResult<>(ResultStatus.SUBMIT_ERROR.getCode(), e.getMessage(), null);
        }
        return new ResponseResult<>(ResultStatus.SUBMIT_ERROR.getCode(), e.getStderr(), null);
    }

    @ExceptionHandler(value = CodeRunException.class)
    public ResponseResult<String> runtimeExceptionHandler(CodeRunException e) {
        log.warn("运行异常捕获：{}", e.getMessage());
        if(e.getStderr()==null||e.getStderr().isEmpty()||e.getStderr().equalsIgnoreCase("0")){
            new ResponseResult<>(ResultStatus.RUNTIME_ERROR.getCode(), e.getMessage(), null);
        }
        return new ResponseResult<>(ResultStatus.RUNTIME_ERROR.getCode(), e.getStderr(), null);
    }

}
