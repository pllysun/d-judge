package com.dong.djudge.controller;

import com.dong.djudge.common.exception.CompileException;
import com.dong.djudge.common.exception.SystemException;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.service.CompileService;
import com.dong.djudge.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * @author 樊东升
 * @date 2023/11/27 21:37
 */
@RestController
public class CompileController {
    @Autowired
    private CompileService compileService;

    @GetMapping(value = "/Compiler")
    public Result<Objects> compiler(@RequestBody JudgeRequest request) throws SystemException, CompileException {
        String compile = compileService.compile(request);
        return Result.successResponse(compile);
    }
}
