package com.dong.djudge.controller;

import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.exception.CompileException;
import com.dong.djudge.exception.SystemException;
import com.dong.djudge.service.CompileService;
import com.dong.djudge.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 编译代码服务类
 *
 * @author 樊东升
 * @date 2023/11/27 21:37
 */
@RestController
public class CompileController {
    @Autowired
    private CompileService compileService;

    @GetMapping(value = "/Compiler")
    public ResponseResult<Objects> compiler(@RequestBody JudgeRequest request) throws SystemException, CompileException {
        String compile = compileService.compile(request);
        return ResponseResult.successResponse(compile);
    }
}
