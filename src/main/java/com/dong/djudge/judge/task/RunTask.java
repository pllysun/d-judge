package com.dong.djudge.judge.task;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONArray;
import com.dong.djudge.exception.SystemException;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.judge.LanguageConfigLoader;
import com.dong.djudge.judge.entity.LanguageConfig;
import com.dong.djudge.judge.service.RunService;
import org.springframework.stereotype.Component;

/**
 * @author 阿东
 * @since 2023/9/10 [14:05]
 */
@Component
public class RunTask {

    private final RunService runService= SpringUtil.getBean(RunService.class);

    public JSONArray runTask(JudgeRequest request,String fileId,String fileContent) throws SystemException {
        LanguageConfigLoader languageConfigLoader = new LanguageConfigLoader();
        //获取语言配置
        LanguageConfig languageConfigByName = languageConfigLoader.getLanguageConfigByName(request.getLanguage());
        return runService.testCase(languageConfigByName,request,fileId,fileContent);
    }
}
