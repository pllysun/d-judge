package com.dong.djudge.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.dto.standardCodeDTO;
import com.dong.djudge.dto.UpdatestandardCodeDTO;
import com.dong.djudge.entity.InCaseGroupRoot;
import com.dong.djudge.entity.SaveCaseGroupRoot;
import com.dong.djudge.entity.SaveTestCaseGroup;
import com.dong.djudge.entity.StandardCodeEntity;
import com.dong.djudge.entity.TestGroupEntity;
import com.dong.djudge.entity.judge.RunResultRoot;
import com.dong.djudge.entity.judge.StandardCode;
import com.dong.djudge.enums.CodeRunTypeEnum;
import com.dong.djudge.enums.InputFileEnum;
import com.dong.djudge.enums.ModeEnum;
import com.dong.djudge.judge.task.RunTask;
import com.dong.djudge.mapper.StandardCodeMapper;
import com.dong.djudge.mapper.TestGroupMapper;
import com.dong.djudge.service.CompileService;
import com.dong.djudge.service.StandardCodeService;
import com.dong.djudge.util.CommonUtils;
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

    private final RunTask runTask = SpringUtil.getBean(RunTask.class);
    @Autowired
    StandardCodeMapper standardCodeMapper;
    @Autowired
    TestGroupMapper testGroupMapper;
    @Autowired
    private CompileService compileService;
    RunResultRoot runResultRoot;

    private static JudgeRequest getJudgeRequest(standardCodeDTO standardCodeDTO) {
        JudgeRequest judgeRequest = new JudgeRequest();
        StandardCode standardCode = new StandardCode();
        standardCode.setRunCodeType(CodeRunTypeEnum.BEFORE.getValue());
        standardCode.setRunCodeLanguage(standardCodeDTO.getLanguage());
        standardCode.setRunCode(standardCodeDTO.getCode());
        standardCode.setInputFileType(InputFileEnum.FILE.getValue());
        standardCode.setInputFileContext(standardCodeDTO.getTestGroupId());
        judgeRequest.setStandardCode(standardCode);
        judgeRequest.setLanguage(standardCodeDTO.getLanguage());
        judgeRequest.setCode(standardCodeDTO.getCode());
        judgeRequest.setModeType(ModeEnum.OJ.getName());
        return judgeRequest;
    }

    /**
     * 根据提供的 standardCodeDTO 执行标准代码运行。
     * 初始化代码执行，获取测试用例组根，返回执行结果。
     *
     * @param standardCodeDTO 包含代码执行信息的 standardCodeDTO。
     * @return 包含标准代码运行结果的 ResponseResult。
     * @throws Exception 如果在代码执行过程中发生错误。
     */
    @Override
    public ResponseResult<String> standardCodeRun(standardCodeDTO standardCodeDTO) throws Exception {
        // 初始化代码执行
        ResponseResult<String> init = init(standardCodeDTO);
        if (init != null) {
            return init;
        }
        // 获取测试用例组根
        List<SaveCaseGroupRoot> list = CommonUtils.getRunResultList(runResultRoot);
        // 返回带有代码 ID 的结果
        return getCodeId(standardCodeDTO, list);
    }

    @Override
    public ResponseResult<String> standardCode(UpdatestandardCodeDTO updatestandardCodeDTO) throws Exception {
        standardCodeDTO standardCodeDTO = new standardCodeDTO(updatestandardCodeDTO);
        ResponseResult<String> init = init(standardCodeDTO);
        if (init != null) {
            return init;
        }
        // 获取测试用例组根
        List<SaveCaseGroupRoot> list = CommonUtils.getRunResultList(runResultRoot);
        // 将测试用例组根转换为 JSON 字符串
        String json = JSON.toJSONString(list);

        // 生成带有前缀的唯一代码ID
        String jsonFileId = updatestandardCodeDTO.getStandardCodeId();

        // 将 JSON 写入带有生成的代码ID的文件中
        CommonUtils.writeFile(jsonFileId, json);

        // 在数据库中插入记录，包括生成的代码ID和测试组ID
        StandardCodeEntity standardCodeEntity = new StandardCodeEntity();
        standardCodeEntity.setTestGroupId(standardCodeDTO.getTestGroupId());
        LambdaQueryWrapper<StandardCodeEntity> lambda = new QueryWrapper<StandardCodeEntity>().lambda();
        lambda.eq(StandardCodeEntity::getCodeId, jsonFileId);
        standardCodeMapper.update(standardCodeEntity, lambda);

        // 返回带有生成的代码ID的 ResponseResult
        return ResponseResult.ok(jsonFileId);
    }

    @Override
    public String getStandardCode(String standardCodeId) throws Exception {
        return CommonUtils.readFile(standardCodeId);
    }

    @Override
    public ResponseResult<String> deleteStandardCode(String standardCodeId) {
        LambdaQueryWrapper<StandardCodeEntity> lambda = new QueryWrapper<StandardCodeEntity>().lambda();
        lambda.eq(StandardCodeEntity::getCodeId, standardCodeId);
        try {
            standardCodeMapper.delete(lambda);
            CommonUtils.deleteLocalFile(standardCodeId);
        } catch (Exception e) {
            return ResponseResult.failResponse("删除失败", e.getMessage());
        }
        return ResponseResult.successResponse("删除成功");
    }



    /**
     * 通过验证测试组、编译代码并运行任务初始化代码执行。
     * 如果出现问题，返回带有错误消息的 ResponseResult。
     *
     * @param standardCodeDTO 包含代码执行信息的 standardCodeDTO。
     * @return 如果出现问题，带有错误消息的 ResponseResult；否则返回 null。
     * @throws Exception 如果在代码初始化过程中发生错误。
     */
    private ResponseResult<String> init(standardCodeDTO standardCodeDTO) throws Exception {
        // 根据测试组ID查询测试组实体
        LambdaQueryWrapper<TestGroupEntity> testGroupEntityLambdaQueryWrapper =
                new QueryWrapper<TestGroupEntity>().lambda();
        testGroupEntityLambdaQueryWrapper.eq(TestGroupEntity::getTestGroupId, standardCodeDTO.getTestGroupId());
        TestGroupEntity testGroupEntity = testGroupMapper.selectOne(testGroupEntityLambdaQueryWrapper);

        // 检查测试组实体是否存在
        if (testGroupEntity == null) {
            return ResponseResult.failResponse(standardCodeDTO.getTestGroupId() + "不存在！");
        }

        // 获取用于代码执行的 JudgeRequest
        JudgeRequest judgeRequest = getJudgeRequest(standardCodeDTO);

        // 检查测试组文件是否存在
        if (!CommonUtils.isFileExist(standardCodeDTO.getTestGroupId())) {
            return ResponseResult.failResponse("该测试集文件不存在");
        }

        // 编译代码并获取文件ID
        String fileId = compileService.compile(judgeRequest);

        // 运行任务并获取运行结果
        runResultRoot = runTask.runTask(judgeRequest, fileId);

        // 检查代码执行过程中是否有错误
        if (runResultRoot == null) {
            return ResponseResult.failResponse("执行出错");
        }

        // 检查执行状态，如果不是 "Accepted" 则返回错误响应
        if (runResultRoot.getState() != null && !"Accepted".equals(runResultRoot.getState())) {
            return ResponseResult.failResponse(runResultRoot.getState());
        }

        // 如果初始化成功则返回 null
        return null;
    }

    /**
     * 生成唯一的代码ID，将测试用例组根写入文件，并插入数据库记录。
     * 返回带有生成的代码ID的 ResponseResult。
     *
     * @param standardCodeDTO 包含代码执行信息的 standardCodeDTO。
     * @param list          表示测试用例组的 TestCaseGroupRoot 列表。
     * @return 带有生成的代码ID的 ResponseResult。
     * @throws Exception 如果在代码执行过程中发生错误。
     */
    private ResponseResult<String> getCodeId(standardCodeDTO standardCodeDTO, List<SaveCaseGroupRoot> list) throws Exception {
        // 将测试用例组根转换为 JSON 字符串
        String json = JSON.toJSONString(list);

        // 生成带有前缀的唯一代码ID
        String jsonFileId = CommonUtils.generateRandomUpperCaseWithPrefix("SC-", 12);

        // 将 JSON 写入带有生成的代码ID的文件中
        CommonUtils.writeFile(jsonFileId, json);

        // 在数据库中插入记录，包括生成的代码ID和测试组ID
        StandardCodeEntity standardCodeEntity = new StandardCodeEntity();
        standardCodeEntity.setCodeId(jsonFileId);
        standardCodeEntity.setTestGroupId(standardCodeDTO.getTestGroupId());
        standardCodeMapper.insert(standardCodeEntity);

        // 返回带有生成的代码ID的 ResponseResult
        return ResponseResult.ok(jsonFileId);
    }


}
