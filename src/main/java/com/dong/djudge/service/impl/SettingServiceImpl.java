package com.dong.djudge.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dong.djudge.dto.SettingDTO;
import com.dong.djudge.enums.SettingEnum;
import com.dong.djudge.mapper.SettingMapper;
import com.dong.djudge.pojo.Setting;
import com.dong.djudge.service.SettingService;
import com.dong.djudge.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SettingServiceImpl extends ServiceImpl<SettingMapper, Setting> implements SettingService {

    @Autowired
    private SettingMapper settingMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public ResponseResult<Object> setting(SettingDTO settingDTO) {
        SettingEnum setting = SettingEnum.getByKey(settingDTO.getKey());
        Integer code = setting.getCode();
        settingDTO.setKey(setting.getKey().toLowerCase());
        switch (code) {
            case 1:
                checkServerUrl(settingDTO.getValue());
                LambdaQueryWrapper<Setting> lambda = new QueryWrapper<Setting>().lambda();
                lambda.eq(Setting::getKey, settingDTO.getKey());
                Setting one = getOne(lambda);
                Set<String> urlList = new HashSet<>();
                if (one == null) {
                    urlList.add(settingDTO.getValue());
                    String jsonString = JSON.toJSONString(urlList);
                    Setting st = new Setting(settingDTO.getKey(), jsonString);
                    save(st);
                } else {
                    String value = one.getValue();
                    urlList.addAll(JSON.parseArray(value, String.class));
                    urlList.add(settingDTO.getValue());
                    String jsonString = JSON.toJSONString(urlList);
                    one.setValue(jsonString);
                    updateById(one);
                }
                break;
            case 2:
                break;
            case 3:
                break;
            default:
                break;
        }
        return ResponseResult.ok("设置成功");
    }

    private void checkServerUrl(String url) {
        ResponseEntity<String> entity;
        url = url + "/version";
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url; // 如果没有前缀，则添加 "http://"
        }
        try {
            entity = restTemplate.getForEntity(url, String.class);

        } catch (Exception e) {
            throw new RuntimeException("沙盒服务器地址: " + url + " 不可用");
        }
        if (entity.getStatusCode().is2xxSuccessful()) {
            ResponseResult.ok("true");
        } else {
            throw new RuntimeException("沙盒服务器地址: " + url + " 不可用");
        }
    }


}
