package com.dong.djudge.service;

import com.dong.djudge.dto.StaredCodeDTO;
import com.dong.djudge.util.ResponseResult;

/**
 * @author 樊东升
 * @date 2023/11/30 21:39
 */
public interface StandardCodeService {
    ResponseResult<String> standardCodeRun(StaredCodeDTO staredCodeDTO) throws Exception;
}
