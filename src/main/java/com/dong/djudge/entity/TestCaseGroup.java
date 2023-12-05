package com.dong.djudge.entity;

import lombok.Data;

import java.util.List;

/**
 * @author 阿东
 * @since 2023/9/7 [19:34]
 */
@Data
public class TestCaseGroup {
    /**
     * 测试ID
     */
    private int id;
    /**
     * 测试输入值
     */
    private String value;
}
