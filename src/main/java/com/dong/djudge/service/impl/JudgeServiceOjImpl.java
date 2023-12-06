package com.dong.djudge.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.entity.StandardCodeEntity;
import com.dong.djudge.entity.TestCaseGroup;
import com.dong.djudge.entity.TestCaseGroupRoot;
import com.dong.djudge.entity.TestGroupEntity;
import com.dong.djudge.entity.judge.RunResult;
import com.dong.djudge.entity.judge.RunResultRoot;
import com.dong.djudge.exception.CompileException;
import com.dong.djudge.exception.SystemException;
import com.dong.djudge.judge.task.RunTask;
import com.dong.djudge.mapper.StandardCodeMapper;
import com.dong.djudge.mapper.TestGroupMapper;
import com.dong.djudge.service.CompileService;
import com.dong.djudge.service.HttpService;
import com.dong.djudge.service.JudgeService;
import com.dong.djudge.util.CommonUtils;
import com.dong.djudge.util.ResponseResult;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 樊东升
 * @date 2023/11/30 21:40
 */
@Service("JudgeServiceOjImpl")
public class JudgeServiceOjImpl extends ServiceImpl<TestGroupMapper, TestGroupEntity> implements JudgeService {

    @Autowired
    private HttpService httpService;

    @Autowired
    private CompileService compileService;

    @Autowired
    private StandardCodeMapper standardCodeMapper;

    private RunTask runTask = SpringUtil.getBean(RunTask.class);

    @Override
    public ResponseResult<Object> Judge(JudgeRequest request) throws Exception {
        // 编译代码 并且得到沙盒里代码编译的文件id
        String fileId = compileService.compile(request);
        RunResultRoot runResultRoot = runTask.runTask(request, fileId);
        if (runResultRoot == null) {
            return ResponseResult.failResponse("执行出错");
        }
        if (runResultRoot.getState() != null && !"Accepted".equals(runResultRoot.getState())) {
            return ResponseResult.failResponse(runResultRoot.getState());
        }
        LambdaQueryWrapper<StandardCodeEntity> lambda = new QueryWrapper<StandardCodeEntity>().lambda();
        lambda.eq(StandardCodeEntity::getCodeId, request.getStandardCode().getRunCodeId());
        StandardCodeEntity standardCodeEntity = standardCodeMapper.selectOne(lambda);
        if (standardCodeEntity == null) {
            return ResponseResult.failResponse("没有测试集答案");
        }
        List<TestCaseGroupRoot> testCaseGroupRoots = CommonUtils.getTestCaseGroupRoots(runResultRoot);
        String jsonForFile = CommonUtils.getJsonForFile(request.getStandardCode().getRunCodeId());
        List<TestCaseGroupRoot> testCaseGroupList = CommonUtils.getTestCaseGroupList(jsonForFile);
        if (CollectionUtils.isEqualCollection(testCaseGroupRoots, testCaseGroupList)) {
            return ResponseResult.ok("Accepted");
        } else {
            return ResponseResult.failResponse("Wrong Answer");
        }
    }
}
