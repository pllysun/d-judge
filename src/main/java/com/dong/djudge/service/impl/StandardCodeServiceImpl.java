package com.dong.djudge.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.dto.StaredCodeDTO;
import com.dong.djudge.entity.StandardCodeEntity;
import com.dong.djudge.entity.TestGroupEntity;
import com.dong.djudge.entity.judge.RunResult;
import com.dong.djudge.entity.judge.StandardCode;
import com.dong.djudge.enums.CodeRunTypeEnum;
import com.dong.djudge.enums.InputFileEnum;
import com.dong.djudge.enums.ModeEnum;
import com.dong.djudge.exception.CompileException;
import com.dong.djudge.exception.SystemException;
import com.dong.djudge.judge.task.RunTask;
import com.dong.djudge.mapper.StandardCodeMapper;
import com.dong.djudge.mapper.TestGroupMapper;
import com.dong.djudge.service.CompileService;
import com.dong.djudge.service.StandardCodeService;
import com.dong.djudge.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 樊东升
 * @date 2023/11/30 21:39
 */
@Service
public class StandardCodeServiceImpl extends ServiceImpl<StandardCodeMapper, StandardCodeEntity> implements StandardCodeService {

    @Autowired
    StandardCodeMapper  standardCodeMapper;

    @Autowired
    TestGroupMapper testGroupMapper;

    @Autowired
    private CompileService compileService;

    private RunTask runTask= SpringUtil.getBean(RunTask.class);
    @Override
    public ResponseResult<String> standardCodeRun(StaredCodeDTO staredCodeDTO) throws Exception {
        LambdaQueryWrapper<TestGroupEntity> testGroupEntityLambdaQueryWrapper = new QueryWrapper<TestGroupEntity>().lambda();
        testGroupEntityLambdaQueryWrapper.eq(TestGroupEntity::getTestGroupId, staredCodeDTO.getTestGroupId());
        TestGroupEntity testGroupEntity = testGroupMapper.selectOne(testGroupEntityLambdaQueryWrapper);
        if (testGroupEntity == null) {
            return ResponseResult.failResponse(staredCodeDTO.getTestGroupId()+"不存在！");
        }
        JudgeRequest judgeRequest = getJudgeRequest(staredCodeDTO);
        String fileId= compileService.compile(judgeRequest);
        List<RunResult> runResults = runTask.runTask(judgeRequest, fileId);
        return null;
    }

    private static JudgeRequest getJudgeRequest(StaredCodeDTO staredCodeDTO) {
        JudgeRequest judgeRequest = new JudgeRequest();
        StandardCode standardCode = new StandardCode();
        standardCode.setRunCodeType(CodeRunTypeEnum.BEFORE.getValue());
        standardCode.setRunCodeLanguage(staredCodeDTO.getLanguage());
        standardCode.setRunCode(staredCodeDTO.getCode());
        standardCode.setInputFileType(InputFileEnum.FILE.getValue());
        standardCode.setInputFileContext(staredCodeDTO.getTestGroupId());
        judgeRequest.setStandardCode(standardCode);
        judgeRequest.setLanguage(staredCodeDTO.getLanguage());
        judgeRequest.setCode(staredCodeDTO.getCode());
        judgeRequest.setModeType(ModeEnum.OJ.getName());
        return judgeRequest;
    }
}
