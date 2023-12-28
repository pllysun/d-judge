package com.dong.djudge.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sandbox_run")
public class SandBoxRun extends AbstractPojo {
    private String fileId;
    private String baseUrl;

    public SandBoxRun( String fileId, String baseUrl) {
        this.fileId = fileId;
        this.baseUrl = baseUrl;
    }
}
