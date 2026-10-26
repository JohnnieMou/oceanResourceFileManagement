package com.example.pojo.dto.notebook;

import com.example.pojo.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class AddNotebookDTO extends BaseDTO {

    @NotBlank(message = "标题不能为空")
    @Schema(example = "示例标题")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Schema(example = "示例内容")
    private String content;
}
