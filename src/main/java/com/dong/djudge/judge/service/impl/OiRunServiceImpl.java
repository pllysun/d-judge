package com.dong.djudge.judge.service.impl;

import cn.hutool.json.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.entity.judge.CodeSetting;
import com.dong.djudge.exception.SystemException;
import com.dong.djudge.judge.SandboxRun;
import com.dong.djudge.judge.entity.LanguageConfig;
import com.dong.djudge.judge.service.RunService;
import com.dong.djudge.mapper.SandBoxRunMapper;
import com.dong.djudge.pojo.SandBoxRun;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.dong.djudge.judge.service.impl.CompilerServiceImpl.parseCompileCommand;

/**
 * @author 阿东
 * @since 2023/9/10 [15:22]
 */
@Service
@Slf4j(topic = "doj")
public class OiRunServiceImpl implements RunService {


    /**
     * 默认最大运行时间，单位ns 1s
     */
    Integer maxTime = 1000;
    /**
     * 最大运行内存，单位Mb  128M
     */
    Integer maxMemory = 128;
    /**
     * 最大栈空间，单位Mb  128M
     */
    Integer maxStack = 128;
    /**
     * 最大输出，单位byte 10K
     */
    Integer maxOutput = 10;

    @Autowired
    private SandBoxRunMapper sandBoxRunMapper;

    @Override
    public JSONArray testCase(LanguageConfig languageConfig, JudgeRequest request, String fileId, String testCaseContent) throws SystemException {
        List<String> args = new ArrayList<>();
        if (request.getCodeSetting() == null) {
            request.setCodeSetting(new CodeSetting());
        }
        LambdaQueryWrapper<SandBoxRun> lambda = new QueryWrapper<SandBoxRun>().lambda();
        lambda.eq(SandBoxRun::getFileId, fileId);
        SandBoxRun sandBoxRun = sandBoxRunMapper.selectOne(lambda);
        args = parseCompileCommand(languageConfig.getRunCommand());
        JSONArray objects = SandboxRun.testCase(sandBoxRun.getBaseUrl(), args,
                languageConfig.getRunEnvs(),
                request.getCodeSetting().getMaxTime() == null ? maxTime : request.getCodeSetting().getMaxTime(),
                request.getCodeSetting().getMaxMemory() == null ? maxMemory : request.getCodeSetting().getMaxMemory(),
                request.getCodeSetting().getMaxStack() == null ? maxStack : request.getCodeSetting().getMaxStack(),
                request.getCodeSetting().getMaxOutput() == null ? maxOutput : request.getCodeSetting().getMaxOutput(),
                languageConfig.getExeName(),
                fileId,
                testCaseContent,
                false);
        //SandboxRun.delFile(fileId);
        log.info("testCase:{}", objects);
        return objects;
    }
}
