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
    Integer maxTime;
    /**
     * 最大运行内存
     */
    Integer maxMemory;
    /**
     * 最大栈大小
     */
    Integer maxStack;
    /**
     * 最大输出大小
     */
    Integer maxOutput;
}
