package com.dong.djudge.entity.setting;

import lombok.Data;

@Data
public class SystemConfig {
    private String baseUrl;
    private String name;
    private Integer state;
    private Integer level;
    private Integer frequency;
}
