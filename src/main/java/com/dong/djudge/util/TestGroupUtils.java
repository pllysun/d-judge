package com.dong.djudge.util;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.io.resource.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Slf4j
public class TestGroupUtils {
    public String getJarFilePath() {
        Resource resource = new ClassPathResource("");
        URL file = resource.getUrl();
        return file.getPath().replace("test-classes/", "").replace("target/", "").replace("/G", "G").replace("/classes","") + "file/";
    }

    public static String getJsonForURL(String url) throws Exception {
        // 检查URL是否有效
        if (!isValidUrl(url)) {
            return null;
        }
        // 从URL获取JSON内容
        String jsonContent;
        try {
            jsonContent = getJsonFromUrl(url);
        } catch (Exception e) {
            return null;
        }

        // 检查检索到的内容是否是有效的JSON
        if (JsonUtils.isValidJson(jsonContent)) {
            return null;
        }
        return jsonContent;
    }

    // 检查给定字符串是否是有效URL的辅助方法
    private static boolean isValidUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }

    // 从URL检索JSON内容的辅助方法
    private static String getJsonFromUrl(String url) throws Exception {
        try {
            RestTemplate restTemplate = new RestTemplate();
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            log.error("从URL检索JSON内容失败：{}", url, e);
            throw new Exception("从URL检索JSON内容失败", e);
        }
    }

    public static String getJsonForFile(String filePath) throws FileNotFoundException {
        String fileContent = null;
        try {
            // 创建File对象
            File file = new File(filePath);

            // 使用FileInputStream读取文件内容
            FileInputStream fileInputStream;
            try {
                fileInputStream = new FileInputStream(file);

            } catch (FileNotFoundException e) {
                throw new FileNotFoundException("文件不存在");
            }
            // 使用FileCopyUtils将文件内容转换为字节数组
            byte[] fileBytes = FileCopyUtils.copyToByteArray(fileInputStream);

            // 将字节数组转换为字符串，这里使用UTF-8编码
            fileContent = new String(fileBytes, StandardCharsets.UTF_8);

            // 关闭输入流
            fileInputStream.close();
        } catch (IOException e) {
            log.error("从文件检索JSON内容失败：{}", filePath, e);
        }
        return fileContent;
    }
}
