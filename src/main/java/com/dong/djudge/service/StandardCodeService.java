package com.dong.djudge.service;

import com.dong.djudge.dto.UpdatestandardCodeDTO;
import com.dong.djudge.dto.standardCodeDTO;
import com.dong.djudge.util.ResponseResult;

/**
 * @author 樊东升
 * @date 2023/11/30 21:39
 */
public interface StandardCodeService {
    ResponseResult<String> standardCodeRun(standardCodeDTO standardCodeDTO) throws Exception;

    ResponseResult<String> standardCode(UpdatestandardCodeDTO standardCodeDTO) throws Exception;

    String getStandardCode(String standardCodeId) throws Exception;

    ResponseResult<String> deleteStandardCode(String standardCodeId);

}
