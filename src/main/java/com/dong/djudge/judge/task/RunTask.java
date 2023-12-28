package com.dong.djudge.judge.task;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONArray;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.entity.InCaseGroupRoot;
import com.dong.djudge.entity.InTestCaseGroup;
import com.dong.djudge.entity.TestGroupEntity;
import com.dong.djudge.entity.judge.RunResult;
import com.dong.djudge.entity.judge.RunResultForTestGroup;
import com.dong.djudge.entity.judge.RunResultRoot;
import com.dong.djudge.enums.InputFileEnum;
import com.dong.djudge.enums.JudgeStateEnum;
import com.dong.djudge.enums.ModeEnum;
import com.dong.djudge.judge.LanguageConfigLoader;
import com.dong.djudge.judge.SandboxRun;
import com.dong.djudge.judge.entity.LanguageConfig;
import com.dong.djudge.judge.service.RunService;
import com.dong.djudge.mapper.SandBoxRunMapper;
import com.dong.djudge.mapper.TestGroupMapper;
import com.dong.djudge.pojo.SandBoxRun;
import com.dong.djudge.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
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
    private final RunService runService = SpringUtil.getBean(RunService.class);
    @Autowired
    TestGroupMapper testGroupMapper;

    @Autowired
    private SandBoxRunMapper sandBoxRunMapper;

    public RunResultRoot runTask(JudgeRequest request, String fileId) throws Exception {
        LanguageConfigLoader languageConfigLoader = new LanguageConfigLoader();
        //获取语言配置
        LanguageConfig languageConfigByName = languageConfigLoader.getLanguageConfigByName(request.getLanguage());
        JSONArray objects;
        RunResultRoot runResultRoot;
        if (request.getModeType().equalsIgnoreCase(ModeEnum.OI.getName())) {
            objects = runService.testCase(languageConfigByName, request, fileId, request.getOiString());
            RunResult runResult = JSON.parseArray(objects.toString(), RunResult.class).get(0);
            runResultRoot = new RunResultRoot(0, 0, runResult);
            SandBoxRun sandBoxRun = sandBoxRunMapper.selectOne(new QueryWrapper<SandBoxRun>().lambda().eq(SandBoxRun::getFileId, fileId));
            SandboxRun.delFile(sandBoxRun.getBaseUrl(), fileId);
        } else {

            if (request.getStandardCode().getInputFileType().equals(InputFileEnum.JSON.getValue())) {
                if (!CommonUtils.isValidJson(request.getStandardCode().getInputFileContext())) {
                    return null;
                }
                runResultRoot = getRunResultList(request, fileId, languageConfigByName);
            } else if (request.getStandardCode().getInputFileType().equals(InputFileEnum.URL.getValue())) {
                String jsonForURL = CommonUtils.getJsonForURL(request.getStandardCode().getInputFileContext());
                if (jsonForURL == null) {
                    return null;
                }
                request.getStandardCode().setInputFileContext(jsonForURL);
                runResultRoot = getRunResultList(request, fileId, languageConfigByName);
            } else {
                LambdaQueryWrapper<TestGroupEntity> testGroupEntityLambdaQueryWrapper = new QueryWrapper<TestGroupEntity>().lambda();
                testGroupEntityLambdaQueryWrapper.eq(TestGroupEntity::getTestGroupId, request.getStandardCode().getInputFileContext());
                TestGroupEntity testGroupEntity = testGroupMapper.selectOne(testGroupEntityLambdaQueryWrapper);
                if (testGroupEntity == null) {
                    return null;
                }
                String jsonForFile = CommonUtils.getJsonForFile(testGroupEntity.getTestGroupId());
                request.getStandardCode().setInputFileContext(jsonForFile);
                runResultRoot = getRunResultList(request, fileId, languageConfigByName);
            }

        }
        return runResultRoot;
    }

    private RunResultRoot getRunResultList(JudgeRequest request, String fileId, LanguageConfig languageConfigByName) {
        //通过虚拟线程同时运行所有测试用例
        RunResultRoot runResultRoot = new RunResultRoot();
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<RunResultForTestGroup>> futures = new ArrayList<>();
            for (InCaseGroupRoot inCaseGroupRoot : CommonUtils.getInTestGroupForJson(request.getStandardCode().getInputFileContext())) {
                Integer gid = inCaseGroupRoot.getGid();
                for (InTestCaseGroup inTestCaseGroup : inCaseGroupRoot.getInput()) {
                    Integer id = inTestCaseGroup.getId();
                    Callable<RunResultForTestGroup> task = () -> {
                        // 执行任务，这里简单返回一个字符串
                        JSONArray finalObjects = runService.testCase(languageConfigByName, request, fileId, inTestCaseGroup.getValue());
                        RunResultForTestGroup runResult = JSON.parseArray(finalObjects.toString(), RunResultForTestGroup.class).getFirst();
                        runResult.setGid(gid);
                        runResult.setId(id);
                        if (!JudgeStateEnum.ACCEPTED.getDescription().equals(runResult.getOriginalStatus())) {
                            for (Future<RunResultForTestGroup> remainingFuture : futures) {
                                remainingFuture.cancel(true);
                            }
                            runResultRoot.setErrorInfo(runResult.getFiles().getStderr());
                            runResultRoot.setState(runResult.getOriginalStatus());
                            runResultRoot.setInput(inTestCaseGroup.getValue());
                        }
                        return runResult;
                    };
                    Future<RunResultForTestGroup> future = executorService.submit(task);
                    futures.add(future);
                }
            }
            List<RunResultForTestGroup> list = new ArrayList<>();
            for (Future<RunResultForTestGroup> future : futures) {
                list.add(future.get());
            }
            runResultRoot.setRunResult(list);
            executorService.shutdown();
            //运行完毕删除沙盒里面的文件
            SandBoxRun sandBoxRun = sandBoxRunMapper.selectOne(new QueryWrapper<SandBoxRun>().lambda().eq(SandBoxRun::getFileId, fileId));
            SandboxRun.delFile(sandBoxRun.getBaseUrl(), fileId);
        } catch (CancellationException e) {
            SandBoxRun sandBoxRun = sandBoxRunMapper.selectOne(new QueryWrapper<SandBoxRun>().lambda().eq(SandBoxRun::getFileId, fileId));
            SandboxRun.delFile(sandBoxRun.getBaseUrl(), fileId);
            log.warn("任务取消:{}", e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return runResultRoot;

    }
}
