package com.dong.djudge.enums;

import lombok.Getter;

@Getter
public enum SettingEnum {
    DEFAULT("Default", 0),
    SERVER_URL("SERVER_URL", 1);

    private final String key;
    private final Integer code;

    SettingEnum(String key, Integer code) {
        this.key = key;
        this.code = code;
    }

    public static SettingEnum getByKey(String key) {
        for (SettingEnum settingEnum : SettingEnum.values()) {
            if (settingEnum.getKey().equalsIgnoreCase(key)) {
                return settingEnum;
            }
        }
        return DEFAULT;
    }
}
