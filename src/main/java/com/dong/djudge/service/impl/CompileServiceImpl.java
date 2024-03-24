package com.dong.djudge.service.impl;

import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.exception.CompileException;
import com.dong.djudge.judge.task.CompilerTask;
import com.dong.djudge.service.CompileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 樊东升
 * @date 2023/11/27 21:36
 * 编译的接口实现类，主要调用编译相关的代码来获取编译完毕沙箱返回的文件ID
 */
@Service
@Slf4j
public class CompileServiceImpl implements CompileService {

    private final CompilerTask compilerTask;

    @Autowired
    public CompileServiceImpl(CompilerTask compilerTask) {
        this.compilerTask = compilerTask;
    }

    @Override
    public String compile(JudgeRequest judgeRequest) throws CompileException {
        log.debug("code:{}", judgeRequest.getCode());
        return compilerTask.compilerTask(judgeRequest);
    }
}
