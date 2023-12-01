package com.dong.djudge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dong.djudge.exception.CompileException;
import com.dong.djudge.exception.SystemException;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.entity.TestGroupEntity;
import com.dong.djudge.util.ResponseResult;

/**
 * @author 阿东
 * @date 2023/9/6 [0:23]
 */
public interface JudgeService extends IService<TestGroupEntity> {

    /**
     * 判题接口通用实现类
     * @param request 请求参数
     * @return 返回判题结果Json
     * @throws SystemException 系统错误
     * @throws CompileException 编译错误
     */
    ResponseResult<Object> Judge(JudgeRequest request) throws Exception;
}
