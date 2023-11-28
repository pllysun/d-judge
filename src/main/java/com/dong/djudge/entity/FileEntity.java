package com.dong.djudge.entity;

import lombok.Data;

import java.util.Date;

@Data
public class FileEntity {
    private Integer id;
    private String fileId;
    private String filePath;
    private Date createTime;
}
