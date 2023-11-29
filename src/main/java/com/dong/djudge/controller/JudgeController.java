package com.dong.djudge.controller;

import cn.hutool.json.JSONArray;
import com.dong.djudge.exception.CompileException;
import com.dong.djudge.exception.SystemException;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.enums.ModeEnum;
import com.dong.djudge.service.JudgeService;
import com.dong.djudge.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * 判题服务类
 *
 * @author 阿东
 * @date 2023/9/6 [0:23]
 */
@RestController
public class JudgeController {

    @Autowired
    private JudgeService judgeService;


    /**
     * 判题服务类
     *
     * @param request 请求参数
     */
    @PostMapping(value = "/judge")
    public ResponseResult<Object> submitProblemTestJudge(@RequestBody JudgeRequest request) throws SystemException, CompileException {

        if (request == null || request.getModeType() == null
                || ObjectUtils.isEmpty(request.getCode())
                || ObjectUtils.isEmpty(request.getLanguage())
                || request.getInputFileType() == null
                || request.getInputFileContext() == null) {
            return ResponseResult.failResponse("调用参数错误！请检查您的调用参数！");
        }
        // result为判题结果
        JSONArray result = null;
        Integer code = Objects.requireNonNull(ModeEnum.getTypeByName(request.getModeType())).getCode();
        switch (code) {
            case 0:
                result = judgeService.Judge(request);
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
            default:
                return ResponseResult.failResponse("调用参数错误！请检查您的调用参数！");
        }

        return ResponseResult.successResponse(result);
    }
}
