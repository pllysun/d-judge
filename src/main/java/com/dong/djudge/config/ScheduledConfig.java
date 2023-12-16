package com.dong.djudge.config;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dong.djudge.entity.setting.CpuAndMemoryEntity;
import com.dong.djudge.mapper.SandBoxSettingMapper;
import com.dong.djudge.mapper.SystemMessageMapper;
import com.dong.djudge.pojo.SandBoxSetting;
import com.dong.djudge.pojo.SystemMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@Slf4j
public class ScheduledConfig {
    @Autowired
    private SandBoxSettingMapper sandBoxSettingMapper;
    @Autowired
    private SystemMessageMapper systemMessageMapper;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 定期检查沙盒状态。
     * 每30秒执行一次，从数据库获取沙盒设置列表，并对每个沙盒执行状态检查和评分。
     */
    @Scheduled(fixedRate = 30000)
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
                    systemMessageMapper.insert(new SystemMessage(cpuAndMemoryEntity.getCpu(), cpuAndMemoryEntity.getMemory()));
                    grades = calculateGradesBasedOnValue(Double.parseDouble(cpuAndMemoryEntity.getCpu()), grades);
                    grades = calculateGradesBasedOnValue(Double.parseDouble(cpuAndMemoryEntity.getMemory()), grades);
                } else {
                    grades += 30;
                }

                // 根据计算的分数调整沙盒设置的级别和状态
                grades = adjustSandBoxSettingLevel(sandBoxSetting, grades);
                adjustSandBoxSettingState(sandBoxSetting);
                LambdaQueryWrapper<SandBoxSetting> lambda = new QueryWrapper<SandBoxSetting>().lambda();
                lambda.eq(SandBoxSetting::getId, sandBoxSetting.getId());
                sandBoxSettingMapper.update(sandBoxSetting, lambda);
                log.info("沙盒服务器地址：{}，分数：{}，级别：{}，状态：{}，频率：{}", sandBoxSetting.getBaseUrl(), grades, sandBoxSetting.getLevel(), sandBoxSetting.getState(), sandBoxSetting.getFrequency());
            });
        }
    }

    /**
     * 测量给定基础URL的响应时间。
     * @param baseUrl 基础URL。
     * @return 响应时间（毫秒）。
     */
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

    /**
     * 根据响应时间计算分数。
     * @param duration 响应时间。
     * @param sandBoxSetting 沙盒设置。
     * @param grades 当前分数。
     * @return 更新后的分数。
     */
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
            grades += 5;
        } else {
            grades += 6;
        }
        return grades;
    }

    /**
     * 从给定的基础URL获取CPU和内存信息。
     * @param baseUrl 基础URL。
     * @return CpuAndMemoryEntity对象，包含CPU和内存信息。
     */
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

    /**
     * 根据CPU或内存值计算分数。
     * @param value CPU或内存值。
     * @param grades 当前分数。
     * @return 更新后的分数。
     */
    private Integer calculateGradesBasedOnValue(double value, Integer grades) {
        int caseValue = (int) (value);
        if (caseValue < 10) {
            grades--;
        } else if (caseValue < 20) {
            grades += 1;
        } else if (caseValue < 30) {
            grades += 2;
        } else if (caseValue < 40) {
            grades += 3;
        } else if (caseValue < 50) {
            grades += 4;
        } else if (caseValue < 60) {
            grades += 6;
        } else if (caseValue < 70) {
            grades += 10;
        } else if (caseValue < 80) {
            grades += 15;
        } else {
            grades += 25;
        }
        return grades;
    }

    /**
     * 根据分数调整沙盒设置的级别。
     * @param sandBoxSetting 沙盒设置。
     * @param grades 分数。
     * @return 更新后的分数。
     */
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

    /**
     * 根据频率调整沙盒设置的状态。
     * @param sandBoxSetting 沙盒设置。
     */
    private void adjustSandBoxSettingState(SandBoxSetting sandBoxSetting) {
        Integer frequency = sandBoxSetting.getFrequency();
        // 如果频率在特定范围内，调整级别和状态
        if (frequency >= 10 && frequency <= 20) {
            sandBoxSetting.setLevel(0);
            sandBoxSetting.setState(2);
            sandBoxSetting.setFrequency(0);
            log.warn("沙盒服务器地址：{} 暂停服务", sandBoxSetting.getBaseUrl());
        } else if (frequency > 20) {
            sandBoxSetting.setLevel(-1);
            sandBoxSetting.setState(0);
            sandBoxSetting.setFrequency(0);
            log.warn("沙盒服务器地址：{} 停止服务", sandBoxSetting.getBaseUrl());
        }
    }

    /**
     * 定期清理CPU和内存消息。
     * 每天午夜执行，删除创建时间早于当前时间30天的SystemMessage记录。
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void clearCpuAndMemoryMessage() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        LambdaQueryWrapper<SystemMessage> lambda = new QueryWrapper<SystemMessage>().lambda();
        lambda.lt(SystemMessage::getCreateTime, thirtyDaysAgo);
        systemMessageMapper.delete(lambda);
    }



}
