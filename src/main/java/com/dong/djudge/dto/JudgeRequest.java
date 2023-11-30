package com.dong.djudge.dto;

import com.dong.djudge.entity.judge.CodeSetting;
import com.dong.djudge.entity.judge.StandardCode;
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
     * 评测的类型(0-OI,1-OJ,2-ACM,3-CODE)
     */
    private String modeType;
    /**
     * 评测的代码
     */
    private String code;
    /**
     * 评测的语言
     */
    private String language;
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
