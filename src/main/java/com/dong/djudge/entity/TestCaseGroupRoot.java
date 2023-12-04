package com.dong.djudge.entity;

import lombok.Data;

import java.util.List;

/**
 * @author 樊东升
 * @date 2023/12/4 20:19
 */
@Data
public class TestCaseGroupRoot {
    private int gid;
    private List<TestCaseGroup> input;
}
