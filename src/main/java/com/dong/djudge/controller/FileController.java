package com.dong.djudge.controller;

import com.dong.djudge.dto.TestGroupFileDTO;
import com.dong.djudge.enums.UpLoadFileEnum;
import com.dong.djudge.service.FileService;
import com.dong.djudge.util.JsonUtils;
import com.dong.djudge.util.Result;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

/**
 * 上传测试集服务类
 *
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
     * 这个接口主要用于上传测试集，当测试集过大或者出于其他目的，我们需要将测试集提前上传上去，那么就需要调用这个接口\n
     * 这个接口支持三种方式上传测试集
     * 1、JSON，通过JSON直接将测试集上传服务器本地
     * 2、URL，通过URL，一般来说也就是对象存储的URL，在服务器下载测试集然后维护这个测试集
     * 3、文件，通过文件上传测试集
     * 这三种方式都是通过type来区分的，type有三种，分别是JSON，URL，file，不区分大小写
     * 并且测试集要满足一定的格式，具体的测试集格式请参考文档
     * type    文件类型，有三种，一种是测试Json，一种是测试Json的网络URL，一种是测试文件
     * content 文件具体内容，但type=file时，这个参数可以为空
     * file    type为file时这个参数才有效件
     * 参数类型有三种，一种是测试Json，一种是测试Json的网络URL，一种是测试文件
     */
    @PutMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Objects> uploadFile(@ModelAttribute @Valid TestGroupFileDTO testGroupFileDTO) {
        log.info("type:{}, content:{},file:{}", testGroupFileDTO.getType(), testGroupFileDTO.getContent(), testGroupFileDTO.getFile());
        if (testGroupFileDTO.getType() == null || testGroupFileDTO.getType().isBlank()) {
            return new Result<>(0, "type参数为空，请检查！", null);
        }
        try {
            log.info("type:{}, content:{},file:{}", testGroupFileDTO.getType(), testGroupFileDTO.getContent(), testGroupFileDTO.getFile());
            // 获取类型码
            Integer code;
            try {
                code = Objects.requireNonNull(UpLoadFileEnum.getTypeByValue(testGroupFileDTO.getType())).getCode();
            } catch (NullPointerException e) {
                return new Result<>(0, "type参数仅支持:JSON,URL,FILE", null);
            }
            if (code == 0 || code == 1) {
                if (testGroupFileDTO.getContent() == null || testGroupFileDTO.getContent().isBlank()) {
                    return new Result<>(0, "content参数为空，请检查！", null);
                }
            }
            switch (code) {
                case 0:
                    // 检查 JSON 是否有效
                    if (!JsonUtils.isValidJson(testGroupFileDTO.getContent())) {
                        return new Result<>(0, "无效的 JSON 内容！", null);
                    }
                    fileService.uploadFile(testGroupFileDTO.getContent());
                    break;
                case 1:
                    // 检查URL是否有效
                    if (!isValidUrl(testGroupFileDTO.getContent())) {
                        return new Result<>(0, "无效的URL！", null);
                    }
                    // 从URL获取JSON内容
                    String jsonContent = getJsonFromUrl(testGroupFileDTO.getContent());

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
                        if (testGroupFileDTO.getFile() == null) {
                            return new Result<>(0, "但type=file时，file参数不能为空", null);
                        }
                        // 使用 Apache Tika 检测文件类型
                        String fileType = new Tika().detect(testGroupFileDTO.getFile().getBytes());

                        // 检查文件类型是否为 JSON
                        if (!"application/json".equals(fileType)) {
                            return new Result<>(0, "文件类型不是 JSON，请检查您的文件格式！", null);
                        }
                        String json = new String(testGroupFileDTO.getFile().getBytes());
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
