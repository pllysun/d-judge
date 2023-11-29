package com.dong.djudge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName(value="file")
public class FileEntity {
    @TableId(value = "id",type = IdType.NONE)
    private Long id;
    @TableField(value = "file_id")
    private String fileId;
    @TableField(value = "file_path")
    private String filePath;
    @TableField(value = "create_time",fill= FieldFill.INSERT)
    private Date createTime;

    public FileEntity(String fileId, String filePath) {
        this.fileId = fileId;
        this.filePath = filePath;
    }
}
