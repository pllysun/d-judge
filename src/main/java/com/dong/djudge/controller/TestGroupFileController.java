package com.dong.djudge.controller;

import com.dong.djudge.dto.TestGroupFileDTO;
import com.dong.djudge.enums.UpLoadFileEnum;
import com.dong.djudge.service.TestGroupFileService;
import com.dong.djudge.util.JsonUtils;
import com.dong.djudge.util.ResponseResult;
import com.dong.djudge.util.TestGroupUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * 测试集上传服务类
 *
 * @author 樊东升
 * @date 2023/11/27 22:38
 */
@Slf4j
@RestController()
public class TestGroupFileController {

    @Autowired
    private TestGroupFileService testGroupFileService;

    /**
     * 上传文件
     * 这个接口主要用于上传测试集，当测试集过大或者出于其他目的，我们需要将测试集提前上传上去，那么就需要调用这个接口\n
     * 这个接口支持三种方式上传测试集
     * 1、JSON，通过JSON直接将测试集上传服务器本地
     * 2、URL，通过URL，一般来说也就是对象存储的URL，在服务器下载测试集然后维护这个测试集
     * 3、文件，通过文件上传测试集
     * 这三种方式都是通过type来区分的，type有三种，分别是JSON，URL，file，不区分大小写
     * 并且测试集要满足一定的格式，具体的测试集格式请参考文档
     * type    文件类型，有两种，一种是测试Json的网络URL，一种是测试文件
     * content 文件具体内容，但type=file时，这个参数可以为空
     * file    type为file时这个参数才有效件,仅支持扩展名为.json的文件
     * 参数类型有三种，一种是测试Json，一种是测试Json的网络URL，一种是测试文件
     */
    @PostMapping(value = "/uploadFile")
    public ResponseResult<String> uploadFile(@ModelAttribute @Validated @RequestBody TestGroupFileDTO testGroupFileDTO, BindingResult bindingResult) throws Exception {
        return uploadFile(testGroupFileDTO, bindingResult, true);
    }

    private ResponseResult<String> uploadFile(TestGroupFileDTO testGroupFileDTO, BindingResult bindingResult, boolean isUpload) throws Exception {
        log.info("type:{}, content:{}", testGroupFileDTO.getType(), testGroupFileDTO.getContent());
        if (bindingResult.hasErrors()) {
            List<FieldError> fieldErrors = bindingResult.getFieldErrors();
            return ResponseResult.failResponse(fieldErrors.get(0).getDefaultMessage());
        }
        // 获取类型码
        Integer code;
        try {
            code = Objects.requireNonNull(UpLoadFileEnum.getTypeByValue(testGroupFileDTO.getType())).getCode();
        } catch (NullPointerException e) {
            return ResponseResult.failResponse("type参数仅支持:JSON,URL,FILE");
        }
        if (code == 0 || code == 1) {
            if (testGroupFileDTO.getContent() == null || testGroupFileDTO.getContent().isBlank()) {
                return ResponseResult.failResponse("content参数为空，请检查！");
            }
        }
        String fileId;
        switch (code) {
            case 0:
                // 检查 JSON 是否有效
                if (JsonUtils.isValidJson(testGroupFileDTO.getContent())) {
                    return ResponseResult.failResponse("无效的 JSON 内容！");
                }
                if (isUpload) {
                    fileId = testGroupFileService.uploadFile(testGroupFileDTO.getContent());
                } else {
                    testGroupFileService.updateFile(testGroupFileDTO.getFileId(), testGroupFileDTO.getContent());
                    return ResponseResult.successResponse("更新成功");
                }
                break;
            case 1:
                String jsonContent;
                jsonContent = TestGroupUtils.getJsonForURL(testGroupFileDTO.getContent());
                if(jsonContent==null){
                    return ResponseResult.failResponse("无效的URL或URL内容无法解析！");
                }
                // 处理JSON内容或调用fileService.uploadFile方法
                if (isUpload) {
                    fileId = testGroupFileService.uploadFile(jsonContent);
                } else {
                    testGroupFileService.updateFile(testGroupFileDTO.getFileId(), jsonContent);
                    return ResponseResult.successResponse("更新成功");
                }
                break;
            case 2:
                // 处理类型为2的情况
                try {
                    if (testGroupFileDTO.getFile() == null) {
                        return ResponseResult.failResponse("当参数type=file时，file参数不能为空");
                    }
                    log.info("file:{}", testGroupFileDTO.getFile().getOriginalFilename());
                    // 使用 Apache Tika 检测文件类型
                    String fileType = new Tika().detect(testGroupFileDTO.getFile().getOriginalFilename());
                    System.out.println(fileType);
                    // 检查文件类型是否为 JSON
                    if (!"application/json".equals(fileType)) {
                        return ResponseResult.failResponse("文件类型不是 JSON，请检查您的文件格式！");
                    }
                    String json = new String(testGroupFileDTO.getFile().getBytes());
                    if (JsonUtils.isValidJson(json)) {
                        return ResponseResult.failResponse("无效的 JSON 内容！");
                    }
                    if (isUpload) {
                        fileId = testGroupFileService.uploadFile(json);
                    } else {
                        testGroupFileService.updateFile(testGroupFileDTO.getFileId(), json);
                        return ResponseResult.successResponse("更新成功");
                    }
                } catch (IOException e) {
                    log.error("文件类型检测失败！", e);
                    return ResponseResult.failResponse("文件类型检测失败！");
                }
                break;
            default:
                return ResponseResult.failResponse("调用参数错误！请检查您的调用参数！");
        }
        // 返回成功的结果
        return new ResponseResult<>(200, "上传成功", fileId);
    }


    /**
     * 下载文件
     *
     * @param fileId 文件id
     * @return json文件
     */
    @GetMapping(value = "/downLoadFile")
    public ResponseResult<String> getFile(String fileId) {
        if (fileId == null || fileId.isBlank()) {
            return ResponseResult.failResponse("fileId参数不能为空");
        }
        String Json = testGroupFileService.getFile(fileId);
        return new ResponseResult<>(200, "获取成功", Json);
    }

    /**
     * 修改文件
     *
     * @param testGroupFileIdDTO 修改文件的相关内容
     * @param bindingResult      参数校验
     * @return 响应信息
     */
    @PutMapping(value = "/updateFile" ,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseResult<String> updateFile(@ModelAttribute @Validated TestGroupFileDTO testGroupFileIdDTO, BindingResult bindingResult) throws Exception {
        if (testGroupFileIdDTO.getFileId() == null) {
            return ResponseResult.failResponse("fileId不能为空");
        }
        return uploadFile(testGroupFileIdDTO, bindingResult, false);
    }


    /**
     * 删除文件
     *
     * @param fileId 文件ID
     * @return 响应信息
     */
    @DeleteMapping(value = "/DeleteFile")
    public ResponseResult<String> deleteFile(String fileId) {
        if (fileId == null || fileId.isBlank()) {
            return ResponseResult.failResponse("fileId参数不能为空");
        }
        try {
            testGroupFileService.deleteFile(fileId);
        } catch (IOException e) {
            log.warn("删除测试集出问题:{}",e.getMessage());
            return ResponseResult.failResponse("删除文件出错，文件可能不存在或fileId出错");
        }
        return ResponseResult.successResponse("删除成功");
    }
}
