package com.dong.djudge.util;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.io.resource.Resource;
import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.dong.djudge.entity.InCaseGroupRoot;
import com.dong.djudge.entity.InTestCaseGroup;
import com.dong.djudge.entity.OutCaseGroupRoot;
import com.dong.djudge.entity.OutCaseResult;
import com.dong.djudge.entity.OutTestCaseGroup;
import com.dong.djudge.entity.SaveCaseGroupRoot;
import com.dong.djudge.entity.SaveTestCaseGroup;
import com.dong.djudge.entity.judge.RunResult;
import com.dong.djudge.entity.judge.RunResultRoot;
import com.dong.djudge.pojo.SandBoxSetting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@Component
public class CommonUtils {

    @Autowired
    private static ResourceLoader resourceLoader;
    /**
     * 获取当前类路径下的文件路径。
     *
     * @return 文件路径字符串
     * @throws Exception 如果发生异常，抛出通用异常
     */
    public static String getFilePath() throws Exception {
        try {
            // 获取当前工作目录（项目根目录）
            String currentWorkingDir = System.getProperty("user.dir");

            // 拼接文件路径
            Path filePath = Paths.get(currentWorkingDir, "file");

            // 将Path转换为String
            return filePath.toString();
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
            // 在实际应用中，你可能需要将文件内容传递给前端或进行其他操作
        } catch (IOException e) {
            log.warn("读取文件不存在:{}",e.getMessage());
            // 在实际应用中，你可能需要返回一个错误页面或其他错误处理逻辑
        }catch (Exception e){
            log.warn("读取文件时发生错误: " + e.getMessage());
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
            List<InCaseGroupRoot> inCaseGroupRoots = JSON.parseArray(jsonString, InCaseGroupRoot.class);

            // 检查解析后的列表是否为null或空，表示JSON结构无效
            if (inCaseGroupRoots == null || inCaseGroupRoots.isEmpty()) {
                return true; // 无效的JSON
            }

            // 创建一个Map以根据其'gid'值存储TestCaseGroupRoot对象
            Map<Integer, InCaseGroupRoot> rootMap = new HashMap<>();

            // 遍历TestCaseGroupRoot对象的列表
            for (InCaseGroupRoot inCaseGroupRoot : inCaseGroupRoots) {
                // 检查Map中是否已存在'gid'值，表示重复
                if (rootMap.containsKey(inCaseGroupRoot.getGid())) {
                    return true; // 具有重复'gid'的无效JSON
                }

                // 使用'gid'作为键将TestCaseGroupRoot对象添加到Map中
                rootMap.put(inCaseGroupRoot.getGid(), inCaseGroupRoot);

                // 创建一个Map以根据其'id'值存储TestCaseGroup对象
                Map<Integer, InTestCaseGroup> caseMap = new HashMap<>();

                // 遍历当前TestCaseGroupRoot中的TestCaseGroup对象列表
                for (InTestCaseGroup inTestCaseGroup : inCaseGroupRoot.getInput()) {
                    // 检查Map中是否已存在'id'值，表示重复
                    if (caseMap.containsKey(inTestCaseGroup.getId())) {
                        return true; // 具有重复'id'的无效JSON，位于同一TestCaseGroupRoot内
                    }

                    // 使用'id'作为键将TestCaseGroup对象添加到Map中
                    caseMap.put(inTestCaseGroup.getId(), inTestCaseGroup);
                }
            }

            return true; // 有效的JSON
        } catch (Exception e) {
            log.warn("JSON解析失败:{}", e.getMessage());
            // 解析失败，表示输入不是有效的JSON
            return false; // 无效的JSON
        }
    }


    /**
     * 将 JSON 字符串转换为 InCaseGroupRoot 对象的列表。
     *
     * @param jsonString 要转换的 JSON 字符串
     * @return 包含 InCaseGroupRoot 对象的列表
     */
    public static List<InCaseGroupRoot> getInTestGroupForJson(String jsonString) {
        // 使用 fastjson 的 parseArray 方法将 JSON 字符串转换为 InCaseGroupRoot 对象的列表
        return JSON.parseArray(jsonString, InCaseGroupRoot.class);
    }

    /**
     * 将 JSON 字符串转换为 SaveCaseGroupRoot 对象的列表。
     *
     * @param jsonString 要转换的 JSON 字符串
     * @return 包含 SaveCaseGroupRoot 对象的列表
     */
    public static List<SaveCaseGroupRoot> getSaveTestGroupForJson(String jsonString) {
        // 使用 fastjson 的 parseArray 方法将 JSON 字符串转换为 SaveCaseGroupRoot 对象的列表
        return JSON.parseArray(jsonString, SaveCaseGroupRoot.class);
    }


    /**
     * 将指定的 JSON 字符串写入文件，并构建包含文件内容的 ResponseEntity 对象.
     *
     * @param standardCodeId 要写入的文件的唯一标识
     * @param jsonContent    要写入文件的 JSON 内容字符串
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

    /**
     * 将 TestCaseGroupRoot 列表转换为 Map，其中每个 TestCaseGroupRoot 的 gid 值映射到另一个 Map，
     * 该 Map 将每个 TestCaseGroup 的 id 值映射到相应的 TestCaseGroup 对象。
     *
     * @param inCaseGroupRootList 包含 TestCaseGroupRoot 对象的列表
     * @return 一个 Map，其中每个 TestCaseGroupRoot 的 gid 值映射到另一个 Map，
     * 该 Map 将每个 TestCaseGroup 的 id 值映射到相应的 TestCaseGroup 对象
     */
    public static Map<Integer, Map<Integer, InTestCaseGroup>> getTestCaseGroupMapByList(List<InCaseGroupRoot> inCaseGroupRootList) {
        // 创建一个新的 Map 来存储结果
        Map<Integer, Map<Integer, InTestCaseGroup>> testCaseGroupRootMap = new HashMap<>();
        // 遍历 testCaseGroupRootList
        for (InCaseGroupRoot inCaseGroupRoot : inCaseGroupRootList) {
            // 从 testCaseGroupRootMap 中获取或创建一个新的 Map，该 Map 将每个 TestCaseGroup 的 id 值映射到相应的 TestCaseGroup 对象
            Map<Integer, InTestCaseGroup> orDefault = testCaseGroupRootMap.getOrDefault(inCaseGroupRoot.getGid(), new HashMap<>());
            // 遍历 testCaseGroupRoot 的输入列表
            for (InTestCaseGroup inTestCaseGroup : inCaseGroupRoot.getInput()) {
                // 将 testCaseGroup 的 id 值和 testCaseGroup 对象添加到 Map 中
                orDefault.put(inTestCaseGroup.getId(), inTestCaseGroup);
            }
            // 将 gid 值和 Map 添加到 testCaseGroupRootMap 中
            testCaseGroupRootMap.put(inCaseGroupRoot.getGid(), orDefault);
        }
        // 返回结果 Map
        return testCaseGroupRootMap;
    }

    /**
     * 获取并返回基于 runResultRoot 的 TestCaseGroupRoot 列表。
     *
     * @return 表示测试用例组的 TestCaseGroupRoot 列表。
     */
    public static List<SaveCaseGroupRoot> getRunResultList(RunResultRoot runResultRoot) {
        List<SaveCaseGroupRoot> list = new ArrayList<>();
        // 遍历 runResultRoot 提取测试用例组信息
        for (Integer i : runResultRoot.getRunResult().keySet()) {
            SaveCaseGroupRoot saveCaseGroupRoot = new SaveCaseGroupRoot();
            Map<Integer, RunResult> integerRunResultMap = runResultRoot.getRunResult().get(i);
            for (Integer j : integerRunResultMap.keySet()) {
                SaveTestCaseGroup saveTestCaseGroup = new SaveTestCaseGroup();
                RunResult runResult = integerRunResultMap.get(j);
                saveTestCaseGroup.setId(j);
                saveTestCaseGroup.setInput(runResult.getInput());
                saveTestCaseGroup.setValue(runResult.getFiles().getStdout());
                // 将 testCaseGroup 添加到 testCaseGroupRoot 的输入列表中
                if (saveCaseGroupRoot.getSavePut() == null) {
                    saveCaseGroupRoot.setSavePut(new ArrayList<>());
                }
                saveCaseGroupRoot.getSavePut().add(saveTestCaseGroup);
                saveCaseGroupRoot.setGid(i);
            }
            list.add(saveCaseGroupRoot);
        }
        return list;
    }

    /**
     * 获取并设置测试用例组的运行结果
     *
     * @param runResultRoot 包含运行结果的对象
     * @param outCaseGroupRootList 测试用例组列表，将被设置运行结果
     * @return 更新后的测试用例组列表
     */
    public static List<OutCaseGroupRoot> getCaseResult(RunResultRoot runResultRoot, List<OutCaseGroupRoot> outCaseGroupRootList) {
        // 创建一个映射，用于存储每个测试组的结果
        Map<Integer, OutCaseResult> groupResultMap = new HashMap<>();
        // 创建一个映射，用于存储每个测试用例的结果
        Map<Integer, Map<Integer, OutCaseResult>> testCaseResultMap = new HashMap<>();
        // 获取运行结果
        Map<Integer, Map<Integer, RunResult>> runResult = runResultRoot.getRunResult();
        // 遍历运行结果
        for (Map.Entry<Integer, Map<Integer, RunResult>> groupEntry : runResult.entrySet()) {
            // 创建一个映射，用于存储每个测试用例的结果
            Map<Integer, OutCaseResult> testCaseMap = new HashMap<>();
            // 初始化总时间、运行时间和内存使用量
            long totalTime = 0L, totalRunTime = 0L, totalMemory = 0L;
            // 遍历每个测试用例的运行结果
            for (Map.Entry<Integer, RunResult> testCaseEntry : groupEntry.getValue().entrySet()) {
                // 获取当前测试用例的运行结果
                RunResult rR = testCaseEntry.getValue();
                // 累加总时间、运行时间和内存使用量
                totalTime += rR.getTime();
                totalRunTime += rR.getRunTime();
                totalMemory += rR.getMemory();
                // 将当前测试用例的结果存入映射
                testCaseMap.put(testCaseEntry.getKey(), new OutCaseResult(totalTime, totalRunTime, totalMemory));
            }
            // 将测试组的结果存入映射，注意这里对时间和内存使用量进行了单位转换
            groupResultMap.put(groupEntry.getKey(), new OutCaseResult(totalTime/1000000, totalRunTime/1000000, totalMemory/1024));
            // 将测试用例的结果存入映射
            testCaseResultMap.put(groupEntry.getKey(), testCaseMap);
        }
        // 遍历测试用例组列表
        for (OutCaseGroupRoot outCaseGroupRoot : outCaseGroupRootList) {
            // 设置测试用例组的结果
            outCaseGroupRoot.setGroupResult(groupResultMap.get(outCaseGroupRoot.getGid()));

            // 遍历测试用例组的输出列表
            for (OutTestCaseGroup outTestCaseGroup : outCaseGroupRoot.getOutput()) {
                // 获取并设置测试用例的结果，注意这里对时间和内存使用量进行了单位转换
                OutCaseResult ocr = testCaseResultMap.get(outCaseGroupRoot.getGid()).get(outTestCaseGroup.getId());
                ocr.setMemory(ocr.getMemory()/1024);
                ocr.setRunTime(ocr.getRunTime()/1000000);
                ocr.setTime(ocr.getTime()/1000000);
                outTestCaseGroup.setCaseResult(ocr);
            }
        }
        // 返回更新后的测试用例组列表
        return outCaseGroupRootList;
    }

    /**
     * 比较两个 SaveCaseGroupRoot 列表，并根据每个 SaveTestCaseGroup 的 value 值是否相等来设置其 isAccepted 属性。
     * 如果在同一 SaveCaseGroupRoot 中的任何 SaveTestCaseGroup 的 value 值不相等，那么该 SaveCaseGroupRoot 的 groupAccepted 属性将被设置为 false。
     *
     * @param TA 测试答案，包含 SaveCaseGroupRoot 对象的列表
     * @param SA 标准答案，包含 SaveCaseGroupRoot 对象的列表
     * @return 更新后的 TA 列表，其中每个 SaveTestCaseGroup 和 SaveCaseGroupRoot 的 isAccepted 属性已经根据比较结果进行了设置
     */
    public static List<OutCaseGroupRoot> getTestCaseGroupRoots(List<SaveCaseGroupRoot> TA, List<SaveCaseGroupRoot> SA) {
        // 创建一个新的 OutCaseGroupRoot 列表来存储结果
        List<OutCaseGroupRoot> list = new ArrayList<>();
        // 遍历 TA 列表
        for (int i = 0; i < TA.size(); i++) {
            // 获取当前位置的 SaveCaseGroupRoot 对象
            SaveCaseGroupRoot ta = TA.get(i);
            SaveCaseGroupRoot sa = SA.get(i);
            // 创建一个新的 OutCaseGroupRoot 对象
            OutCaseGroupRoot outCaseGroupRoot = new OutCaseGroupRoot();
            // 遍历当前 SaveCaseGroupRoot 的输入列表
            for (int j = 0; j < ta.getSavePut().size(); j++) {
                // 创建一个新的 OutTestCaseGroup 对象
                OutTestCaseGroup outTestCaseGroup = new OutTestCaseGroup();
                // 获取当前位置的 SaveTestCaseGroup 对象
                SaveTestCaseGroup t = ta.getSavePut().get(j);
                SaveTestCaseGroup s = sa.getSavePut().get(j);
                // 设置 OutTestCaseGroup 的 id 和 value 属性
                outTestCaseGroup.setId(t.getId());
                outTestCaseGroup.setInput(t.getInput());
                outTestCaseGroup.setTaOutput(t.getValue());
                outTestCaseGroup.setSaOutput(s.getValue());

                // 移除末尾的换行符并进行比较
                String taValue = removeTrailingNewline(t.getValue());
                String saValue = removeTrailingNewline(s.getValue());

                // 比较 SaveTestCaseGroup 的 value 值是否相等
                if (!taValue.equals(saValue)) {
                    // 如果 value 值不相等，设置 isAccepted 为 false，并将 OutCaseGroupRoot 的 groupAccepted 设置为 false
                    outTestCaseGroup.setAccepted(false);
                    outCaseGroupRoot.setGroupAccepted(false);
                }

                // 将 OutTestCaseGroup 对象添加到 OutCaseGroupRoot 的输出列表中
                if (outCaseGroupRoot.getOutput() == null) {
                    outCaseGroupRoot.setOutput(new ArrayList<>());
                }
                outCaseGroupRoot.getOutput().add(outTestCaseGroup);
            }
            outCaseGroupRoot.setGid(ta.getGid());
            // 将 OutCaseGroupRoot 对象添加到结果列表中
            list.add(outCaseGroupRoot);
        }
        // 返回结果列表
        return list;
    }

    /**
     * 去除字符串末尾的换行符，如果存在。
     *
     * @param value 输入的字符串
     * @return 去除末尾换行符后的字符串
     */
    private static String removeTrailingNewline(String value) {
        if (value != null && value.endsWith("\n")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }


    public static String getInstalledPackages(RestTemplate restTemplate, SandBoxSetting sandBoxSetting) {
        String s = sandBoxSetting.getBaseUrl() + "/getInstallList";
        ResponseEntity<String> forEntity = restTemplate.getForEntity(s, String.class);
        String body = forEntity.getBody();
        return Objects.requireNonNull(JSONObject.parseObject(body)).getString("installed_packages");
    }

    public static String getLanguage(String language) {
        return switch (language.toLowerCase()) {
            case "c" -> "C";
            case "cpp", "c++" -> "C++";
            case "java" -> "Java";
            case "python" -> "Python";
            case "python3" -> "python3";
            default -> language;
        };
    }

}
