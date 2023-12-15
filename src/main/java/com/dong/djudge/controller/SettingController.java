package com.dong.djudge.controller;

import com.dong.djudge.dto.SettingDTO;
import com.dong.djudge.service.SettingService;
import com.dong.djudge.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SettingController {
    @Autowired
    private SettingService settingService;

    @PostMapping("/setting")
    public ResponseResult<Object> setting(@RequestBody SettingDTO settingDTO) {
       return settingService.setting(settingDTO);
    }
}
