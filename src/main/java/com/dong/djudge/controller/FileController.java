package com.dong.djudge.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 樊东升
 * @date 2023/11/27 22:38
 */
@Slf4j
@RestController()
public class FileController {

    /**
     * 上传文件
     * @param type 类型，有两种，一种是测试Json，一种是测试Json的网络URL
     * @param file 具体内容
     */
    @PutMapping(value = "/file")
    public void uploadFile(String type,String file){

    }
}
