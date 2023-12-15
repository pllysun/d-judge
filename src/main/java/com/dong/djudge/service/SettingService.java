package com.dong.djudge.service;

import com.dong.djudge.dto.SettingDTO;
import com.dong.djudge.util.ResponseResult;

public interface SettingService {

    ResponseResult<Object> setting(SettingDTO settingDTO);
}
