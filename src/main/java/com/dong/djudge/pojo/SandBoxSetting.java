package com.dong.djudge.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "sandbox_setting")
public class SandBoxSetting {
    @TableId(value = "id", type = IdType.NONE)
    private Long id;
    @TableField(value = "base_url")
    private String baseUrl;
    @TableField(value = "state")
    private Integer state;
    @TableField(value = "create_time")
    private Date createTime;
    @TableField(value = "update_time")
    private Date updateTime;
}
