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

    public RunResult() {
    }

    public RunResult(RunResultForTestGroup runResultForTestGroup) {
        this.exitStatus = runResultForTestGroup.getExitStatus();
        this.files = runResultForTestGroup.getFiles();
        this.memory = runResultForTestGroup.getMemory();
        this.originalStatus = runResultForTestGroup.getOriginalStatus();
        this.runTime = runResultForTestGroup.getRunTime();
        this.status = runResultForTestGroup.getStatus();
        this.time = runResultForTestGroup.getTime();
    }
}
