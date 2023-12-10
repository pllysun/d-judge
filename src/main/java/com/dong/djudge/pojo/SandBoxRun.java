package com.dong.djudge.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class SandBoxRun {
    private Long id;
    private String fileId;
    private String baseUrl;
    private Date createTime;
    private Date updateTime;
}
