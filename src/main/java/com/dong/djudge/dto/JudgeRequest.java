package com.dong.djudge.dto;

import com.dong.djudge.entity.judge.CodeSetting;
import com.dong.djudge.entity.judge.StandardCode;
import jakarta.validation.constraints.NotNull;
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
     * 评测的类型(0-OI,1-OJ)
     */
    private String modeType;
    /**
     * 评测的代码
     */
    @NotNull(message = "code不能为空")
    private String code;
    /**
     * 评测的语言
     */
    @NotNull(message = "language不能为空")
    private String language;
    /**
     * 测试测试集按照单个测试来计算时间和内存还是总的测试集来计算时间和内存（默认:true）
     */
    private Boolean isSingleTest;
    /**
     * 当且仅当评测类型为OI时，这个字段为必填。其余都不需要填写
     */
    private String oiString;
    /**
     * 标准代码
     */
    private StandardCode standardCode;
    /**
     * 代码相关设置
     */
    CodeSetting codeSetting;
    public JudgeRequest(String code, String language) {
        this.code = code;
        this.language = language;
    }
}
