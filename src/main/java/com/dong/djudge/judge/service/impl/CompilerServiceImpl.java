package com.dong.djudge.judge.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.dong.djudge.exception.CompileException;
import com.dong.djudge.exception.SubmitException;
import com.dong.djudge.exception.SystemException;
import com.dong.djudge.judge.SandboxRun;
import com.dong.djudge.judge.entity.LanguageConfig;
import com.dong.djudge.judge.service.CompilerService;
import com.dong.djudge.mapper.SandBoxRunMapper;
import com.dong.djudge.mapper.SandBoxSettingMapper;
import com.dong.djudge.pojo.SandBoxRun;
import com.dong.djudge.pojo.SandBoxSetting;
import com.dong.djudge.util.Constants;
import com.dong.djudge.util.JudgeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * @author 阿东
 * @since 2023/9/10 [15:23]
 */
@Service
@Slf4j(topic = "doj")
public class CompilerServiceImpl implements CompilerService {
    /**
     * 最大时间和最大内存-单位:ms 10s
     */
    Long maxTime = 10000L;
    /**
     * 最大内存 100M
     */
    Long maxMemory = 104857600L;
    @Autowired
    private SandBoxSettingMapper sandBoxSettingMapper;
    @Autowired
    private SandBoxRunMapper sandBoxRunMapper;

    static List<String> parseCompileCommand(String command) {
        return JudgeUtils.translateCommandline(command);
    }

    /**
     * 编译
     * 这个方法是编译的核心方法，只涉及到编译，不涉及运行，将源代码编译为可执行文件。
     *
     * @param languageConfig 语言配置
     * @param code           代码
     * @param language       语言
     * @param extraFiles     额外文件
     * @return 文件id
     * @throws SystemException  系统错误
     * @throws CompileException 编译错误
     * @throws SubmitException  提交错误
     */
    @Override
    public String compile(LanguageConfig languageConfig, String code, String language, HashMap<String, String> extraFiles) throws SystemException, CompileException, SubmitException {
        // 检查是否支持指定的编程语言，如果languageConfig为null，则抛出RuntimeException
        if (languageConfig == null) {
            throw new RuntimeException("Unsupported language " + language);
        }
        SandBoxSetting sandboxBaseUrl = getSandboxBaseUrl();
        // 调用安全沙箱的compile方法进行编译
        JSONArray result = SandboxRun.compile(
                sandboxBaseUrl.getBaseUrl(),
                languageConfig.getMaxCpuTime(),
                languageConfig.getMaxRealTime() == null ? maxTime : languageConfig.getMaxRealTime(),
                languageConfig.getMaxMemory() == null ? maxMemory : languageConfig.getMaxMemory(),
                256 * 1024 * 1024L,
                languageConfig.getSrcName(),
                languageConfig.getExeName(),
                parseCompileCommand(languageConfig.getCompileCommand()),
                languageConfig.getCompileEnvs(),
                code,
                extraFiles,
                true,
                false,
                null
        );

        // 解析编译结果的JSON对象
        JSONObject compileResult = (JSONObject) result.get(0);
        log.info("compileResult:{}", compileResult);
        // 检查编译结果的状态，如果不是"Accepted"，则抛出CompileError异常
        String status = "status";
        if (compileResult.getInt(status).intValue() != Constants.Judge.STATUS_ACCEPTED.getStatus()) {
            throw new CompileException("Compile Error.", ((JSONObject) compileResult.get("files")).getStr("stdout"),
                    ((JSONObject) compileResult.get("files")).getStr("stderr"));
        }

        // 获取可执行文件的文件ID
        String fileId = ((JSONObject) compileResult.get("fileIds")).getStr(languageConfig.getExeName());

        // 如果文件ID为空，表示可执行文件未找到，抛出SubmitError异常
        if (ObjectUtils.isEmpty(fileId)) {
            throw new SubmitException("Executable file not found.", ((JSONObject) compileResult.get("files")).getStr("stdout"),
                    ((JSONObject) compileResult.get("files")).getStr("stderr"));
        }
        sandBoxRunMapper.insert(new SandBoxRun(fileId, sandboxBaseUrl.getBaseUrl()));
        // 返回可执行文件的文件ID
        return fileId;
    }

    public SandBoxSetting getSandboxBaseUrl() throws SystemException {
        List<SandBoxSetting> sandBoxSettings = sandBoxSettingMapper.selectList(null);
        if (sandBoxSettings.isEmpty()) {
            throw new SystemException("没有可用代码沙盒服务", "", "");
        }
        Map<Long, String> url = new HashMap<>();
        List<SandBoxSetting> list = new ArrayList<>();
        for (SandBoxSetting sandBoxSetting : sandBoxSettings) {
            if (sandBoxSetting.getState() == 1) {
                url.put(sandBoxSetting.getId(), sandBoxSetting.getBaseUrl());
                list.add(sandBoxSetting);
            }
        }
        int sum = 0;
        int level = 0;
        Map<Long, Set<Integer>> map = new HashMap<>();
        for (SandBoxSetting sandBoxSetting : list) {
            Long id = sandBoxSetting.getId();
            Set<Integer> set = new HashSet<>();
            for (int i = 0; i < sandBoxSetting.getLevel(); i++) {
                set.add(level);
                level++;
            }
            map.put(id, set);
            sum += sandBoxSetting.getLevel();
        }
        Random random = new Random();
        int i = random.nextInt(sum);
        for (Map.Entry<Long, Set<Integer>> entry : map.entrySet()) {
            if (entry.getValue().contains(i)) {
                SandBoxSetting sandBoxSetting = new SandBoxSetting();
                sandBoxSetting.setId(entry.getKey());
                sandBoxSetting.setBaseUrl(url.get(entry.getKey()));
                return sandBoxSetting;
            }
        }
        throw new SystemException("分配服务器出错", "随机到的数为:" + i + ",最大值为:" + sum, "");
    }
}
