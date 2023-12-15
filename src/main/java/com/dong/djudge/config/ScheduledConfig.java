package com.dong.djudge.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dong.djudge.mapper.SandBoxRunMapper;
import com.dong.djudge.mapper.SandBoxSettingMapper;
import com.dong.djudge.pojo.SandBoxSetting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@Slf4j
public class ScheduledConfig {
    @Autowired
    private SandBoxRunMapper sandBoxRunMapper;
    @Autowired
    private SandBoxSettingMapper sandBoxSettingMapper;
    @Autowired
    private  RestTemplate restTemplate;

    static List<String> sandBoxUrlList = new ArrayList<>();
    static Map<String,Boolean> sandBoxStateMap=new HashMap<>();

    static String url = "http://8.219.11.202:5051/version";
    static {
        sandBoxUrlList.add(url);
        sandBoxStateMap.put(url,true);
    }
    @Scheduled(fixedRate = 60000)
    public void checkSandboxState() {
        if(sandBoxUrlList==null|| sandBoxUrlList.isEmpty()){
            List<SandBoxSetting> sandBoxSettings = sandBoxSettingMapper.selectList(null);
            for (SandBoxSetting sandBoxSetting : sandBoxSettings) {
                sandBoxUrlList.add(sandBoxSetting.getBaseUrl());
            }
        }
        for (String sandBoxUrl : sandBoxUrlList) {
            try {
                System.out.println(sandBoxStateMap.get(url));
                long startTime = System.currentTimeMillis();
                ResponseEntity<String> response = restTemplate.getForEntity(sandBoxUrl, String.class);
                long endTime = System.currentTimeMillis();
                long duration = endTime - startTime;
                log.info("请求耗时：{}ms", duration);
                byte[] responseBytes = Objects.requireNonNull(response.getBody()).getBytes(StandardCharsets.UTF_8);
                log.info(String.valueOf(responseBytes.length));
                LambdaQueryWrapper<SandBoxSetting> lambda = new QueryWrapper<SandBoxSetting>().lambda();
                SandBoxSetting sandBoxSetting = new SandBoxSetting();
                if (!response.getStatusCode().is2xxSuccessful()) {
                    updateState(lambda, sandBoxUrl, sandBoxSetting, 0);
                }else{
                    if(!sandBoxStateMap.get(sandBoxUrl)){
                        updateState(lambda, sandBoxUrl, sandBoxSetting, 1);
                    }
                }
                log.info(response.getStatusCode().toString());
            } catch (Exception e) {
                updateState(new QueryWrapper<SandBoxSetting>().lambda(), sandBoxUrl, new SandBoxSetting(), 0);
                log.error("网络请求错误:{}", sandBoxUrl);
            }
        }

    }

    private void updateState(LambdaQueryWrapper<SandBoxSetting> lambda, String sandBoxUrl, SandBoxSetting sandBoxSetting, int state) {
        lambda.eq(SandBoxSetting::getBaseUrl, sandBoxUrl);
        sandBoxSetting.setState(state);
        sandBoxSettingMapper.update(sandBoxSetting, lambda);
        sandBoxStateMap.put(sandBoxUrl,false);
    }
}
