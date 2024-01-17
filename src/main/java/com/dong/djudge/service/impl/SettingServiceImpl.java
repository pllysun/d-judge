package com.dong.djudge.service.impl;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dong.djudge.dto.SandBoxSettingDTO;
import com.dong.djudge.entity.setting.SystemConfig;
import com.dong.djudge.mapper.*;
import com.dong.djudge.pojo.*;
import com.dong.djudge.service.SettingService;
import com.dong.djudge.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SettingServiceImpl implements SettingService {


    @Autowired
    private SandBoxSettingMapper sandBoxSettingMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private SystemMessageMapper systemMessageMapper;

    @Autowired
    private LanguageDictionaryMapper languageDictionaryMapper;

    @Autowired
    private LanguageConfigMapper languageConfigMapper;

    @Autowired
    private LanguageInstallMapper languageInstallMapper;



    /**
     * 该方法用于为沙盒环境设置服务器URL。
     *
     * @param sandBoxSettingDTO 包含沙盒设置的数据传输对象。
     * @return ResponseResult<Object> 设置服务器URL操作的结果。
     */
    @Override
    public ResponseResult<Object> postServerUrl(SandBoxSettingDTO sandBoxSettingDTO) {
        if(sandBoxSettingDTO.getUrl()==null||sandBoxSettingDTO.getUrl().isEmpty()){
            return ResponseResult.failResponse("URL不能为空");
        }
        // 验证并修改DTO中的URL。
        String url;
        try {
            url = checkServerUrl(sandBoxSettingDTO.getUrl());
        } catch (RuntimeException e) {
            return ResponseResult.failResponse(e.getMessage());
        }

        log.info("url: {}", url);
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
        if (sandBoxSettingDTO.getName() != null && !sandBoxSettingDTO.getName().isEmpty()) {
            sandBoxSetting.setName(sandBoxSettingDTO.getName());
        } else {
            int size = sandBoxSettings.size();

            sandBoxSetting.setName(size + "号服务器");
        }
        // 将新沙盒设置插入数据库。
        sandBoxSettingMapper.insert(sandBoxSetting);

        // 返回带有设置URL的成功响应。
        return ResponseResult.ok("URL: " + url + " 设置成功");
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
            entity = restTemplate.getForEntity(url + "/version", String.class);

        } catch (Exception e) {
            // 如果URL无法访问，则抛出运行时异常。
            log.error("沙盒服务器地址: {} 不可用", url);
            log.error("错误信息: {}", e.getMessage());
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
        for (SandBoxSetting sandBoxSetting : sandBoxSettings) {
            sandBoxSetting.setKeyId(sandBoxSetting.getId().toString());
        }
        return ResponseResult.ok(sandBoxSettings);
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
        int delete = sandBoxSettingMapper.delete(lambda);
        return ResponseResult.ok(delete);
    }

    @Override
    public ResponseResult<Object> onlineServer(String sid) {
        LambdaQueryWrapper<SandBoxSetting> lambda = new QueryWrapper<SandBoxSetting>().lambda();
        lambda.eq(SandBoxSetting::getId, sid);
        SandBoxSetting sandBoxSetting = sandBoxSettingMapper.selectOne(lambda);
        if (sandBoxSetting != null) {
            checkServerUrl(sandBoxSetting.getBaseUrl());
            sandBoxSetting.setState(1);
            sandBoxSettingMapper.updateById(sandBoxSetting);
            return ResponseResult.ok("上线成功");

        }
        return ResponseResult.failResponse(sid + "服务不存在");
    }

    @Override
    public ResponseResult<Object> offlineServer(String sid) {
        LambdaQueryWrapper<SandBoxSetting> lambda = new QueryWrapper<SandBoxSetting>().lambda();
        lambda.eq(SandBoxSetting::getId, sid);
        SandBoxSetting sandBoxSetting = sandBoxSettingMapper.selectOne(lambda);
        if (sandBoxSetting != null) {
            sandBoxSetting.setState(0);
            sandBoxSettingMapper.updateById(sandBoxSetting);
            return ResponseResult.ok("下线成功");
        }
        return ResponseResult.failResponse(sid + "服务不存在");
    }

    @Override
    public ResponseResult<Object> systemInfo(String sid) {
        List<SystemMetricsPojo> smList;
        if (sid == null || sid.isEmpty()) {
            return ResponseResult.failResponse("sid不能为空");
        } else {

            DateTime now = DateTime.now();
            DateTime elevenHoursAgo = now.offset(DateField.HOUR, -11);
            String time = elevenHoursAgo.toString("yyyy-MM-dd HH:mm:ss");
            smList = systemMessageMapper.selectMetricsAfterDate(time, sid);
        }

        // 设置时区为中国时区 UTC+8
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));

        Instant now = Instant.now();
        Instant threeHoursAgo = now.minus(3, ChronoUnit.HOURS);

        List<SystemMetricsPojo> sortedAndFilteredSmList = smList.stream()
                .sorted(Comparator.comparing(SystemMetricsPojo::getCreateTime))
                .peek(sm -> {
                    // 格式化 createTime 和 updateTime
                    sm.setCreateTimeFormatted(formatter.format(sm.getCreateTime()));
                    sm.setUpdateTimeFormatted(formatter.format(sm.getUpdateTime()));
                })
                .filter(sm -> sm.getCreateTime().toInstant().isAfter(threeHoursAgo))
                .collect(Collectors.toList());


        return ResponseResult.ok(sortedAndFilteredSmList);
    }

    @Override
    public ResponseResult<Object> serverInfo() {
        List<SandBoxSetting> sandBoxSettings = sandBoxSettingMapper.selectList(null);
        for (SandBoxSetting sandBoxSetting : sandBoxSettings) {
            sandBoxSetting.setKeyId(sandBoxSetting.getId().toString());
        }
        return ResponseResult.ok(sandBoxSettings);
    }

    @Override
    public ResponseResult<Object> editServerName(String sid, String name) {
        if (name == null || name.isEmpty()) {
            return ResponseResult.failResponse("服务器名为空");
        }
        if (sid == null || sid.isEmpty()) {
            return ResponseResult.failResponse("服务器ID为空");
        }
        if (name.length() > 12) {
            return ResponseResult.failResponse("服务器名过长,最长为12个字符");
        }
        LambdaQueryWrapper<SandBoxSetting> lambda = new QueryWrapper<SandBoxSetting>().lambda();
        lambda.eq(SandBoxSetting::getId, sid);
        SandBoxSetting sandBoxSetting = sandBoxSettingMapper.selectOne(lambda);
        if (sandBoxSetting != null) {
            sandBoxSetting.setName(name);
            sandBoxSettingMapper.updateById(sandBoxSetting);
            return ResponseResult.ok("修改成功");
        } else {
            return ResponseResult.failResponse("服务器不存在");
        }
    }

    @Override
    public ResponseResult<Object> systemConfigInfo(String sid) {
        LambdaQueryWrapper<SandBoxSetting> lambda = new QueryWrapper<SandBoxSetting>().lambda();
        lambda.eq(SandBoxSetting::getId, sid);
        SandBoxSetting sandBoxSetting = sandBoxSettingMapper.selectOne(lambda);
        SystemConfig systemConfig = new SystemConfig();
        systemConfig.setName(sandBoxSetting.getName());
        systemConfig.setBaseUrl(sandBoxSetting.getBaseUrl());
        systemConfig.setFrequency(sandBoxSetting.getFrequency());
        systemConfig.setLevel(sandBoxSetting.getLevel());
        systemConfig.setState(sandBoxSetting.getState());
        LambdaQueryWrapper<LanguageConfig> l2 = new QueryWrapper<LanguageConfig>().lambda();
        LambdaQueryWrapper<LanguageConfig> eq = l2.eq(LanguageConfig::getServerId, sid);
        List<LanguageConfig> languageConfigs = languageConfigMapper.selectList(eq);
        List<LanguageInstall> languageInstalls = languageInstallMapper.selectList(null);
        Map<Long,String> map=new HashMap<>();
        for (LanguageInstall languageInstall : languageInstalls) {
            map.put(languageInstall.getId(),languageInstall.getLanguage());
        }
        List<String> list=new ArrayList<>();
        for (LanguageConfig languageConfig : languageConfigs) {
            list.add(map.get(Long.parseLong(languageConfig.getLanguageId())));
        }
        systemConfig.setLanguageLabelList(list);
        return ResponseResult.ok(systemConfig);
    }




    @Override
    public ResponseResult<Object> getLanguageDictionary() {
        List<LanguageDictionary> languageDictionaries = languageDictionaryMapper.selectList(null);
        return ResponseResult.ok(languageDictionaries);
    }

    @Override
    public ResponseResult<Object> getLanguageInstallConfig() {
        List<LanguageInstall> languageInstalls = languageInstallMapper.selectList(null);
        return ResponseResult.ok(languageInstalls);
    }
}
