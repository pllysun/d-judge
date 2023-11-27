package com.dong.djudge.service.impl;

import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONArray;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.djudge.common.exception.CompileException;
import com.dong.djudge.common.exception.SystemException;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.entity.InputFile;
import com.dong.djudge.entity.Test;
import com.dong.djudge.entity.TestCase;
import com.dong.djudge.judge.task.CompilerTask;
import com.dong.djudge.judge.task.RunTask;
import com.dong.djudge.mapper.TestMapper;
import com.dong.djudge.service.CompileService;
import com.dong.djudge.service.HttpService;
import com.dong.djudge.service.JudgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 阿东
 * @since 2023/9/6 [0:23]
 */
@SuppressWarnings("ALL")
@Service
public class JudgeServiceImplForIO extends ServiceImpl<TestMapper, Test> implements JudgeService {

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
    public JSONArray Judge(JudgeRequest request) throws SystemException, CompileException {
        // 编译代码 并且得到沙盒里代码编译的文件id
        String fileId= compileService.compile(request);
        String inputFileContext = request.getInputFileContext();
        JSONArray json = null;
        try {
            json = runTask.runTask(request, fileId, inputFileContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;

    }

    /**
     * 根据 JSON 数据进行评测。
     *
     * @param request 评测请求对象，包含评测所需的信息。
     * @throws CompileException 如果编译过程中发生异常，则抛出 CompileException。
     */
    public void judgeByJson(JudgeRequest request) throws CompileException {
        // 创建编译器任务
        CompilerTask compilerTask = new CompilerTask();
        // 调用编译器任务进行编译，获取文件ID
        String fileId = compilerTask.compilerTask(request);
        // 获取输入文件的内容
        String inputFileContext = request.getInputFileContext();
        // 将输入文件内容解析为 InputFile 对象
        InputFile inputFile = JSONObject.parseObject(inputFileContext, InputFile.class);
        // 遍历输入文件的测试用例
        for (TestCase testCase : inputFile.getTestCases()) {
            // 遍历测试用例的输入数据
            for (String caseString : testCase.getInput()) {
                // 在这里执行相应的评测逻辑，处理测试用例的输入数据

            }
        }
    }


}
