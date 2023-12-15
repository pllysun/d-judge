package com.dong.djudge.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dong.djudge.dto.SandBoxSettingDTO;
import com.dong.djudge.mapper.SandBoxSettingMapper;
import com.dong.djudge.mapper.SettingMapper;
import com.dong.djudge.pojo.SandBoxSetting;
import com.dong.djudge.pojo.Setting;
import com.dong.djudge.service.SettingService;
import com.dong.djudge.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SettingServiceImpl implements SettingService {

    @Autowired
    private SettingMapper settingMapper;

    @Autowired
    private SandBoxSettingMapper sandBoxSettingMapper;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 该方法用于为沙盒环境设置服务器URL。
     *
     * @param sandBoxSettingDTO 包含沙盒设置的数据传输对象。
     * @return ResponseResult<Object> 设置服务器URL操作的结果。
     */
    @Override
    public ResponseResult<Object> postServerUrl(SandBoxSettingDTO sandBoxSettingDTO) {
        // 验证并修改DTO中的URL。
        String url = checkServerUrl(sandBoxSettingDTO.getUrl());

        // 检索现有的沙盒设置列表。
        List<SandBoxSetting> sandBoxSettings = sandBoxSettingMapper.selectList(null);

        // 检查沙盒设置列表是否不为空。
        if (!sandBoxSettings.isEmpty()) {
            Set<String> set = new HashSet<>();
            // 遍历每个设置，将基本URL添加到集合中。
            for (SandBoxSetting sandBoxSetting : sandBoxSettings) {
                set.add(sandBoxSetting.getBaseUrl());
            }
            // 检查URL是否已在集合中存在。
            if (set.contains(url)) {
                // 如果URL已存在，则返回失败响应。
                return ResponseResult.failResponse("沙盒服务器地址已存在");
            }
        }
        // 使用给定URL创建新的沙盒设置。
        SandBoxSetting sandBoxSetting = new SandBoxSetting(url);

        // 将新沙盒设置插入数据库。
        sandBoxSettingMapper.insert(sandBoxSetting);

        // 返回带有设置URL的成功响应。
        return ResponseResult.ok("URL: "+url+" 设置成功");
    }

    /**
     * 验证沙盒服务器URL。
     *
     * @param url 要验证URL。
     * @return String 经过验证且可能被修改的URL。
     */
    private String checkServerUrl(String url) {
        ResponseEntity<String> entity;
        // 如果URL不是以"http://"或"https://"开头，则添加"http://"前缀。
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        try {
            // 尝试对URL进行GET请求。
            entity = restTemplate.getForEntity(url+"/version", String.class);

        } catch (Exception e) {
            // 如果URL无法访问，则抛出运行时异常。
            throw new RuntimeException("沙盒服务器地址: " + url + " 不可用");
        }
        // 检查响应状态码是否为成功（2xx）。
        if (entity.getStatusCode().is2xxSuccessful()) {
            ResponseResult.ok("true");
        } else {
            // 如果响应状态码不是成功，则抛出运行时异常。
            throw new RuntimeException("沙盒服务器地址: " + url + " 不可用");
        }
        // 返回经过验证的URL。
        return url;
    }




    @Override
    public ResponseResult<Object> getServerUrl() {
        List<SandBoxSetting> sandBoxSettings = sandBoxSettingMapper.selectList(null);
        return  ResponseResult.ok(sandBoxSettings);
    }

    @Override
    public ResponseResult<Object> deleteServerUrl(String sid) {
        long l = 0;
        LambdaQueryWrapper<SandBoxSetting> lambda;
        try {
            lambda = new QueryWrapper<SandBoxSetting>().lambda();
            l = Long.parseLong(sid);
            lambda.eq(SandBoxSetting::getId, l);
        } catch (Exception e) {
            return ResponseResult.failResponse("sid格式错误-" + l);
        }
        return ResponseResult.ok(sandBoxSettingMapper.delete(lambda));
    }
}
