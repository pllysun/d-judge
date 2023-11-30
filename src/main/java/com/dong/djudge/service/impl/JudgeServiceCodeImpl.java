package com.dong.djudge.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.entity.FileEntity;
import com.dong.djudge.exception.CompileException;
import com.dong.djudge.exception.SystemException;
import com.dong.djudge.mapper.FileMapper;
import com.dong.djudge.service.JudgeService;
import com.dong.djudge.util.ResponseResult;
import org.springframework.stereotype.Service;

/**
 * @author 樊东升
 * @date 2023/11/30 21:42
 */
@Service("JudgeServiceCodeImpl")
public class JudgeServiceCodeImpl extends ServiceImpl<FileMapper, FileEntity> implements JudgeService {
    @Override
    public ResponseResult<Object> Judge(JudgeRequest request) throws SystemException, CompileException {
        return null;
    }
}
