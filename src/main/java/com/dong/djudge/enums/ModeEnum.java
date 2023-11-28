package com.dong.djudge.enums;

import lombok.Getter;

/**
 * @author 阿东
 * @since 2023/9/7 [3:12]
 */
@Getter
public enum ModeEnum {
    /*
      评测模式
     */
    /**
     * OI模式(写代码模式)
     */
    OI(0, "IO"),
    /**
     * ACM模式(普通模式)
     */
    OJ(1, "OJ"),
    /**
     * ACM模式(比赛模式)
     */
    ACM(2, "ACM"),
    /**
     * 核心代码模式
     */
    CODE(3, "CODE"),
    /**
     * 软协模式(中南林业科技大学软件协会专用模式，高定制化)
     */
    SAP(4, "SAP");


    private final Integer code;
    private final String name;

    ModeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }


    public static ModeEnum getTypeByName(String name) {
        for (ModeEnum modeEnum : ModeEnum.values()) {
            if (modeEnum.getName().equalsIgnoreCase(name)) {
                return modeEnum;
            }
        }
        return null;
    }
}
