package com.dong.djudge.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;


/**
 * @author ADong
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class JudgeRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 666L;
    /**
     * 评测的类型(0-OI,1-OJ,2-ACM,3-CODE,4-SAP)
     */
    private Integer testType;

    /**
     * 评测的代码
     */
    private String code;

    /**
     * 评测的语言
     */
    private String language;

    /**
     * 代码测试案例的文件类型（0-Json，1-OSS，2-LocalFile）具体参照judge.enums下面的InputFileEnum枚举类
     */
    private Integer inputFileType;

    /**
     * 代码测试案例的文件内容（根据inputFileType的类型来决定这个，0是json文本，1是网址，2是文件地址）
     */
    private String inputFileContext;

    /**
     * 最大运行时间
     */
    Long maxTime ;
    /**
     * 最大运行内存
     */
    Long maxMemory ;
    /**
     * 最大栈大小
     */
    Long maxStack ;
    /**
     * 最大输出大小
     */
    Integer maxOutput ;

}
