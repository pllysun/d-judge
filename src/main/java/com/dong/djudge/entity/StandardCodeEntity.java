package com.dong.djudge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName(value = "standard_code")
public class StandardCodeEntity {
    @TableId(value = "id", type = IdType.NONE)
    private Long id;
    private String codeId;
    private String testGroupId;
    private Timestamp createTime;
    private Timestamp updateTime;

}
