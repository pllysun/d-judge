package com.dong.djudge.judge.task;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONArray;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.entity.TestCaseGroup;
import com.dong.djudge.entity.TestGroupEntity;
import com.dong.djudge.entity.judge.RunResult;
import com.dong.djudge.enums.InputFileEnum;
import com.dong.djudge.enums.ModeEnum;
import com.dong.djudge.judge.LanguageConfigLoader;
import com.dong.djudge.judge.entity.LanguageConfig;
import com.dong.djudge.judge.service.RunService;
import com.dong.djudge.mapper.TestGroupMapper;
import com.dong.djudge.util.JsonUtils;
import com.dong.djudge.util.TestGroupUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author 阿东
 * @since 2023/9/10 [14:05]
 */
@Component
@Slf4j(topic = "RunTask")
public class RunTask {
    @Autowired
    TestGroupMapper testGroupMapper;

    private final RunService runService = SpringUtil.getBean(RunService.class);

    public List<RunResult> runTask(JudgeRequest request, String fileId) throws Exception {
        LanguageConfigLoader languageConfigLoader = new LanguageConfigLoader();
        //获取语言配置
        LanguageConfig languageConfigByName = languageConfigLoader.getLanguageConfigByName(request.getLanguage());
        JSONArray objects;
        List<RunResult> runResultList = new ArrayList<>();
        if (request.getModeType().equals(ModeEnum.OI.getName())) {
            objects = runService.testCase(languageConfigByName, request, fileId, request.getOiString());
            RunResult runResult = JSON.parseArray(objects.toString(), RunResult.class).get(0);
            runResultList.add(runResult);
        } else {

            if (request.getStandardCode().getInputFileType().equals(InputFileEnum.JSON.getValue())) {
                if (!JsonUtils.isValidJson(request.getStandardCode().getInputFileContext())) {
                    return null;
                }
                runResultList = getRunResultList(request, fileId, languageConfigByName);
            } else if (request.getStandardCode().getInputFileType().equals(InputFileEnum.URL.getValue())) {
                String jsonForURL = TestGroupUtils.getJsonForURL(request.getStandardCode().getInputFileContext());
                if (jsonForURL == null) {
                    return null;
                }
                request.getStandardCode().setInputFileContext(jsonForURL);
                runResultList = getRunResultList(request, fileId, languageConfigByName);
            } else {
                LambdaQueryWrapper<TestGroupEntity> testGroupEntityLambdaQueryWrapper = new QueryWrapper<TestGroupEntity>().lambda();
                testGroupEntityLambdaQueryWrapper.eq(TestGroupEntity::getTestGroupId, request.getStandardCode().getInputFileContext());
                TestGroupEntity testGroupEntity = testGroupMapper.selectOne(testGroupEntityLambdaQueryWrapper);
                if (testGroupEntity == null) {
                    return null;
                }
                String jsonForFile = TestGroupUtils.getJsonForFile(testGroupEntity.getTestGroupPath());
                request.getStandardCode().setInputFileContext(jsonForFile);
                runResultList = getRunResultList(request, fileId, languageConfigByName);
            }

        }
        return runResultList;
    }

    private List<RunResult> getRunResultList(JudgeRequest request, String fileId, LanguageConfig languageConfigByName) {
        //通过虚拟线程同时运行所有测试用例
        List<RunResult> list;
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<RunResult>> futures = new ArrayList<>();
            for (TestCaseGroup testCaseGroup : JsonUtils.getTestCaseGroupList(request.getStandardCode().getInputFileContext())) {
                for (String test : testCaseGroup.getInput()) {
                    Callable<RunResult> task = () -> {
                        // 执行任务，这里简单返回一个字符串
                        JSONArray finalObjects = runService.testCase(languageConfigByName, request, fileId, test);
                        List<RunResult> runResults = null;
                        if (finalObjects != null) {
                            runResults = JSON.parseArray(finalObjects.toString(), RunResult.class);
                        }
                        RunResult runResult = null;
                        if (runResults != null) {
                            runResult = runResults.get(0);
                        }
                        return runResult;
                    };
                    Future<RunResult> future = executorService.submit(task);
                    futures.add(future);
                }
            }
            list = new ArrayList<>();
            for (Future<RunResult> future : futures) {
                list.add(future.get());
            }
            executorService.shutdown();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return list;

    }
}
