package com.dong.djudge.judge.task;

import cn.hutool.extra.spring.SpringUtil;
import com.dong.djudge.common.exception.CompileException;
import com.dong.djudge.common.exception.SubmitException;
import com.dong.djudge.common.exception.SystemException;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.judge.LanguageConfigLoader;
import com.dong.djudge.judge.entity.LanguageConfig;
import com.dong.djudge.judge.service.CompilerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;

/**
 * @author 阿东
 * @since 2023/9/7 [3:37]
 * 编译源码的具体实现，通过不同的参数来选择编译的方式
 */
@Slf4j(topic = "CompilerTask")
@Component
public class CompilerTask {

    /**
     * 语言配置加载器
     */
    private final LanguageConfigLoader languageConfigLoader = SpringUtil.getBean(LanguageConfigLoader.class);
    /**
     * 对沙盒编译服务的封装
     */
    private final CompilerService compilerService = SpringUtil.getBean(CompilerService.class);

    public String compilerTask(JudgeRequest request) throws CompileException {
        String testCasesDir;
        // 对用户源代码进行编译 获取tmpfs中的fileId
        LanguageConfig languageConfig = languageConfigLoader.getLanguageConfigByName(request.getLanguage());
        if (languageConfig == null) {
            log.error("Unsupported language {}", request.getLanguage());
            throw new CompileException("不受支持的语言: " + request.getLanguage(), "0", "0");
        }
        try {
            // 有的语言可能不支持编译, 目前有js、php不支持编译
            testCasesDir = compilerService.compile(languageConfig, request.getCode(), request.getLanguage(), null);
            System.out.println(testCasesDir);
        } catch (CompileException e) {
            log.error("编译错误:{}", e.getMessage());
            throw new CompileException("编译错误:" + e.getMessage(), e.getStdout(), e.getStderr());
        } catch (SystemException e) {
            log.error("系统错误:{}", e.getMessage());
            throw new CompileException("系统错误:" + e.getMessage(), e.getStdout(), e.getStderr());
        } catch (SubmitException e) {
            log.error("提交错误:{}", e.getMessage());
            throw new CompileException("提交错误:" + e.getMessage(), e.getStdout(), e.getStderr());
        } catch (Exception e) {
            log.error("未知异常:{}", e.getMessage());
            throw new CompileException("未知异常" + e.getMessage(), "0", "0");
        }
        return testCasesDir;
    }
}
