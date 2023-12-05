package com.dong.djudge.controller;

import com.dong.djudge.dto.standardCodeDTO;
import com.dong.djudge.dto.UpdatestandardCodeDTO;
import com.dong.djudge.service.StandardCodeService;
import com.dong.djudge.util.CommonUtils;
import com.dong.djudge.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author 樊东升
 * @date 2023/11/30 21:39
 */
@RestController
public class StandardCodeController {

    @Autowired
    StandardCodeService standardCodeService;
    @Autowired
    ResourceLoader resourceLoader;

    /**
     * 上传保存标准代码的运行结果
     *
     * @param standardCodeDTO 请求参数
     * @param bindingResult   参数校验结果
     * @return 返回标准代码文件ID
     * @throws Exception 异常
     */
    @PostMapping("/standardCode")
    public ResponseResult<String> StandardCode(@RequestBody @Validated standardCodeDTO standardCodeDTO, BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()) {
            return ResponseResult.failResponse(bindingResult.getFieldErrors().get(0).getDefaultMessage());
        }
        return standardCodeService.standardCodeRun(standardCodeDTO);
    }

    /**
     * 更新标准代码信息.
     *
     * @param standardCodeDTO 要更新的标准代码的DTO对象
     * @param bindingResult   用于验证请求参数的绑定结果
     * @return 包含更新结果的响应对象
     * @throws Exception 如果在更新标准代码信息过程中发生异常
     */
    @PutMapping("/standardCode")
    public ResponseResult<String> standardCode(@RequestBody @Validated UpdatestandardCodeDTO standardCodeDTO, BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()) {
            return ResponseResult.failResponse(bindingResult.getFieldErrors().get(0).getDefaultMessage());
        }
        return standardCodeService.standardCode(standardCodeDTO);
    }

    /**
     * 根据标准代码ID获取标准代码信息，并以JSON格式返回.
     *
     * @param standardCodeId 要获取的标准代码的唯一标识
     * @return 包含标准代码信息的ResponseEntity对象，如果未找到相关信息返回404状态码
     * @throws Exception 如果在获取标准代码信息过程中发生异常
     */
    @GetMapping("/standardCode")
    public ResponseEntity<Object> standardCode(@RequestParam("standardCodeId") String standardCodeId) throws Exception {
        String jsonContent = standardCodeService.getStandardCode(standardCodeId);
        if (jsonContent == null || jsonContent.isBlank()) {
            return ResponseEntity
                    .status(404)
                    .body(null);
        }
        return CommonUtils.getObjectResponseEntity(standardCodeId, jsonContent);
    }



    /**
     * 删除标准代码信息.
     *
     * @param standardCodeId 要删除的标准代码的唯一标识
     * @return 包含删除结果的响应对象
     * @throws Exception 如果在删除标准代码信息过程中发生异常
     */
    @DeleteMapping("/standardCode")
    public ResponseResult<String> deleteStandardCode(@RequestParam("standardCodeId") String standardCodeId) throws Exception {
        return standardCodeService.deleteStandardCode(standardCodeId);
    }


}
