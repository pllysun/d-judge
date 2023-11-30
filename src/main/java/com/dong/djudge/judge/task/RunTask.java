package com.dong.djudge.judge.task;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONArray;
import com.alibaba.fastjson2.JSON;
import com.dong.djudge.entity.TestCaseGroup;
import com.dong.djudge.entity.judge.RunResult;
import com.dong.djudge.exception.SystemException;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.judge.LanguageConfigLoader;
import com.dong.djudge.judge.entity.LanguageConfig;
import com.dong.djudge.judge.service.RunService;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author 阿东
 * @since 2023/9/10 [14:05]
 */
@Component
public class RunTask {

    private final RunService runService= SpringUtil.getBean(RunService.class);

    public List<RunResult>  runTask(JudgeRequest request,String fileId,String fileContent) throws SystemException {
        LanguageConfigLoader languageConfigLoader = new LanguageConfigLoader();
        //获取语言配置
        LanguageConfig languageConfigByName = languageConfigLoader.getLanguageConfigByName(request.getLanguage());
        JSONArray objects = runService.testCase(languageConfigByName, request, fileId, fileContent);
        return JSON.parseArray(objects.toString(), RunResult.class);
    }
}
