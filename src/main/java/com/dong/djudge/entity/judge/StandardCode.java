package com.dong.djudge.entity.judge;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * @author 樊东升
 * @date 2023/11/30 20:45
 */
@Data
public class StandardCode {
    /**
     * 执行标准代码有两种方式：一是使用 "now"，即运行测试集的标准代码；二是使用 "before"，直接通过 codeId 查询先前运行的结果。需要注意的是，使用 "before" 方式会使当前上传的测试集失效，因为 codeId 记录了之前的运行结果和测试集。此时，测试代码将运行先前保存的测试集，而当前上传的测试集如果与之前不同，则不会生效。在 "before" 模式下，测试集的参数应为空。
     */
    private String runCodeType;
    /**
     * 标准代码ID
     */
    private String runCodeId;
    /**
     * 标准代码,标准代码只支持字符串上传，因为标准代码不会太长
     */
    private String runCode;
    /**
     * 标准代码语言
     */
    private String runCodeLanguage;
    /**
     * 代码测试案例的文件类型（Json，URL，FILE）具体参照judge.enums下面的InputFileEnum枚举类
     */
    private String inputFileType;

    /**
     * 代码测试案例的文件内容（根据inputFileType的类型来决定这个，json，url，file）
     */
    private String inputFileContext;
}
