package com.dong.djudge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author 阿东
 * @date 2023/9/6 [13:01]
 */
@Data
public class Test {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String test;
}
