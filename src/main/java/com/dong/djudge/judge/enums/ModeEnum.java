package com.dong.djudge.judge.enums;

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
    OI(0, "OI"),
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


    private final Integer mode;
    private final String name;

    ModeEnum(Integer mode, String name) {
        this.mode = mode;
        this.name = name;
    }

    public static ModeEnum getTypeByMode(int mode) {
        for (ModeEnum modeEnum : ModeEnum.values()) {
            if (modeEnum.getMode() == mode) {
                return modeEnum;
            }
        }
        return null;
    }
}
