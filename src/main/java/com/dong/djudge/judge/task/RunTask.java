package com.dong.djudge.judge.task;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONArray;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dong.djudge.entity.TestCaseGroup;
import com.dong.djudge.entity.TestGroupEntity;
import com.dong.djudge.entity.judge.RunResult;
import com.dong.djudge.enums.InputFileEnum;
import com.dong.djudge.enums.ModeEnum;
import com.dong.djudge.exception.SystemException;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.judge.LanguageConfigLoader;
import com.dong.djudge.judge.entity.LanguageConfig;
import com.dong.djudge.judge.service.RunService;
import com.dong.djudge.mapper.TestGroupMapper;
import com.dong.djudge.util.JsonUtils;
import com.dong.djudge.util.TestGroupUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author 阿东
 * @since 2023/9/10 [14:05]
 */
@Component
public class RunTask {
    @Autowired
    TestGroupMapper testGroupMapper;

    private final RunService runService= SpringUtil.getBean(RunService.class);

    public List<RunResult>  runTask(JudgeRequest request,String fileId) throws Exception {
        List<RunResult> list=new ArrayList<>();
        LanguageConfigLoader languageConfigLoader = new LanguageConfigLoader();
        //获取语言配置
        LanguageConfig languageConfigByName = languageConfigLoader.getLanguageConfigByName(request.getLanguage());
        JSONArray objects;
        if(request.getModeType().equals(ModeEnum.OI.getName())){
            objects= runService.testCase(languageConfigByName, request, fileId, request.getOiString());
            list.add(JSON.parseObject(objects.toString(), RunResult.class));
        }else{
            if(request.getStandardCode().getInputFileType().equals(InputFileEnum.JSON.getValue())){
                if(!JsonUtils.isValidJson(request.getStandardCode().getInputFileContext())){
                    return null;
                }
                getRunResultList(request, fileId, languageConfigByName, list);
            }else if(request.getStandardCode().getInputFileType().equals(InputFileEnum.URL.getValue())){
                String jsonForURL = TestGroupUtils.getJsonForURL(request.getStandardCode().getInputFileContext());
                if(jsonForURL==null){
                    return null;
                }
                request.getStandardCode().setInputFileContext(jsonForURL);
                getRunResultList(request, fileId, languageConfigByName, list);
            }else{
                LambdaQueryWrapper<TestGroupEntity> testGroupEntityLambdaQueryWrapper = new QueryWrapper<TestGroupEntity>().lambda();
                testGroupEntityLambdaQueryWrapper.eq(TestGroupEntity::getTestGroupId, request.getStandardCode().getInputFileContext());
                TestGroupEntity testGroupEntity = testGroupMapper.selectOne(testGroupEntityLambdaQueryWrapper);
                if(testGroupEntity==null){
                    return null;
                }
                String jsonForFile = TestGroupUtils.getJsonForFile(testGroupEntity.getTestGroupPath());
                request.getStandardCode().setInputFileContext(jsonForFile);
                getRunResultList(request, fileId, languageConfigByName, list);
            }

        }

        return list;
    }

    private void getRunResultList(JudgeRequest request, String fileId, LanguageConfig languageConfigByName, List<RunResult> list) throws SystemException {
        AtomicInteger i= new AtomicInteger();
        for (TestCaseGroup testCaseGroup : JsonUtils.getTestCaseGroupList(request.getStandardCode().getInputFileContext())) {
            for (String test : testCaseGroup.getInput()) {
                Thread.startVirtualThread(()->{
                    JSONArray  objects= null;
                    try {
                        System.out.println(test);
                        objects = runService.testCase(languageConfigByName, request, fileId, test);
                    } catch (SystemException e) {
                        System.out.println("lklklkl");
                        System.out.println(e.getMessage());
                    }
                    JSONArray finalObjects = objects;
                    List<RunResult> runResults = JSON.parseArray(finalObjects.toString(), RunResult.class);
                    list.add(runResults.get(0));
                    System.out.println(i.getAndIncrement());
                });

            }
        }
        try {
            Thread.sleep(10000);
            System.out.println(list);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
