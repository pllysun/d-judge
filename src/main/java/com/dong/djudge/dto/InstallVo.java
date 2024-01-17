package com.dong.djudge.dto;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Snowflake;
import lombok.Data;

@Data
public class InstallVo {
    private Long id;
    private String time;
    private String state;
    private String message;

    public InstallVo(String state, String message) {
        this.id = new Snowflake().nextId();
        this.time = DateUtil.format(DateTime.now(), "yyyy-MM-dd HH:mm:ss");
        this.state = state;
        this.message = message;
    }
}
