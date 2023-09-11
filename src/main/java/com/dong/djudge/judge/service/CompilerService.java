package com.dong.djudge.judge.service;

import com.dong.djudge.common.exception.CompileException;
import com.dong.djudge.common.exception.SubmitException;
import com.dong.djudge.common.exception.SystemException;
import com.dong.djudge.judge.entity.LanguageConfig;

import java.util.HashMap;

/**
 * @author 阿东
 * @since 2023/9/10 [15:22]
 */
public interface CompilerService {
    /**
     * 编译服务
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
    String compile(LanguageConfig languageConfig,
                   String code,
                   String language,
                   HashMap<String, String> extraFiles
    ) throws SystemException, CompileException, SubmitException;
}
