package com.dong.djudge.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "sandbox_setting")
public class SandBoxSetting extends AbstractPojo {
    @TableField(value = "base_url")
    private String baseUrl;
    @TableField(value = "state")
    private Integer state;
    @TableField(value = "level")
    private Integer level;
    @TableField(value = "frequency")
    private Integer frequency;

    public SandBoxSetting(String baseUrl) {
        this.baseUrl = baseUrl;
        this.state=1;
        this.level=8;
        this.frequency=0;
    }

    public SandBoxSetting() {
    }
}
