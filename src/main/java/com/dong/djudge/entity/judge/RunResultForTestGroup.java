package com.dong.djudge.entity.judge;

import lombok.Data;

/**
 * @author 樊东升
 * @date 2023/12/4 20:47
 */
@Data
public class RunResultForTestGroup {
    private Integer gid;
    private Integer id;
    private long exitStatus;
    private RunResultFile files;
    private long memory;
    private String originalStatus;
    private long runTime;
    private long status;
    private long time;
}
