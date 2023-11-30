package com.dong.djudge.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONArray;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.djudge.entity.TestCaseGroup;
import com.dong.djudge.entity.judge.RunResult;
import com.dong.djudge.enums.InputFileEnum;
import com.dong.djudge.exception.CompileException;
import com.dong.djudge.exception.SystemException;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.entity.FileEntity;
import com.dong.djudge.judge.task.CompilerTask;
import com.dong.djudge.judge.task.RunTask;
import com.dong.djudge.mapper.FileMapper;
import com.dong.djudge.service.CompileService;
import com.dong.djudge.service.HttpService;
import com.dong.djudge.service.JudgeService;
import com.dong.djudge.util.JsonUtils;
import com.dong.djudge.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 阿东
 * @since 2023/9/6 [0:23]
 */
@SuppressWarnings("ALL")
@Service
public class JudgeServiceImplForIO extends ServiceImpl<FileMapper, FileEntity> implements JudgeService {

    @Autowired
    private HttpService httpService;

    @Autowired
    private CompileService compileService;

    private RunTask runTask= SpringUtil.getBean(RunTask.class);

    /**
     * OI判题，OI模式是指: online input模式，也就是在线输入模式
     * 这种模式下，用户提交的代码会可被编译成执行文件，然后通过标准输入流输入数据，通过标准输出流输出数据。
     * 简单的输入输出模式
     * @param request 请求参数
     * @return 返回判题结果Json
     */
    @Override
    public ResponseResult<Object> Judge(JudgeRequest request) throws SystemException, CompileException {
        // 编译代码 并且得到沙盒里代码编译的文件id
        String fileId= compileService.compile(request);
        String inputFileContext = request.getInputFileContext();
        List<RunResult> runResults = runTask.runTask(request, fileId, inputFileContext);
        return ResponseResult.successResponse(runResults.get(0));
    }

}
