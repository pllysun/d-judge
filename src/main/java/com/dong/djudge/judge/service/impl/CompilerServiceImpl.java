package com.dong.djudge.judge.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.dong.djudge.common.exception.CompileException;
import com.dong.djudge.common.exception.SubmitException;
import com.dong.djudge.common.exception.SystemException;
import com.dong.djudge.judge.SandboxRun;
import com.dong.djudge.judge.entity.LanguageConfig;
import com.dong.djudge.judge.service.CompilerService;
import com.dong.djudge.util.Constants;
import com.dong.djudge.util.JudgeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.List;

/**
 * @author 阿东
 * @since 2023/9/10 [15:23]
 */
@Service
@Slf4j(topic = "doj")
public class CompilerServiceImpl implements CompilerService {
    /**
     * 最大时间和最大内存-单位:ms 10s
     */
    Long maxTime = 10000L;
    /**
     * 最大内存 100M
     */
    Long maxMemory = 104857600L;

    /**
     * 编译
     * 这个方法是编译的核心方法，只涉及到编译，不涉及运行，将源代码编译为可执行文件。
     *
     * @param languageConfig 语言配置
     * @param code           代码
     * @param language       语言
     * @param extraFiles     额外文件
     * @return 文件id
     * @throws SystemException  系统错误
     * @throws CompileException 编译错误
     * @throws SubmitException  提交错误
     */
    @Override
    public String compile(LanguageConfig languageConfig, String code, String language, HashMap<String, String> extraFiles) throws SystemException, CompileException, SubmitException {
        // 检查是否支持指定的编程语言，如果languageConfig为null，则抛出RuntimeException
        if (languageConfig == null) {
            throw new RuntimeException("Unsupported language " + language);
        }

        // 调用安全沙箱的compile方法进行编译
        JSONArray result = SandboxRun.compile(languageConfig.getMaxCpuTime(),
                languageConfig.getMaxRealTime() == null ? maxTime : languageConfig.getMaxRealTime(),
                languageConfig.getMaxMemory() == null ? maxMemory : languageConfig.getMaxMemory(),
                256 * 1024 * 1024L,
                languageConfig.getSrcName(),
                languageConfig.getExeName(),
                parseCompileCommand(languageConfig.getCompileCommand()),
                languageConfig.getCompileEnvs(),
                code,
                extraFiles,
                true,
                false,
                null
        );

        // 解析编译结果的JSON对象
        JSONObject compileResult = (JSONObject) result.get(0);
        log.info("compileResult:{}", compileResult);
        // 检查编译结果的状态，如果不是"Accepted"，则抛出CompileError异常
        String status="status";
        if (compileResult.getInt(status).intValue() != Constants.Judge.STATUS_ACCEPTED.getStatus()) {
            throw new CompileException("Compile Error.", ((JSONObject) compileResult.get("files")).getStr("stdout"),
                    ((JSONObject) compileResult.get("files")).getStr("stderr"));
        }

        // 获取可执行文件的文件ID
        String fileId = ((JSONObject) compileResult.get("fileIds")).getStr(languageConfig.getExeName());

        // 如果文件ID为空，表示可执行文件未找到，抛出SubmitError异常
        if (ObjectUtils.isEmpty(fileId)) {
            throw new SubmitException("Executable file not found.", ((JSONObject) compileResult.get("files")).getStr("stdout"),
                    ((JSONObject) compileResult.get("files")).getStr("stderr"));
        }

        // 返回可执行文件的文件ID
        return fileId;
    }

    static List<String> parseCompileCommand(String command) {
        return JudgeUtils.translateCommandline(command);
    }
}
