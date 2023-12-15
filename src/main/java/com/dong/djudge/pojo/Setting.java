package com.dong.djudge.pojo;


import lombok.Data;


@Data
public class Setting  extends AbstractPojo{

    private String key;

    private String value;

    public Setting(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public Setting() {
    }
}
