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
        String language = CommonUtils.getLanguage(request.getLanguage());
        LanguageConfig languageConfigByName = languageConfigLoader.getLanguageConfigByName(language);
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
                    return new RunResultRoot("1");
                }
                runResultRoot = getRunResultList(request, fileId, languageConfigByName);
            } else if (request.getStandardCode().getInputFileType().equals(InputFileEnum.URL.getValue())) {
                String jsonForURL = CommonUtils.getJsonForURL(request.getStandardCode().getInputFileContext());
                if (jsonForURL == null) {
                    return new RunResultRoot("2");
                }
                request.getStandardCode().setInputFileContext(jsonForURL);
                runResultRoot = getRunResultList(request, fileId, languageConfigByName);
            } else {
                LambdaQueryWrapper<TestGroupEntity> testGroupEntityLambdaQueryWrapper = new QueryWrapper<TestGroupEntity>().lambda();
                testGroupEntityLambdaQueryWrapper.eq(TestGroupEntity::getTestGroupId, request.getStandardCode().getInputFileContext());
                TestGroupEntity testGroupEntity = testGroupMapper.selectOne(testGroupEntityLambdaQueryWrapper);
                if (testGroupEntity == null) {
                    return new RunResultRoot("3");
                }
                String jsonForFile = CommonUtils.getJsonForFile(testGroupEntity.getTestGroupId());
                request.getStandardCode().setInputFileContext(jsonForFile);
                runResultRoot = getRunResultList(request, fileId, languageConfigByName);
            }

        }
        return runResultRoot;
    }

    private RunResultRoot getRunResultList(JudgeRequest request, String fileId, LanguageConfig languageConfigByName) {
        // 创建一个运行结果根对象
        RunResultRoot runResultRoot = new RunResultRoot();
        // 创建一个新的虚拟线程执行器，用于并行运行所有测试用例
        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            // 创建一个Future列表，用于存储每个测试用例的运行结果
            List<Future<RunResultForTestGroup>> futures = new ArrayList<>();
            // 遍历输入的测试组
            for (InCaseGroupRoot inCaseGroupRoot : CommonUtils.getInTestGroupForJson(request.getStandardCode().getInputFileContext())) {
                Integer gid = inCaseGroupRoot.getGid();
                // 遍历每个测试组中的测试用例
                for (InTestCaseGroup inTestCaseGroup : inCaseGroupRoot.getInput()) {
                    Integer id = inTestCaseGroup.getId();
                    // 创建一个任务，该任务将运行测试用例并返回运行结果
                    Callable<RunResultForTestGroup> task = () -> {
                        // 运行测试用例
                        JSONArray finalObjects = runService.testCase(languageConfigByName, request, fileId, inTestCaseGroup.getValue());
                        // 解析运行结果
                        RunResultForTestGroup runResult = JSON.parseArray(finalObjects.toString(), RunResultForTestGroup.class).getFirst();
                        // 设置运行结果的gid和id
                        runResult.setGid(gid);
                        runResult.setId(id);
                        // 设置运行结果的输入
                        runResult.setInput(inTestCaseGroup.getValue());
                        // 如果运行结果的状态不是"接受"，则取消所有剩余的Future，并设置运行结果根的错误信息、状态和输入
                        if (!JudgeStateEnum.ACCEPTED.getDescription().equals(runResult.getOriginalStatus())) {
                            for (Future<RunResultForTestGroup> remainingFuture : futures) {
                                remainingFuture.cancel(true);
                            }

                            runResultRoot.setErrorInfo(runResult.getFiles().getStderr());
                            runResultRoot.setState(runResult.getOriginalStatus());
                            runResultRoot.setInput(inTestCaseGroup.getValue());
                        }
                        // 返回运行结果
                        return runResult;
                    };
                    // 提交任务并将Future添加到列表中
                    Future<RunResultForTestGroup> future = executorService.submit(task);
                    futures.add(future);
                }
            }
            // 创建一个列表，用于存储所有运行结果
            List<RunResultForTestGroup> list = new ArrayList<>();
            // 从每个Future中获取运行结果并添加到列表中
            for (Future<RunResultForTestGroup> future : futures) {
                list.add(future.get());
            }
            // 设置运行结果根的运行结果
            runResultRoot.setRunResult(list);
            // 关闭执行器
            executorService.shutdown();
            // 运行完毕后删除沙盒中的文件
            SandBoxRun sandBoxRun = sandBoxRunMapper.selectOne(new QueryWrapper<SandBoxRun>().lambda().eq(SandBoxRun::getFileId, fileId));
            SandboxRun.delFile(sandBoxRun.getBaseUrl(), fileId);
        } catch (CancellationException e) {
            // 如果任务被取消，删除沙盒中的文件并记录警告
            SandBoxRun sandBoxRun = sandBoxRunMapper.selectOne(new QueryWrapper<SandBoxRun>().lambda().eq(SandBoxRun::getFileId, fileId));
            SandboxRun.delFile(sandBoxRun.getBaseUrl(), fileId);
            log.warn("任务取消:{}", e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
            // 如果执行异常，抛出运行时异常
            throw new RuntimeException(e);
        }
        // 返回运行结果根
        return runResultRoot;
    }
}
