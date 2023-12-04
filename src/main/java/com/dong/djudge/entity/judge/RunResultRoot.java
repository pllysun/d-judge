package com.dong.djudge.entity.judge;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 樊东升
 * @date 2023/12/4 19:30
 */
@Data
public class RunResultRoot {
    Map<Integer, Map<Integer, RunResult>> runResult;
    String errorInfo;
    String state;

    public RunResultRoot() {
    }

    public RunResultRoot(Integer gid, Integer id, RunResultForTestGroup runResult) {
        if (this.runResult == null) {
            this.runResult = new HashMap<>();
        }
        Map<Integer, RunResult> orDefault = this.runResult.getOrDefault(gid, new HashMap<>());
        RunResult rR = new RunResult(runResult);
        orDefault.put(id, rR);
        this.runResult.put(gid, orDefault);
    }

    public RunResultRoot(Integer gid, Integer id, RunResult runResult) {
        if (this.runResult == null) {
            this.runResult = new HashMap<>();
        }
        Map<Integer, RunResult> orDefault = this.runResult.getOrDefault(gid, new HashMap<>());
        orDefault.put(id, runResult);
        this.runResult.put(gid, orDefault);
    }


    public void setRunResult(Integer gid, Integer id, RunResultForTestGroup runResult) {
        Map<Integer, RunResult> orDefault = this.runResult.getOrDefault(gid, new HashMap<>());
        RunResult rR = new RunResult(runResult);
        orDefault.put(id, rR);
        this.runResult.put(gid, orDefault);
    }

    public void setRunResult(List<RunResultForTestGroup> runResults) {
        if (this.runResult == null) {
            this.runResult = new HashMap<>();
        }
        for (RunResultForTestGroup result : runResults) {
            Map<Integer, RunResult> orDefault = this.runResult.getOrDefault(result.getGid(), new HashMap<>());
            RunResult rR = new RunResult(result);
            orDefault.put(result.getId(), rR);
            this.runResult.put(result.getGid(), orDefault);
        }
    }
}
