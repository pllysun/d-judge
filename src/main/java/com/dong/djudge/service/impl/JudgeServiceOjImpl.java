package com.dong.djudge.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.entity.InCaseGroupRoot;
import com.dong.djudge.entity.OutCaseGroupRoot;
import com.dong.djudge.entity.SaveCaseGroupRoot;
import com.dong.djudge.entity.StandardCodeEntity;
import com.dong.djudge.entity.TestGroupEntity;
import com.dong.djudge.entity.judge.RunResultRoot;
import com.dong.djudge.enums.JudgeStateEnum;
import com.dong.djudge.judge.task.RunTask;
import com.dong.djudge.mapper.StandardCodeMapper;
import com.dong.djudge.mapper.TestGroupMapper;
import com.dong.djudge.service.CompileService;
import com.dong.djudge.service.HttpService;
import com.dong.djudge.service.JudgeService;
import com.dong.djudge.util.CommonUtils;
import com.dong.djudge.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    private final RunTask runTask = SpringUtil.getBean(RunTask.class);

    @Override
    public ResponseResult<Object> Judge(JudgeRequest request) throws Exception {
        // 编译代码，并获取编译后的文件ID
        String fileId = compileService.compile(request);
        // 运行任务，并获取运行结果
        RunResultRoot runResultRoot = runTask.runTask(request, fileId);
        // 如果运行结果为空，则返回执行出错的响应
        if (runResultRoot == null) {
            return ResponseResult.failResponse("执行出错");
        }
        // 如果运行结果的状态不为空且不等于"接受"，则返回失败响应
        if (runResultRoot.getState() != null && !JudgeStateEnum.ACCEPTED.getDescription().equals(runResultRoot.getState())) {
            return ResponseResult.failResponse(runResultRoot.getState(), runResultRoot.getInput());
        }
        // 创建查询条件，查询标准代码实体
        LambdaQueryWrapper<StandardCodeEntity> lambda = new QueryWrapper<StandardCodeEntity>().lambda();
        lambda.eq(StandardCodeEntity::getCodeId, request.getStandardCode().getRunCodeId());
        // 查询标准代码实体
        StandardCodeEntity standardCodeEntity = standardCodeMapper.selectOne(lambda);
        // 如果标准代码实体为空，则返回没有测试集答案的失败响应
        if (standardCodeEntity == null) {
            return ResponseResult.failResponse("没有测试集答案");
        }
        // 获取运行结果列表
        List<SaveCaseGroupRoot> ta = CommonUtils.getRunResultList(runResultRoot);
        // 获取文件的JSON内容
        String jsonForFile = CommonUtils.getJsonForFile(request.getStandardCode().getRunCodeId());
        // 从JSON内容中获取保存的测试组
        List<SaveCaseGroupRoot> sa = CommonUtils.getSaveTestGroupForJson(jsonForFile);
        // 获取测试用例组根列表
        List<OutCaseGroupRoot> outCaseGroupRootList = CommonUtils.getTestCaseGroupRoots(ta, sa);
        // 获取用例结果
        List<OutCaseGroupRoot> caseResult = CommonUtils.getCaseResult(runResultRoot, outCaseGroupRootList);
        // 遍历用例结果，如果有未接受的用例，则返回错误答案的失败响应
        for (OutCaseGroupRoot outCaseGroupRoot : caseResult) {
            if(!outCaseGroupRoot.isGroupAccepted()){
                return ResponseResult.failResponse(JudgeStateEnum.WRONG_ANSWER.getDescription(),caseResult);
            }
        }
        // 所有用例都被接受，返回成功响应
        return ResponseResult.successResponse(JudgeStateEnum.ACCEPTED.getDescription(), caseResult);
    }
}
