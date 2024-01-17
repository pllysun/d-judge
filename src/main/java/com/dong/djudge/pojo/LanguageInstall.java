package com.dong.djudge.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value = "language_install")
public class LanguageInstall {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "language")
    private String language;

    @TableField(value = "package_name")
    private String packageName;
}
