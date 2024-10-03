package com.dong.djudge.judge.task;

import cn.hutool.extra.spring.SpringUtil;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.exception.CompileException;
import com.dong.djudge.exception.SubmitException;
import com.dong.djudge.exception.SystemException;
import com.dong.djudge.judge.LanguageConfigLoader;
import com.dong.djudge.judge.entity.LanguageConfig;
import com.dong.djudge.judge.service.CompilerService;
import com.dong.djudge.util.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

    /**
     * @param request 评判请求
     * @return 返回测试用例目录
     * @throws CompileException 编译异常
     */
    public String compilerTask(JudgeRequest request) throws CompileException {
        String testCasesDir; // 测试用例目录
        String language = CommonUtils.getLanguage(request.getLanguage());
        // 对用户源代码进行编译 获取tmpfs中的fileId
        LanguageConfig languageConfig = languageConfigLoader.getLanguageConfigByName(language);
        // 如果语言配置为空，即不支持的语言，抛出编译异常
        if (languageConfig == null) {
            log.error("Unsupported language {}", request.getLanguage());
            throw new CompileException("不受支持的语言: " + request.getLanguage(), "0", "0");
        }
        try {
            // 有的语言可能不支持编译, 目前有js、php不支持编译
            // 调用编译服务进行编译
            testCasesDir = compilerService.compile(languageConfig, request.getCode(), request.getLanguage(), null);
            System.out.println(testCasesDir);
        } catch (CompileException e) {
            // 捕获编译异常，记录日志并重新抛出
            log.warn("编译错误:{}", e.getStderr());
            throw new CompileException("编译错误:" + e.getMessage(), e.getStdout(), e.getStderr());
        } catch (SystemException e) {
            // 捕获系统异常，记录日志并重新抛出
            log.warn("系统错误:{}", e.getMessage());
            throw new CompileException("系统错误:" + e.getMessage(), e.getStdout(), e.getStderr());
        } catch (SubmitException e) {
            // 捕获提交异常，记录日志并重新抛出
            log.warn("提交错误:{}", e.getMessage());
            throw new CompileException("提交错误:" + e.getMessage(), e.getStdout(), e.getStderr());
        } catch (Exception e) {
            // 捕获未知异常，记录日志并重新抛出
            log.warn("未知异常:{}", e.getMessage());
            throw new CompileException("未知异常" + e.getMessage(), "0", "0");
        }
        // 返回测试用例目录
        return testCasesDir;
    }




}
