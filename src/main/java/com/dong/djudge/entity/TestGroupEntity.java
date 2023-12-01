package com.dong.djudge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value="test_group")
public class TestGroupEntity {
    @TableId(value = "id",type = IdType.NONE)
    private Long id;
    @TableField(value = "test_group_id")
    private String testGroupId;
    @TableField(value = "test_group_path")
    private String testGroupPath;
    @TableField(value = "create_time")
    private Date createTime;
    @TableField(value = "update_time")
    private Date updateTime;

    public TestGroupEntity(String testGroupId, String testGroupPath) {
        this.testGroupId = testGroupId;
        this.testGroupPath = testGroupPath;
    }
}
