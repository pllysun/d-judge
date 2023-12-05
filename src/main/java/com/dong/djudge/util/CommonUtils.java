package com.dong.djudge.util;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.io.resource.Resource;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSON;
import com.dong.djudge.entity.TestCaseGroup;
import com.dong.djudge.entity.TestCaseGroupRoot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CommonUtils {

    /**
     * 获取当前类路径下的文件路径。
     *
     * @return 文件路径字符串
     * @throws Exception 如果发生异常，抛出通用异常
     */
    public static String getFilePath() throws Exception {
        try {
            // 获取ClassPathResource，表示当前类路径
            Resource resource = new ClassPathResource("");

            // 获取文件的URL
            URL fileUrl = ResourceUtils.getURL(resource.getUrl().getPath());

            // 将URL转换为Path
            Path path = Paths.get(fileUrl.toURI());

            // 移到父目录（移除路径的最后一个元素）
            path = path.getParent();

            // 再次移动到父目录以移除 "target"
            path = path.getParent();

            // 用空字符串替换 "test-classes" 和 "classes"
            path = path.resolve("file"); // 添加 "file" 到路径

            // 将Path转换为String
            return path.toString();
        } catch (Exception e) {
            // 处理异常，例如 URISyntaxException 或 IOException
            log.error("获取文件路径错误:{}", e.getMessage());
            throw new Exception("获取文件路径错误", e);
        }
    }


    /**
     * 从指定URL获取JSON内容并返回字符串，先进行有效性检查。
     *
     * @param url 要获取JSON内容的URL地址。
     * @return 如果成功获取并内容是有效JSON，则返回JSON字符串；否则返回null。
     * @throws Exception 如果在获取JSON内容的过程中发生异常，则抛出Exception。
     */
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
            // 获取JSON内容的过程中发生异常，返回null
            return null;
        }

        // 检查检索到的内容是否是有效的JSON
        if (!CommonUtils.isValidJson(jsonContent)) {
            // 如果不是有效的JSON，返回null
            return null;
        }

        // 返回获取到的JSON内容
        return jsonContent;
    }


    /**
     * 检查给定的字符串是否是一个有效的URL。
     *
     * @param url 要检查的字符串。
     * @return 如果是有效的URL，则返回true；否则返回false。
     */
    private static boolean isValidUrl(String url) {
        try {
            // 尝试将字符串转换为URL，并检查是否存在语法错误
            new URL(url).toURI();
            // 如果没有抛出异常，则表示是一个有效的URL
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            // 捕获MalformedURLException和URISyntaxException异常，表示不是一个有效的URL
            return false;
        }
    }


    /**
     * 从指定URL获取JSON内容并返回字符串。
     *
     * @param url URL地址。
     * @return 包含从URL获取的JSON内容的字符串。
     * @throws Exception 如果从URL检索JSON内容失败，则抛出Exception。
     */
    private static String getJsonFromUrl(String url) throws Exception {
        try {
            // 创建RestTemplate对象
            RestTemplate restTemplate = new RestTemplate();

            // 使用RestTemplate从URL获取JSON内容
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            // 记录从URL检索JSON内容失败的异常信息
            log.error("从URL检索JSON内容失败：{}", url, e);

            // 抛出包含失败原因的异常
            throw new Exception("从URL检索JSON内容失败", e);
        }
    }


    /**
     * 从指定文件路径读取JSON内容并返回字符串。
     *
     * @return 包含文件内容的JSON字符串。
     * @throws FileNotFoundException 如果文件不存在，则抛出FileNotFoundException。
     */
    public static String getJsonForFile(String fileName) throws Exception {
        String fileContent = null;
        try {
            String filePath = getFilePath(fileName);
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
            // 记录从文件检索JSON内容失败的异常信息
            log.error("从文件检索JSON内容失败：{}", fileName, e);
        }
        return fileContent;
    }


    /**
     * 生成具有指定前缀的随机大写字符串。
     *
     * @param prefix 前缀字符串。
     * @param length 生成的字符串总长度（包括前缀）。
     * @return 包含前缀和随机字符的字符串。
     */
    public static String generateRandomUpperCaseWithPrefix(String prefix, int length) {
        // 生成一个随机的UUID字符串并转换为大写形式，去掉其中的横杠
        String randomUUID = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        // 生成结果字符串，包括指定前缀和部分随机字符，确保不超过总长度
        return prefix + randomUUID.substring(0, Math.min(length - prefix.length(), 32));
    }


    /**
     * 将内容写入具有指定文件标识的文件。
     *
     * @param fileId  文件的唯一标识符。
     * @param content 要写入文件的内容。
     */
    public static void writeFile(String fileId, String content) throws Exception {
        Path filePath = getFilePathForFileId(fileId);

        try {
            // 将内容写入文件
            // 创建一个FileWriter对象，用于写入文件
            FileWriter fileWriter = new FileWriter(filePath.toString(), false);
            // 创建一个BufferedWriter对象，用于提高写入性能
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            // 将内容写入文件
            bufferedWriter.write(content);
            // 关闭流
            bufferedWriter.close();
            fileWriter.close();
            // 记录文件写入成功的日志信息
            log.info("{}文件写入成功，文件路径:{}", fileId, filePath);
        } catch (IOException e) {
            // 记录文件写入失败的异常信息
            log.error(e.getMessage());
            log.error("文件写入失败");
        }
    }


    /**
     * 获取指定文件标识的文件路径对象.
     *
     * @param fileId 文件标识
     * @return 文件路径对象
     * @throws Exception 如果获取文件路径时发生异常
     */
    private static Path getFilePathForFileId(String fileId) throws Exception {
        // 获取文件路径
        String uri = getFilePath();

        // 获取Path对象，表示文件路径
        Path path = Paths.get(uri + File.separator);

        // 创建文件路径，使用文件的唯一标识 fileId 作为文件名，并添加 .json 扩展名
        return path.resolve(fileId + ".json");
    }

    /**
     * 判断指定文件路径是否存在.
     *
     * @param filePath 文件路径
     * @return 如果文件存在则返回 true，否则返回 false
     * @throws Exception 如果获取文件路径时发生异常
     */
    public static boolean isFileExist(String filePath) throws Exception {
        Path filePathForFileId = getFilePathForFileId(filePath);
        return Files.exists(filePathForFileId);
    }

    /**
     * 读取指定文件标识的文件内容并返回.
     *
     * @param fileId 文件标识
     * @return 文件内容字符串
     * @throws Exception 如果读取文件内容时发生异常
     */
    public static String readFile(String fileId) throws Exception {
        String filePath = getFilePath(fileId);
        String fileContent = null;
        try {
            fileContent = readFileContent(filePath);
            log.info("文件内容:\n" + fileContent);
            // 在实际应用中，你可能需要将文件内容传递给前端或进行其他操作
        } catch (IOException e) {
            log.warn("读取文件时发生错误: " + e.getMessage());
            // 在实际应用中，你可能需要返回一个错误页面或其他错误处理逻辑
        }

        return fileContent; // 返回一个成功页面或其他结果
    }

    /**
     * 获取指定文件标识的文件路径字符串.
     *
     * @param fileId 文件标识
     * @return 文件路径字符串
     * @throws Exception 如果获取文件路径时发生异常
     */
    private static String getFilePath(String fileId) throws Exception {
        String baseUrl = getFilePath();
        return baseUrl + File.separator + fileId + ".json";
    }

    /**
     * 读取指定文件路径的文件内容并以字符串形式返回.
     *
     * @param filePath 文件路径
     * @return 文件内容字符串
     * @throws IOException 如果读取文件内容时发生异常
     */
    private static String readFileContent(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        StringBuilder content = new StringBuilder();

        for (String line : lines) {
            content.append(line).append("\n");
        }

        return content.toString();
    }

    /**
     * 删除本地文件.
     *
     * @param fileId 要删除的文件的唯一标识
     * @throws Exception 如果在文件删除过程中发生异常
     */
    public static void deleteLocalFile(String fileId) throws Exception {
        String filePath = getFilePath(fileId);
        Path path = Paths.get(filePath);

        try {
            Files.delete(path);
        } catch (IOException e) {
            // 在实际应用中，你可能需要根据具体情况进行适当的错误处理
            log.error("删除文件时发生错误: " + e.getMessage());
            throw new Exception("文件删除失败", e);
        }
    }


    /**
     * 验证提供的字符串是否是有效的JSON表示。
     *
     * @param jsonString 要验证的输入JSON字符串。
     * @return 如果JSON无效则为true，如果有效则为false。
     */
    public static boolean isValidJson(String jsonString) {
        try {
            // 尝试将JSON字符串解析为TestCaseGroupRoot对象的列表
            List<TestCaseGroupRoot> testCaseGroupRoots = JSON.parseArray(jsonString, TestCaseGroupRoot.class);

            // 检查解析后的列表是否为null或空，表示JSON结构无效
            if (testCaseGroupRoots == null || testCaseGroupRoots.isEmpty()) {
                return true; // 无效的JSON
            }

            // 创建一个Map以根据其'gid'值存储TestCaseGroupRoot对象
            Map<Integer, TestCaseGroupRoot> rootMap = new HashMap<>();

            // 遍历TestCaseGroupRoot对象的列表
            for (TestCaseGroupRoot testCaseGroupRoot : testCaseGroupRoots) {
                // 检查Map中是否已存在'gid'值，表示重复
                if (rootMap.containsKey(testCaseGroupRoot.getGid())) {
                    return true; // 具有重复'gid'的无效JSON
                }

                // 使用'gid'作为键将TestCaseGroupRoot对象添加到Map中
                rootMap.put(testCaseGroupRoot.getGid(), testCaseGroupRoot);

                // 创建一个Map以根据其'id'值存储TestCaseGroup对象
                Map<Integer, TestCaseGroup> caseMap = new HashMap<>();

                // 遍历当前TestCaseGroupRoot中的TestCaseGroup对象列表
                for (TestCaseGroup testCaseGroup : testCaseGroupRoot.getInput()) {
                    // 检查Map中是否已存在'id'值，表示重复
                    if (caseMap.containsKey(testCaseGroup.getId())) {
                        return true; // 具有重复'id'的无效JSON，位于同一TestCaseGroupRoot内
                    }

                    // 使用'id'作为键将TestCaseGroup对象添加到Map中
                    caseMap.put(testCaseGroup.getId(), testCaseGroup);
                }
            }

            return false; // 有效的JSON
        } catch (Exception e) {
            log.warn("JSON解析失败:{}", e.getMessage());
            // 解析失败，表示输入不是有效的JSON
            return true; // 无效的JSON
        }
    }


    /**
     * 从 JSON 字符串中解析测试用例组列表.
     *
     * @param jsonString 包含测试用例组信息的 JSON 字符串
     * @return 测试用例组列表
     */
    public static List<TestCaseGroupRoot> getTestCaseGroupList(String jsonString) {
        return JSON.parseArray(jsonString, TestCaseGroupRoot.class);
    }


    /**
     * 将指定的 JSON 字符串写入文件，并构建包含文件内容的 ResponseEntity 对象.
     *
     * @param standardCodeId 要写入的文件的唯一标识
     * @param jsonContent 要写入文件的 JSON 内容字符串
     * @return 包含文件内容的 ResponseEntity 对象
     */
    public static ResponseEntity<Object> getObjectResponseEntity(String standardCodeId, String jsonContent) {
        try {
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + standardCodeId + ".json");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(jsonContent);
        } catch (Exception e) {
            System.err.println("文件下载时发生错误: " + e.getMessage());
            // 在实际应用中，你可能需要返回一个错误页面或其他错误处理逻辑
            return ResponseEntity
                    .status(500)
                    .body(null);
        }
    }

}