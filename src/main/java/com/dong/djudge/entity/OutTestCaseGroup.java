package com.dong.djudge.entity;

import lombok.Data;

/**
 * @author 阿东
 * @since 2023/9/7 [19:34]
 */
@Data
public class OutTestCaseGroup {
    /**
     * 测试ID
     */
    private int id;
    /**
     * 测试输入值
     */
    private String input;

    /**
     * 测试输出值
     */
    private String taOutput;

    /**
     * 标准值
     */
    private String saOutput;

    /**
     * 该测试是否通过
     */
    private boolean isAccepted = true;

    /**
     * 测试用例状态
     */
    private OutCaseResult caseResult;
}
