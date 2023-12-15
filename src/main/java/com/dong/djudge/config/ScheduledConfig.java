package com.dong.djudge.config;

import com.alibaba.fastjson2.JSON;
import com.dong.djudge.entity.setting.CpuAndMemoryEntity;
import com.dong.djudge.mapper.SandBoxSettingMapper;
import com.dong.djudge.pojo.SandBoxSetting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@Slf4j
public class ScheduledConfig {
    @Autowired
    private SandBoxSettingMapper sandBoxSettingMapper;
    @Autowired
    private RestTemplate restTemplate;

    @Scheduled(fixedRate = 10000)
    public void checkSandboxState() {
        // 从数据库获取沙盒设置列表
        List<SandBoxSetting> sandBoxSettings = sandBoxSettingMapper.selectList(null);
        for (SandBoxSetting sandBoxSetting : sandBoxSettings) {

            Thread.startVirtualThread(() -> {

                // 如果沙盒设置级别为-1，则跳过当前迭代
                if (sandBoxSetting.getLevel() == -1) {
                    return;
                }
                int grades = 0; // 初始化分数为0

                // 测量响应时间，并根据响应时间计算分数
                long duration = measureResponseTime(sandBoxSetting.getBaseUrl());
                grades = calculateGradesBasedOnDuration(duration, sandBoxSetting, grades);

                // 获取CPU和内存信息，并计算分数
                CpuAndMemoryEntity cpuAndMemoryEntity = fetchCpuAndMemoryEntity(sandBoxSetting.getBaseUrl());
                if (cpuAndMemoryEntity != null) {
                    log.info("url:{}, cpu:{},memory:{}", sandBoxSetting.getBaseUrl(), cpuAndMemoryEntity.getCpu(), cpuAndMemoryEntity.getMemory());
                    grades = calculateGradesBasedOnValue(Double.parseDouble(cpuAndMemoryEntity.getCpu()), grades);
                    grades = calculateGradesBasedOnValue(Double.parseDouble(cpuAndMemoryEntity.getMemory()), grades);
                } else {
                    grades += 30;
                }

                // 根据计算的分数调整沙盒设置的级别和状态
                grades = adjustSandBoxSettingLevel(sandBoxSetting, grades);
                adjustSandBoxSettingState(sandBoxSetting);
                log.info("沙盒服务器地址：{}，分数：{}，级别：{}，状态：{}，频率：{}", sandBoxSetting.getBaseUrl(), grades, sandBoxSetting.getLevel(), sandBoxSetting.getState(),sandBoxSetting.getFrequency());
            });

        }
    }

    private long measureResponseTime(String baseUrl) {
        long startTime = System.currentTimeMillis();
        try {
            // 发送HTTP请求并获取响应
            ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/checkInfo", String.class);
        } catch (Exception e) {
            // 异常处理，可以记录日志或者返回一个默认值
            return Long.MAX_VALUE; // 示例中返回一个很大的数表示请求失败
        }
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private Integer calculateGradesBasedOnDuration(long duration, SandBoxSetting sandBoxSetting, int grades) {
        if (duration <= 1000) {
            grades--;
        } else if (duration <= 3000) {
            grades += 2;
        } else if (duration <= 5000) {
            grades += 3;
        } else if (duration <= 10000) {
            grades += 4;
        } else if (duration <= 30000) {
            sandBoxSetting.setFrequency(sandBoxSetting.getFrequency() + 1);
            grades += 5;
        } else {
            sandBoxSetting.setFrequency(sandBoxSetting.getFrequency() + 2);
            grades += 6;
        }
        return grades;
    }

    private CpuAndMemoryEntity fetchCpuAndMemoryEntity(String baseUrl) {
        try {
            // 发送HTTP请求并获取响应
            ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/checkInfo", String.class);
            return JSON.parseObject(response.getBody(), CpuAndMemoryEntity.class);
        } catch (Exception e) {
            // 异常处理，可以记录日志或者返回null
            return null;
        }
    }

    private Integer calculateGradesBasedOnValue(double value, Integer grades) {
        int caseValue = (int) (value * 10);
        switch (caseValue) {
            case 0:
                grades--;
                break;
            case 1:
                grades += 2;
                break;
            case 2:
                grades += 3;
                break;
            case 3:
                grades += 4;
                break;
            case 4:
                grades += 5;
                break;
            case 5:
                grades += 6;
                break;
            case 6:
                grades += 9;
                break;
            case 7:
                grades += 15;
                break;
            case 8:
                grades += 20;
                break;
            default:
                grades += 30;
                break;
        }
        return grades;
    }

    private Integer adjustSandBoxSettingLevel(SandBoxSetting sandBoxSetting, Integer grades) {
        Integer level = sandBoxSetting.getLevel();
        if (grades <= 5) {
            sandBoxSetting.setLevel(level + 1);
        } else if (grades > 10 && grades <= 15) {
            sandBoxSetting.setLevel(level - 1);
        } else if (grades >= 15 && grades <= 20) {
            sandBoxSetting.setLevel(level - 2);
        } else if (grades > 20 && grades <= 25) {
            sandBoxSetting.setLevel(level - 3);
            sandBoxSetting.setFrequency(sandBoxSetting.getFrequency() + 1);
        } else if (grades > 25 && grades <= 30) {
            sandBoxSetting.setLevel(level - 4);
            sandBoxSetting.setFrequency(sandBoxSetting.getFrequency() + 2);
        } else if (grades > 30) {
            sandBoxSetting.setLevel(level - 5);
            sandBoxSetting.setFrequency(sandBoxSetting.getFrequency() + 3);
        }

        // 确保级别在1到8之间
        if (sandBoxSetting.getLevel() < 1) {
            sandBoxSetting.setLevel(1);
        }
        if (sandBoxSetting.getLevel() > 8) {
            sandBoxSetting.setLevel(8);
        }
        return grades;
    }

    private void adjustSandBoxSettingState(SandBoxSetting sandBoxSetting) {
        Integer frequency = sandBoxSetting.getFrequency();

        // 如果频率在特定范围内，调整级别和状态
        if (frequency > 5 && frequency < 10) {
            sandBoxSetting.setLevel(0);
            sandBoxSetting.setState(0);
            sandBoxSetting.setFrequency(0);
        } else if (frequency >= 10) {
            sandBoxSetting.setLevel(-1);
            sandBoxSetting.setState(0);
            sandBoxSetting.setFrequency(0);
        }
        sandBoxSetting.setFrequency(sandBoxSetting.getFrequency() + 1);
    }


}
