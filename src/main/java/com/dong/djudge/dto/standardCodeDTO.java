package com.dong.djudge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class standardCodeDTO {
    @NotBlank(message = "code不能为空")
    private String code;
    @NotBlank(message = "语言不能为空")
    private String language;
    @NotBlank(message = "测试集不能为空")
    private String testGroupId;

    public standardCodeDTO() {
    }

    public standardCodeDTO(UpdatestandardCodeDTO updatestandardCodeDTO) {
        this.code = updatestandardCodeDTO.getCode();
        this.language = updatestandardCodeDTO.getLanguage();
        this.testGroupId = updatestandardCodeDTO.getTestGroupId();
    }
}
