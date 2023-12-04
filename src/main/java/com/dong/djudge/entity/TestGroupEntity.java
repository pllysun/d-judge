package com.dong.djudge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "test_group")
public class TestGroupEntity {
    @TableId(value = "id", type = IdType.NONE)
    private Long id;
    @TableField(value = "test_group_id")
    private String testGroupId;
    @TableField(value = "create_time")
    private Date createTime;
    @TableField(value = "update_time")
    private Date updateTime;

    public TestGroupEntity(String testGroupId) {
        this.testGroupId = testGroupId;
    }
}
