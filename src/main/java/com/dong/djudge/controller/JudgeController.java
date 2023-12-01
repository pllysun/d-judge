package com.dong.djudge.controller;

import cn.hutool.json.JSONArray;
import com.dong.djudge.exception.CompileException;
import com.dong.djudge.exception.SystemException;
import com.dong.djudge.dto.JudgeRequest;
import com.dong.djudge.enums.ModeEnum;
import com.dong.djudge.service.JudgeService;
import com.dong.djudge.util.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Qualifier("JudgeServiceOiImpl")
    private JudgeService oiJudgeService;

    @Autowired
    @Qualifier("JudgeServiceOjImpl")
    private JudgeService ojJudgeService;

    @Autowired
    @Qualifier("JudgeServiceAcmImpl")
    private JudgeService acmJudgeService;

    @Autowired
    @Qualifier("JudgeServiceCodeImpl")
    private JudgeService codeJudgeService;




    /**
     * 判题服务类
     *
     * @param request 请求参数
     */
    @PostMapping(value = "/judge")
    public ResponseResult<Object> submitProblemTestJudge(@RequestBody JudgeRequest request) throws Exception {

        if (request == null || request.getModeType() == null
                || ObjectUtils.isEmpty(request.getCode())
                || ObjectUtils.isEmpty(request.getLanguage())) {
            return ResponseResult.failResponse("调用参数错误！请检查您的调用参数！");
        }
        // result为判题结果
        ModeEnum typeByName = ModeEnum.getTypeByName(request.getModeType());
        if(typeByName== null){
            return ResponseResult.failResponse("未知modeType类型");
        }
        Integer code = typeByName.getCode();
        switch (code) {
            case 0:
                return oiJudge(request);
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

        return ResponseResult.exceptionError("异常错误");
    }

    private ResponseResult<Object> oiJudge(JudgeRequest request) throws Exception {
        return oiJudgeService.Judge(request);
    }
}
