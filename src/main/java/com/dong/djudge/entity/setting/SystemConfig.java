package com.dong.djudge.entity.setting;

import com.dong.djudge.pojo.LanguageConfig;
import lombok.Data;

import java.util.List;

@Data
public class SystemConfig {
    private String baseUrl;
    private String name;
    private Integer state;
    private Integer level;
    private Integer frequency;
    private List<String> languageLabelList;
}
