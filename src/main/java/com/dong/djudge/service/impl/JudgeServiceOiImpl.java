package com.dong.djudge.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.entity.TestGroupEntity;
import com.dong.djudge.entity.judge.RunResult;
import com.dong.djudge.judge.task.RunTask;
import com.dong.djudge.mapper.TestGroupMapper;
import com.dong.djudge.service.CompileService;
import com.dong.djudge.service.HttpService;
import com.dong.djudge.service.JudgeService;
import com.dong.djudge.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 阿东
 * @since 2023/9/6 [0:23]
 */
@SuppressWarnings("ALL")
@Service("JudgeServiceOiImpl")
public class JudgeServiceOiImpl extends ServiceImpl<TestGroupMapper, TestGroupEntity> implements JudgeService {

    @Autowired
    private HttpService httpService;

    @Autowired
    private CompileService compileService;

    private RunTask runTask = SpringUtil.getBean(RunTask.class);

    /**
     * OI判题，OI模式是指: online input模式，也就是在线输入模式
     * 这种模式下，用户提交的代码会可被编译成执行文件，然后通过标准输入流输入数据，通过标准输出流输出数据。
     * 简单的输入输出模式
     *
     * @param request 请求参数
     * @return 返回判题结果Json
     */
    @Override
    public ResponseResult<Object> Judge(JudgeRequest request) throws Exception {
        // 编译代码 并且得到沙盒里代码编译的文件id
        String fileId = compileService.compile(request);
        String inputFileContext = request.getOiString();
        List<RunResult> runResults = runTask.runTask(request, fileId);
        if (runResults == null) {
            return ResponseResult.failResponse("执行出错");
        }
        return ResponseResult.ok(runResults.get(0));
    }

}
