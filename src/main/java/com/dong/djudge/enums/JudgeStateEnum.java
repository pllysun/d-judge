package com.dong.djudge.enums;

import lombok.Getter;

/**
 * @author 樊东升
 * @date 2023/12/7 17:09
 */

@Getter
public enum JudgeStateEnum {
    ACCEPTED("Accepted"), // normal
    WRONG_ANSWER("Wrong Answer"), // wa
    MEMORY_LIMIT_EXCEEDED("Memory Limit Exceeded"), // mle
    TIME_LIMIT_EXCEEDED("Time Limit Exceeded"), // tle
    OUTPUT_LIMIT_EXCEEDED("Output Limit Exceeded"), // ole
    FILE_ERROR("File Error"), // fe
    NONZERO_EXIT_STATUS("Nonzero Exit Status"),
    SIGNALED("Signalled"),
    INTERNAL_ERROR("Internal Error"); // system error

    private final String description;

    JudgeStateEnum(String description) {
        this.description = description;
    }


}