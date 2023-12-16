package com.dong.djudge.pojo;

import lombok.Data;

@Data
public class SystemMessage extends AbstractPojo {
    private String cpu;
    private String memory;

    public SystemMessage(String cpu, String memory) {
        this.cpu = cpu;
        this.memory = memory;
    }
}
