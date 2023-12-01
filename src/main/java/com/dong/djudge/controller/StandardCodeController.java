package com.dong.djudge.controller;

import com.dong.djudge.dto.StaredCodeDTO;
import com.dong.djudge.service.StandardCodeService;
import com.dong.djudge.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 樊东升
 * @date 2023/11/30 21:39
 */
@RestController
public class StandardCodeController {

    @Autowired
    StandardCodeService standardCodeService;

    @PostMapping("/runStaredCode")
    public ResponseResult<String> runStaredCode(@RequestBody @Validated StaredCodeDTO staredCodeDTO, BindingResult bindingResult) throws Exception {
        if(bindingResult.hasErrors()){
            return ResponseResult.failResponse(bindingResult.getFieldErrors().get(0).getDefaultMessage());
        }
        standardCodeService.standardCodeRun(staredCodeDTO);
        return null;
    }

}
