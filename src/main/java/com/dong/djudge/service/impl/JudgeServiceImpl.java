package com.dong.djudge.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONArray;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.djudge.common.exception.CompileException;
import com.dong.djudge.common.exception.SystemException;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.entity.InputFile;
import com.dong.djudge.entity.Test;
import com.dong.djudge.entity.TestCase;
import com.dong.djudge.judge.enums.InputFileEnum;
import com.dong.djudge.judge.task.CompilerTask;
import com.dong.djudge.judge.task.RunTask;
import com.dong.djudge.mapper.TestMapper;
import com.dong.djudge.service.HttpService;
import com.dong.djudge.service.JudgeService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @author 阿东
 * @since 2023/9/6 [0:23]
 */
@SuppressWarnings("ALL")
@Service
public class JudgeServiceImpl extends ServiceImpl<TestMapper, Test> implements JudgeService {

    @Autowired
    private HttpService httpService;

    private RunTask runTask= SpringUtil.getBean(RunTask.class);
    @Override
    public JSONArray oiJudge(JudgeRequest request) throws SystemException, CompileException {
        String fileId;
        fileId= comppile(request.getCode(), request.getLanguage());
        String inputFileContext = request.getInputFileContext();
        JSONArray json = null;
        try {
            json = runTask.runTask(request, fileId, inputFileContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;

    }

    @Override
    public void sapJudge(JudgeRequest request) throws CompileException {
        /*
         * 根据传入的文件类型来判断是哪种方式
         */
        switch (request.getInputFileType()) {
            case 0 -> judgeByJson(request);
            case 1 -> judgeByOss(request);
            case 2 -> judgeByLocalFile(request);
            default -> {
                throw new IllegalStateException("Unexpected value: " + request.getInputFileType());
            }
        }
    }

    @Override
    public String comppile(String code, String language) throws CompileException {
        CompilerTask compilerTask = new CompilerTask();
        JudgeRequest judgeRequest = new JudgeRequest();
        judgeRequest.setCode(code);
        System.out.println(code);
        judgeRequest.setLanguage(language);
        String fileId = compilerTask.compilerTask(judgeRequest);
        return fileId;
    }


    public void judgeByJson(JudgeRequest request) throws CompileException {
        CompilerTask compilerTask = new CompilerTask();
        String fileId = compilerTask.compilerTask(request);
        String inputFileContext = request.getInputFileContext();
        InputFile inputFile = JSONObject.parseObject(inputFileContext, InputFile.class);
        for (TestCase testCase : inputFile.getTestCases()) {
            for (String caseString : testCase.getInput()) {

            }
        }
    }

    public void judgeByOss(JudgeRequest request) {
        String inputFileForOss = httpService.getInputFileForOss(request.getInputFileContext());
    }

    public void judgeByLocalFile(JudgeRequest request) {

    }

    public void parseJson(String json) {
        InputFile inputFile = JSON.parseObject(json, InputFile.class);
    }
}
