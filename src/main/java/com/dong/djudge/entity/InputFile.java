package com.dong.djudge.entity;

import lombok.Data;

import java.util.List;

/**
 * @author 阿东
 * @since 2023/9/7 [17:42]
 */
@Data
public class InputFile {
    private List<TestCase> testCases;
}
