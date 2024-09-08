package com.dong.djudge.judge;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dong.djudge.exception.SystemException;
import com.dong.djudge.util.Constants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j(topic = "hoj")
public class SandboxRun {

    public static final HashMap<String, Integer> RESULT_MAP_STATUS = new HashMap<>();
    public static final List<String> SIGNALS = Arrays.asList(
            "", // 0
            "Hangup", // 1
            "Interrupt", // 2
            "Quit", // 3
            "Illegal instruction", // 4
            "Trace/breakpoint trap", // 5
            "Aborted", // 6
            "Bus error", // 7
            "Floating point exception", // 8
            "Killed", // 9
            "User defined signal 1", // 10
            "Segmentation fault", // 11
            "User defined signal 2", // 12
            "Broken pipe", // 13
            "Alarm clock", // 14
            "Terminated", // 15
            "Stack fault", // 16
            "Child exited", // 17
            "Continued", // 18
            "Stopped (signal)", // 19
            "Stopped", // 20
            "Stopped (tty input)", // 21
            "Stopped (tty output)", // 22
            "Urgent I/O condition", // 23
            "CPU time limit exceeded", // 24
            "File size limit exceeded", // 25
            "Virtual timer expired", // 26
            "Profiling timer expired", // 27
            "Window changed", // 28
            "I/O possible", // 29
            "Power failure", // 30
            "Bad system call" // 31
    );
    @Getter
    private static final RestTemplate restTemplate;
    // 单例模式
    private static final SandboxRun instance = new SandboxRun();
    private static final String SANDBOX_BASE_URL = "http://8.219.11.202:5051";
    private static final int maxProcessNumber = 128;
    private static final int TIME_LIMIT_MS = 16000;
    private static final int MEMORY_LIMIT_MB = 512;
    private static final int STACK_LIMIT_MB = 128;
    private static final int STDIO_SIZE_MB = 32;
    private static final JSONArray COMPILE_FILES = new JSONArray();

    static {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(20000);
        requestFactory.setReadTimeout(180000);
        restTemplate = new RestTemplate(requestFactory);
    }

    static {
        RESULT_MAP_STATUS.put("Time Limit Exceeded", Constants.Judge.STATUS_TIME_LIMIT_EXCEEDED.getStatus());
        RESULT_MAP_STATUS.put("Memory Limit Exceeded", Constants.Judge.STATUS_MEMORY_LIMIT_EXCEEDED.getStatus());
        RESULT_MAP_STATUS.put("Output Limit Exceeded", Constants.Judge.STATUS_RUNTIME_ERROR.getStatus());
        RESULT_MAP_STATUS.put("Accepted", Constants.Judge.STATUS_ACCEPTED.getStatus());
        RESULT_MAP_STATUS.put("Nonzero Exit Status", Constants.Judge.STATUS_RUNTIME_ERROR.getStatus());
        RESULT_MAP_STATUS.put("Internal Error", Constants.Judge.STATUS_SYSTEM_ERROR.getStatus());
        RESULT_MAP_STATUS.put("File Error", Constants.Judge.STATUS_SYSTEM_ERROR.getStatus());
        RESULT_MAP_STATUS.put("Signalled", Constants.Judge.STATUS_RUNTIME_ERROR.getStatus());
    }

    static {
        JSONObject content = new JSONObject();
        content.set("content", "");

        JSONObject stdout = new JSONObject();
        stdout.set("name", "stdout");
        stdout.set("max", 1024 * 1024 * STDIO_SIZE_MB);

        JSONObject stderr = new JSONObject();
        stderr.set("name", "stderr");
        stderr.set("max", 1024 * 1024 * STDIO_SIZE_MB);
        COMPILE_FILES.put(content);
        COMPILE_FILES.put(stdout);
        COMPILE_FILES.put(stderr);
    }

    public SandboxRun() {

    }

    public static String getSandboxBaseUrl() {
        return SANDBOX_BASE_URL;
    }

    public static void delFile(String url,String fileId) {
        try {
            restTemplate.delete(url + "/file/{0}", fileId);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode() != HttpStatusCode.valueOf(200)) {
                log.error("安全沙箱判题的删除内存中的文件缓存操作异常----------------->{}", ex.getResponseBodyAsString());
            }
        }

    }

    /**
     * @param maxCpuTime        最大编译的cpu时间 ms
     * @param maxRealTime       最大编译的真实时间 ms
     * @param maxMemory         最大编译的空间 b
     * @param maxStack          最大编译的栈空间 b
     * @param srcName           编译的源文件名字
     * @param exeName           编译生成的exe文件名字
     * @param args              编译的cmd参数
     * @param envs              编译的环境变量
     * @param code              编译的源代码
     * @param extraFiles        编译所需的额外文件 key:文件名，value:文件内容
     * @param needCopyOutCached 是否需要生成用户程序的缓存文件，即生成用户程序id
     * @param needCopyOutExe    是否需要生成编译后的用户程序exe文件
     * @param copyOutDir        生成编译后的用户程序exe文件的指定路径
     * @MethodName compile
     * @Description 编译运行
     * @Return
     * @Since 2022/1/3
     */
    public static JSONArray compile(String url,
                                    Long maxCpuTime,
                                    Long maxRealTime,
                                    Long maxMemory,
                                    Long maxStack,
                                    String srcName,
                                    String exeName,
                                    List<String> args,
                                    List<String> envs,
                                    String code,
                                    HashMap<String, String> extraFiles,
                                    Boolean needCopyOutCached,
                                    Boolean needCopyOutExe,
                                    String copyOutDir) throws SystemException {
        JSONObject cmd = new JSONObject();
        cmd.set("args", args);
        cmd.set("env", envs);
        cmd.set("files", COMPILE_FILES);
        // ms-->ns
        maxCpuTime= maxCpuTime ==null?TIME_LIMIT_MS:maxCpuTime;
        cmd.set("cpuLimit", maxCpuTime * 1000 * 1000L);
        cmd.set("clockLimit", maxRealTime * 1000 * 1000L);
        // byte
        cmd.set("memoryLimit", maxMemory);
        cmd.set("procLimit", maxProcessNumber);
        cmd.set("stackLimit", maxStack);

        JSONObject fileContent = new JSONObject();
        fileContent.set("content", code);

        JSONObject copyIn = new JSONObject();
        copyIn.set(srcName, fileContent);

        if (extraFiles != null) {
            for (Map.Entry<String, String> entry : extraFiles.entrySet()) {
                if (!ObjectUtils.isEmpty(entry.getKey()) && !ObjectUtils.isEmpty(entry.getValue())) {
                    JSONObject content = new JSONObject();
                    content.set("content", entry.getValue());
                    copyIn.set(entry.getKey(), content);
                }
            }
        }

        cmd.set("copyIn", copyIn);
        cmd.set("copyOut", new JSONArray().put("stdout").put("stderr"));

        if (needCopyOutCached) {
            cmd.set("copyOutCached", new JSONArray().put(exeName));
        }

        if (needCopyOutExe) {
            cmd.set("copyOutDir", copyOutDir);
        }

        JSONObject param = new JSONObject();
        param.set("cmd", new JSONArray().put(cmd));

        JSONArray result = instance.run("/run", param, url);
        JSONObject compileRes = (JSONObject) result.get(0);
        compileRes.set("originalStatus", compileRes.getStr("status"));
        compileRes.set("status", RESULT_MAP_STATUS.get(compileRes.getStr("status")));
        return result;
    }

    /**
     * @param args          普通评测运行cmd的命令参数
     * @param envs          普通评测运行的环境变量
     * @param maxTime       评测的最大限制时间 ms
     * @param maxOutputSize 评测的最大输出大小 kb
     * @param maxStack      评测的最大限制栈空间 mb
     * @param exeName       评测的用户程序名称
     * @param fileId        评测的用户程序文件id
     * @param fileContent   评测的用户程序文件内容，如果userFileId存在则为null
     * @param isFileIO      是否为文件IO
     * @MethodName testCase
     * @Description 普通评测
     * @Return JSONArray
     * @Since 2022/1/3
     */
    public static JSONArray testCase(String url,
                                     List<String> args,
                                     List<String> envs,
                                     Integer maxTime,
                                     Integer maxMemory,
                                     Integer maxStack,
                                     Integer maxOutputSize,
                                     String exeName,
                                     String fileId,
                                     String fileContent,
                                     Boolean isFileIO
    ) throws SystemException {

        JSONObject cmd = new JSONObject();
        cmd.set("args", args);
        cmd.set("env", envs);

        JSONArray files = new JSONArray();

        JSONObject testCaseInput = new JSONObject();
        if(fileContent==null){
            fileContent="";
        }
        testCaseInput.set("content", fileContent);

        if (BooleanUtils.isFalse(isFileIO)) {
            JSONObject stdout = new JSONObject();
            stdout.set("name", "stdout");
            stdout.set("max", maxOutputSize * 1024);
            files.put(testCaseInput);
            files.put(stdout);
        }
        JSONObject stderr = new JSONObject();
        stderr.set("name", "stderr");
        stderr.set("max", 1024 * 1024 * 16);
        files.put(stderr);

        cmd.set("files", files);

        // ms-->ns
        cmd.set("cpuLimit", maxTime * 1000 * 1000L);
        cmd.set("clockLimit", maxTime * 1000 * 1000L * 3);
        // byte
        cmd.set("memoryLimit", (maxMemory + 100) * 1024 * 1024L);
        cmd.set("procLimit", maxProcessNumber);
        cmd.set("stackLimit", maxStack * 1024 * 1024L);

        JSONObject exeFile = new JSONObject();
        if (!ObjectUtils.isEmpty(fileId)) {
            exeFile.set("fileId", fileId);
        } else {
            exeFile.set("content", fileContent);
        }
        JSONObject copyIn = new JSONObject();
        copyIn.set(exeName, exeFile);

        JSONArray copyOut = new JSONArray();
        copyOut.put("stderr");
        copyOut.put("stdout");
        cmd.set("copyIn", copyIn);
        cmd.set("copyOut", copyOut);

        JSONObject param = new JSONObject();
        param.set("cmd", new JSONArray().put(cmd));

        // 调用判题安全沙箱
        JSONArray result = instance.run("/run", param, url);

        JSONObject testcaseRes = (JSONObject) result.get(0);
        testcaseRes.set("originalStatus", testcaseRes.getStr("status"));
        testcaseRes.set("status", RESULT_MAP_STATUS.get(testcaseRes.getStr("status")));
        return result;
    }


    public JSONArray run(String uri, JSONObject param, String url) throws SystemException {
        // 创建HttpHeaders对象并设置Content-Type为JSON
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 创建HttpEntity对象，将参数param转换为JSON字符串，并将设置的Headers添加到请求中
        HttpEntity<String> request = new HttpEntity<>(JSONUtil.toJsonStr(param), headers);
        log.info(request.toString());
        ResponseEntity<String> postForEntity;
        try {
            // 使用RestTemplate发送POST请求，将响应结果保存在postForEntity中
            postForEntity = restTemplate.postForEntity(url + uri, request, String.class);

            // 解析响应体中的JSON数组并返回
            return JSONUtil.parseArray(postForEntity.getBody());
        } catch (RestClientResponseException ex) {
            // 处理RestClientResponseException异常，通常表示HTTP请求出现问题
            if (ex.getStatusCode() != HttpStatusCode.valueOf(200)) {
                // 如果HTTP状态码不是200，抛出自定义的SystemError异常
                throw new SystemException("Cannot connect to sandbox service.", null, ex.getResponseBodyAsString());
            }
        } catch (Exception e) {
            // 处理其他异常情况
            throw new SystemException("Call SandBox Error.", null, e.getMessage());
        }

        // 如果没有异常抛出，返回null
        return null;
    }

}

