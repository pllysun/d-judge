package com.dong.djudge.entity;

import lombok.Data;

import java.util.List;

/**
 * @author 阿东
 * @since 2023/9/7 [19:34]
 */
@Data
public class TestCase {
    /**
     * 测试用例组ID
     */
    private Integer id;
    /**
     * 测试用例组名称
     */
    private String group;
    /**
     * 测试输入组内容
     */
    private List<String> input;
}
