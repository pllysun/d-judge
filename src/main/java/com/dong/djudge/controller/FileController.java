package com.dong.djudge.controller;

import com.dong.djudge.enums.UpLoadFileEnum;
import com.dong.djudge.service.FileService;
import com.dong.djudge.util.JsonUtils;
import com.dong.djudge.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

/**
 * @author 樊东升
 * @date 2023/11/27 22:38
 */
@Slf4j
@RestController()
public class FileController {

    @Autowired
    private FileService fileService;

    /**
     * 上传文件
     *
     * @param type    类型，有三种，一种是测试Json，一种是测试Json的网络URL，一种是测试文件
     * @param content 具体内容
     */
    @PutMapping(value = "/file")
    public Result<Objects> uploadFile(String type, String content, @RequestPart("file") MultipartFile file) {
        try {
            log.info("type:{}, content:{}", type, content);
            // 获取类型码
            Integer code = Objects.requireNonNull(UpLoadFileEnum.getTypeByValue(type)).getCode();

            switch (code) {
                case 0:
                    // 检查 JSON 是否有效
                    if (!JsonUtils.isValidJson(content)) {
                        return new Result<>(0, "无效的 JSON 内容！", null);
                    }
                    fileService.uploadFile(content);
                    break;
                case 1:
                    // 检查URL是否有效
                    if (!isValidUrl(content)) {
                        return new Result<>(0, "无效的URL！", null);
                    }
                    // 从URL获取JSON内容
                    String jsonContent = getJsonFromUrl(content);

                    // 检查检索到的内容是否是有效的JSON
                    if (!JsonUtils.isValidJson(jsonContent)) {
                        return new Result<>(0, "无效的JSON 内容！", null);
                    }
                    // 处理JSON内容或调用fileService.uploadFile方法
                    fileService.uploadFile(jsonContent);
                    break;
                case 2:
                    // 处理类型为2的情况
                    try {
                        // 使用 Apache Tika 检测文件类型
                        String fileType = new Tika().detect(file.getBytes());

                        // 检查文件类型是否为 JSON
                        if (!"application/json".equals(fileType)) {
                            return new Result<>(0, "文件类型不是 JSON，请检查您的文件格式！", null);
                        }
                        String json = new String(file.getBytes());
                        if (!JsonUtils.isValidJson(json)) {
                            return new Result<>(0, "无效的 JSON 内容！", null);
                        }
                        fileService.uploadFile(json);
                    } catch (IOException e) {
                        log.error("文件类型检测失败！", e);
                        return new Result<>(0, "文件类型检测失败！", null);
                    }
                    break;
                default:
                    return new Result<>(0, "调用参数错误！请检查您的调用参数！", null);
            }

            // 处理其他逻辑

            // 返回成功的结果
            return new Result<>(1, "成功", null);

        } catch (Exception e) {
            log.error("发生异常！", e);
            // 返回通用的错误结果
            return new Result<>(0, "发生异常！", null);
        }
    }

    // 检查给定字符串是否是有效URL的辅助方法
    private boolean isValidUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }

    // 从URL检索JSON内容的辅助方法
    private String getJsonFromUrl(String url) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            log.error("从URL检索JSON内容失败：{}", url, e);
            throw new RuntimeException("从URL检索JSON内容失败", e);
        }
    }
}
