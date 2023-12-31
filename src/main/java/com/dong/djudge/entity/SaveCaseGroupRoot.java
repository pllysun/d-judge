package com.dong.djudge.entity;

import lombok.Data;

import java.util.List;

/**
 * @author 樊东升
 * @date 2023/12/7 21:17
 */
@Data
public class SaveCaseGroupRoot {
    /**
     * 测试组群ID
     */
    private int gid;
    /**
     * 测试组内容
     */
    private List<SaveTestCaseGroup> savePut;
}
