package com.dong.djudge.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "language_install_logs")
public class LanguageInstallLogs extends AbstractPojo {
    @TableField(value = "server_id")
    private Long serverId;
    @TableField(value = "message")
    private String message;
    @TableField(value = "level")
    private Integer level;
}
