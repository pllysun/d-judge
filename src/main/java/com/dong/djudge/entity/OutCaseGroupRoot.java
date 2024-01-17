package com.dong.djudge.entity;

import lombok.Data;

import java.util.List;

/**
 * @author 樊东升
 * @date 2023/12/7 19:47
 */
@Data
public class OutCaseGroupRoot {
    /**
     * 测试组群ID
     */
    private int gid;
    /**
     * 测试组内容
     */
    private List<OutTestCaseGroup> output;

    /**
     * 测试组群是否通过
     */
    private boolean isGroupAccepted = true;

    /**
     * 测试组群状态（总时间，总内存）
     */
    private OutCaseResult groupResult;
}
