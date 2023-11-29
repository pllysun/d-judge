package com.dong.djudge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author 樊东升
 * @date 2023/11/28 22:06
 */
@Data
public class TestGroupFileDTO {
    @NotBlank(message = "参数type不能为空")
    String type;
    String content;
    MultipartFile file;
    String fileId;
}
