package com.dong.djudge.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "language_dictionary")
public class LanguageDictionary {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField(value = "language")
    private String language;

    @TableField(value = "src_path")
    private String srcPath;

    @TableField(value = "exe_path")
    private String exePath;

    @TableField(value = "compile_env")
    private String compileEnv;

    @TableField(value = "compile_command")
    private String compileCommand;

    @TableField(value = "compile_maxCpuTime")
    private String compileMaxCpuTime;

    @TableField(value = "compile_maxRealTime")
    private String compileMaxRealTime;

    @TableField(value = "compile_maxMemory")
    private String compileMaxMemory;

    @TableField(value = "run_env")
    private String runEnv;

    @TableField(value = "run_command")
    private String runCommand;

}
