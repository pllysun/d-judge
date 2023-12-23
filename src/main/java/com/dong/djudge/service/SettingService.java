package com.dong.djudge.service;

import com.dong.djudge.dto.SandBoxSettingDTO;
import com.dong.djudge.util.ResponseResult;

public interface SettingService {

    ResponseResult<Object> postServerUrl(SandBoxSettingDTO sandBoxSettingDTO);


    ResponseResult<Object> getServerUrl();

    ResponseResult<Object> deleteServerUrl(String sid);

    ResponseResult<Object> onlineServer(String sid);

    ResponseResult<Object> offlineServer(String sid);


    ResponseResult<Object> systemInfo(String sid);


    ResponseResult<Object> serverInfo();

    ResponseResult<Object> editServerName(String sid, String name);

    ResponseResult<Object> systemConfigInfo(String sid);
}
