package com.dong.djudge.enums;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
public enum UpLoadFileEnum {
    JSON(0, "Json"),
    OSS(1, "OSS"),
    FILE(2, "File");
    private final Integer code;
    private final String value;

    UpLoadFileEnum(int code, String value) {
        this.code = code;
        this.value = value;
    }

    public static UpLoadFileEnum getTypeByValue(String value) {
        for (UpLoadFileEnum upLoadFileEnum : UpLoadFileEnum.values()) {
            if (upLoadFileEnum.getValue().equalsIgnoreCase(value)) {
                return upLoadFileEnum;
            }
        }
        return null;
    }
}
