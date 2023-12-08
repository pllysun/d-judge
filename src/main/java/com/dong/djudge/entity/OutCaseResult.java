package com.dong.djudge.entity;

import lombok.Data;

@Data
public class OutCaseResult {
    /**
     * cpu运行时间，单位毫秒
     */
    private long time;
    /**
     * 程序运行时间，单位毫秒
     */
    private long runTime;
    /**
     * 程序运行内存，单位KB
     */
    private long memory;

    public OutCaseResult(Long time, Long runTime, Long memory) {
        this.time = time;
        this.runTime = runTime;
        this.memory = memory;
    }
    public OutCaseResult() {
    }
}
