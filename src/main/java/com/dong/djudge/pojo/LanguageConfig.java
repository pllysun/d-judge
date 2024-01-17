package com.dong.djudge.pojo;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "language_config")
public class LanguageConfig {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "server_id")
    private Long serverId;

    @TableField(value = "language_id")
    private String languageId;

    @TableField(value = "create_time")
    private Date createTime;

    @TableField(value = "update_time")
    private Date updateTime;

    public LanguageConfig(Long serverId, String languageId) {
        this.id=new Snowflake().nextId();
        this.serverId = serverId;
        this.languageId = languageId;
        this.createTime = new Date();
        this.updateTime = new Date();
    }
}
