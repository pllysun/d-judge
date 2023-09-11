package com.dong.djudge.judge.service;

import cn.hutool.json.JSONArray;
import com.dong.djudge.common.exception.SystemException;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.judge.entity.LanguageConfig;

/**
 * @author 阿东
 * @since 2023/9/10 [15:21]
 */
public interface RunService {
    /**
     * oi测试判题
     *
     * @param languageConfig  语言配置
     * @param fileId          文件ID
     * @param request         其他参数
     * @param testCaseContent 测试用例内容
     * @return 返回判题结果Json
     * @throws SystemException 系统异常
     */
    JSONArray testCase(LanguageConfig languageConfig, JudgeRequest request, String fileId, String testCaseContent) throws SystemException;
}
