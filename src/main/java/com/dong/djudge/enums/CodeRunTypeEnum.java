package com.dong.djudge.enums;

import lombok.Getter;

/**
 * @author 樊东升
 * @date 2023/11/30 21:10
 */
@Getter
public enum CodeRunTypeEnum {
    NOW(0, "now"),
    BEFORE(1, "before");

    private final Integer code;
    private final String value;

    CodeRunTypeEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    public CodeRunTypeEnum getCodeByValue(String value) {
        for (CodeRunTypeEnum codeRunTypeEnum : CodeRunTypeEnum.values()) {
            if (codeRunTypeEnum.getValue().equalsIgnoreCase(value)) {
                return codeRunTypeEnum;
            }
        }
        return null;
    }
}
