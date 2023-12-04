package com.dong.djudge.entity.judge;

import lombok.Data;

/**
 * @author 樊东升
 * @date 2023/11/30 20:53
 */
@Data
public class CodeSetting {
    /**
     * 最大运行时间
     */
    Long maxTime;
    /**
     * 最大运行内存
     */
    Long maxMemory;
    /**
     * 最大栈大小
     */
    Long maxStack;
    /**
     * 最大输出大小
     */
    Integer maxOutput;
}
