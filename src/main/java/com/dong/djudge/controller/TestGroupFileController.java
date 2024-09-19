package com.dong.djudge.controller;

import com.dong.djudge.dto.TestGroupFileDTO;
import com.dong.djudge.enums.UpLoadFileEnum;
import com.dong.djudge.service.TestGroupFileService;
import com.dong.djudge.util.CommonUtils;
import com.dong.djudge.util.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
        log.info("开始上传文件，type: {}, content: {}", testGroupFileDTO.getType(), testGroupFileDTO.getContent());

        // 检查绑定结果
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            log.warn("字段校验错误: {}", errorMessage);
            return ResponseResult.failResponse(errorMessage);
        }

        // 获取类型码
        Integer code;
        try {
            code = Objects.requireNonNull(UpLoadFileEnum.getTypeByValue(testGroupFileDTO.getType())).getCode();
            log.info("文件类型代码: {}", code);
        } catch (NullPointerException e) {
            log.error("文件类型无效，type: {}", testGroupFileDTO.getType(), e);
            return ResponseResult.failResponse("type参数仅支持:JSON,URL,FILE");
        }

        // 校验 content 参数
        if ((code == 0 || code == 1) && (testGroupFileDTO.getContent() == null || testGroupFileDTO.getContent().isBlank())) {
            log.warn("content参数为空");
            return ResponseResult.failResponse("content参数为空，请检查！");
        }

        String fileId;
        try {
            switch (code) {
                case 0:
                    // JSON类型处理
                    if (!CommonUtils.isValidJson(testGroupFileDTO.getContent())) {
                        log.warn("无效的 JSON 内容: {}", testGroupFileDTO.getContent());
                        return ResponseResult.failResponse("无效的 JSON 内容！");
                    }
                    fileId = handleFileUploadOrUpdate(isUpload, testGroupFileDTO, testGroupFileDTO.getContent());
                    break;

                case 1:
                    // URL类型处理
                    String jsonContent = CommonUtils.getJsonForURL(testGroupFileDTO.getContent());
                    if (jsonContent == null) {
                        log.warn("无效的URL或URL内容无法解析: {}", testGroupFileDTO.getContent());
                        return ResponseResult.failResponse("无效的URL或URL内容无法解析！");
                    }
                    fileId = handleFileUploadOrUpdate(isUpload, testGroupFileDTO, jsonContent);
                    break;

                case 2:
                    // 文件类型处理
                    fileId = handleFileUploadForFileType(testGroupFileDTO, isUpload);
                    break;

                default:
                    log.error("调用参数错误，type: {}", testGroupFileDTO.getType());
                    return ResponseResult.failResponse("调用参数错误！请检查您的调用参数！");
            }
        } catch (Exception e) {
            log.error("文件上传或更新失败！", e);
            return ResponseResult.failResponse("文件上传或更新失败！");
        }

        log.info("文件上传成功，fileId: {}", fileId);
        return new ResponseResult<>(200, "上传成功", fileId);
    }

    private String handleFileUploadOrUpdate(boolean isUpload, TestGroupFileDTO testGroupFileDTO, String content) throws Exception {
        if (isUpload) {
            log.info("执行文件上传操作...");
            return testGroupFileService.uploadFile(content);
        } else {
            log.info("执行文件更新操作，fileId: {}", testGroupFileDTO.getFileId());
            testGroupFileService.updateFile(testGroupFileDTO.getFileId(), content);
            return null;
        }
    }

    private String handleFileUploadForFileType(TestGroupFileDTO testGroupFileDTO, boolean isUpload) throws Exception {
        if (testGroupFileDTO.getFile() == null) {
            log.warn("当参数type=file时，file参数不能为空");
            throw new IllegalArgumentException("当参数type=file时，file参数不能为空");
        }

        String originalFilename = testGroupFileDTO.getFile().getOriginalFilename();
        log.info("处理文件上传, 文件名: {}", originalFilename);

        String fileType = new Tika().detect(originalFilename);
        log.info("检测到的文件类型: {}", fileType);

        if (!"application/json".equals(fileType)) {
            log.warn("文件类型不是 JSON，文件名: {}", originalFilename);
            throw new IllegalArgumentException("文件类型不是 JSON，请检查您的文件格式！");
        }

        String json = new String(testGroupFileDTO.getFile().getBytes());
        if (!CommonUtils.isValidJson(json)) {
            log.warn("无效的 JSON 文件内容: {}", originalFilename);
            throw new IllegalArgumentException("无效的 JSON 内容！");
        }

        return handleFileUploadOrUpdate(isUpload, testGroupFileDTO, json);
    }



    /**
     * 下载文件
     *
     * @param fileId 文件id
     * @return json文件
     */
    @GetMapping(value = "/downLoadFile")
    public ResponseEntity<Object> getFile(String fileId) throws Exception {
        String jsonContent = testGroupFileService.getFile(fileId);
        if (jsonContent == null || jsonContent.isBlank()) {
            return ResponseEntity
                    .status(404)
                    .body(null);
        }
        return CommonUtils.getObjectResponseEntity(fileId, jsonContent);
    }

    /**
     * 修改文件
     *
     * @param testGroupFileIdDTO 修改文件的相关内容
     * @param bindingResult      参数校验
     * @return 响应信息
     */
    @PutMapping(value = "/updateFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
    public ResponseResult<String> deleteFile(String fileId) throws Exception {
        if (fileId == null || fileId.isBlank()) {
            return ResponseResult.failResponse("fileId参数不能为空");
        }
        try {
            testGroupFileService.deleteFile(fileId);
        } catch (IOException e) {
            log.warn("删除测试集出问题:{}", e.getMessage());
            return ResponseResult.failResponse("删除文件出错，文件可能不存在或fileId出错");
        }
        return ResponseResult.successResponse("删除成功");
    }
}
