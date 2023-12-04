package com.dong.djudge.service;

import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.exception.CompileException;
import com.dong.djudge.exception.SystemException;

/**
 * @author 樊东升
 * @date 2023/11/27 21:35
 */

public interface CompileService {
    String compile(JudgeRequest judgeRequest) throws SystemException, CompileException;
}
