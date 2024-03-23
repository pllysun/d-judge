package com.dong.djudge.controller;

import com.dong.djudge.dto.SandBoxSettingDTO;
import com.dong.djudge.service.SettingService;
import com.dong.djudge.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务器设置
 */
@RestController
public class SettingController {
    @Autowired
    private SettingService settingService;

    /**
     * 添加沙盒服务器
     *
     * @param sandBoxSettingDTO 服务器URL
     * @return 添加结果
     */
    @PostMapping("/server_url")
    public ResponseResult<Object> serverUrl(@RequestBody SandBoxSettingDTO sandBoxSettingDTO) {
        return settingService.postServerUrl(sandBoxSettingDTO);
    }

    /**
     * 获取沙盒服务器列表
     *
     * @return 沙盒服务器列表
     */
    @GetMapping("/server_url")
    public ResponseResult<Object> serverUrl() {
        return settingService.getServerUrl();
    }

    /**
     * 删除沙盒服务器
     *
     * @param sid 服务器id
     * @return 删除结果
     */
    @DeleteMapping("/server_url")
    public ResponseResult<Object> deleteServerUrl(@RequestParam String sid) {
        return settingService.deleteServerUrl(sid);
    }

    /**
     * 上线服务器
     *
     * @param sid 服务器id
     * @return 上线结果
     */
    @GetMapping("/online_server")
    public ResponseResult<Object> onlineServer(@RequestParam String sid) {
        return settingService.onlineServer(sid);
    }

    /**
     * 下线服务器
     *
     * @param sid 服务器id
     * @return 下线结果
     */
    @GetMapping("/offline_server")
    public ResponseResult<Object> offlineServer(@RequestParam String sid) {
        return settingService.offlineServer(sid);
    }

    /**
     * 获取系统硬件信息
     *
     * @param sid 服务器id
     * @return 系统信息
     */
    @GetMapping("/systemDeviceInfo")
    public ResponseResult<Object> systemInfo(@RequestParam String sid) {
        return settingService.systemInfo(sid);
    }

    /**
     * 获取系统配置信息
     * @param sid 服务器id
     * @return 系统配置信息
     */
    @GetMapping("/systemConfigInfo")
    public ResponseResult<Object> systemConfigInfo(@RequestParam String sid) {
        return settingService.systemConfigInfo(sid);
    }


    /**
     * 获取服务器信息
     *
     * @return 服务器信息
     */
    @GetMapping("/serverInfo")
    public ResponseResult<Object> serverInfo() {
        return settingService.serverInfo();
    }

    /**
     * 获取服务器名称
     *
     * @param sid  服务器id
     * @param name 服务器名称
     * @return 获取结果
     */
    @PostMapping("/editServerName")
    public ResponseResult<Object> editServerName(@RequestParam String sid, @RequestParam String name) {
        return settingService.editServerName(sid, name);
    }


    /**
     * 获取语言字典
     *
     * @return 语言字典
     */
    @PostMapping("/getLanguageDictionary")
    public ResponseResult<Object> getLanguageDictionary() {
        return settingService.getLanguageDictionary();
    }

    /**
     * 获取语言安装配置
     *
     * @return 语言安装配置
     */
    @GetMapping("/getLanguageInstallConfig")
    public ResponseResult<Object> getLanguageInstallConfig() {
        return settingService.getLanguageInstallConfig();
    }


}
