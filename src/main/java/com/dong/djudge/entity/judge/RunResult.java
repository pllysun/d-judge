package com.dong.djudge.entity.judge;

import lombok.Data;

@Data
public class RunResult {
    private long exitStatus;
    private RunResultFile files;
    private long memory;
    private String originalStatus;
    private long runTime;
    private long status;
    private long time;
}
