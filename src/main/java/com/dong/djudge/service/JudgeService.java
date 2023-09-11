package com.dong.djudge.service;

import cn.hutool.json.JSONArray;
import com.baomidou.mybatisplus.extension.service.IService;
import com.dong.djudge.common.exception.CompileException;
import com.dong.djudge.common.exception.SystemException;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.entity.Test;

import java.util.List;

/**
 * @author 阿东
 * @date 2023/9/6 [0:23]
 */
public interface JudgeService extends IService<Test> {
    /**
     * 判题服务类
     * @param request 请求参数
     */
    void sapJudge(JudgeRequest request) throws SystemException, CompileException;

    String comppile(String code, String language) throws SystemException, CompileException;

    /**
     * OI判题，OI模式是指: online input模式，也就是在线输入模式
     * 这种模式下，用户提交的代码会被编译成可执行文件，然后通过标准输入流输入数据，通过标准输出流输出数据。
     * 简单的输入输出模式
     * @param request 请求参数
     * @return 返回判题结果Json
     */
    JSONArray oiJudge(JudgeRequest request) throws SystemException, CompileException;
}
