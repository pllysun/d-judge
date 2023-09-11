package com.dong.djudge.judge.service.impl;

import cn.hutool.json.JSONArray;
import com.dong.djudge.common.exception.SystemException;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.judge.SandboxRun;
import com.dong.djudge.judge.entity.LanguageConfig;
import com.dong.djudge.judge.service.RunService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 阿东
 * @since 2023/9/10 [15:22]
 */
@Service
@Slf4j(topic = "doj")
public class OiRunServiceImpl implements RunService {

    /**
     * 最大进程数
     */
    private static final int maxProcessNumber = 128;
    /**
     * 默认最大运行时间，单位ns 10s
     */
    Long maxTime = 10000000000L;
    /**
     * 最大运行内存，单位byte 128M
     */
    Long maxMemory = 134217728L;
    /**
     * 最大栈空间，单位byte 128M
     */
    Long maxStack = 10240L;
    /**
     * 最大输出，单位byte 10K
     */
    Integer maxOutput = 134217728;
    @Override
    public JSONArray testCase(LanguageConfig languageConfig, JudgeRequest request, String fileId, String testCaseContent) throws SystemException {
        List<String> args=new ArrayList<>();
        args.add(languageConfig.getRunCommand());
        JSONArray objects = SandboxRun.testCase(args,
                languageConfig.getRunEnvs(),
                request.getMaxTime()==null?maxTime:request.getMaxTime(),
                request.getMaxMemory()==null?maxMemory:request.getMaxMemory(),
                request.getMaxStack()==null?maxStack:request.getMaxStack(),
                request.getMaxOutput()==null?maxOutput:request.getMaxOutput(),
                languageConfig.getExeName(),
                fileId,
                testCaseContent,
                false);
        SandboxRun.delFile(fileId);
        log.info("testCase:{}", objects);
        return objects;
    }
}
