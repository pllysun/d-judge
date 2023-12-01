package com.dong.djudge.enums;

import lombok.Getter;

/**
 * @author 阿东
 * @since 2023/9/7 [3:19]
 */
@Getter
public enum InputFileEnum {
    /*
     * 测试案例文件传输方式
     * 三种方式就是从测试小文本到中文本到大文本，大文本因为网络传输的原因还是很耗费时间，所以直接存到本地来读取本地的Json文件，
     * 前两种只能通过接口传递，第三种提供直接的接口来传输然后返回文本地址，调用接口时直接传入文件地址即可
     */
    /**
     * Json文本，所以的测试案例全部通过Json格式书写，最普通的方式便是把Json直接当做变量传过来，适合于普通测试测试数据量比较小的情况下。
     */
    JSON(0,"JSON"),
    /**
     * 对象存储，Object Storage Service,传过来一个链接，下载对象储存文件然后解析文件里面的Json文本，适合于第一次运行大数据的测试文本以及中小测试文本
     */
    URL(1,"URL"),
    /**
     * 本地文件，直接读取本地的Json文本，适用于多次读取以及大型测试文本
     */
    FILE(2,"FILE");


    /**
     * 文件方式
     */
    private final Integer code;
    /**
     * 文件内容
     */
    private final String value;
    InputFileEnum(int code, String value) {
        this.code=code;
        this.value=value;
    }

    public static InputFileEnum getTypeByCode(int code) {
        for (InputFileEnum inputFileEnum : InputFileEnum.values()) {
            if (inputFileEnum.getCode() == code) {
                return inputFileEnum;
            }
        }
        return null;
    }

    public static InputFileEnum getTypeByValue(String value) {
        for (InputFileEnum inputFileEnum : InputFileEnum.values()) {
            if (inputFileEnum.getValue().equalsIgnoreCase(value)) {
                return inputFileEnum;
            }
        }
        return null;
    }
}
