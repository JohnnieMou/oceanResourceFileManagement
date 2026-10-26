package com.example.pojo.dto.notebook;

import com.example.pojo.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UpdateNotebookDTO extends BaseDTO {

    @NotNull(message = "记事id不能为空")
    @Schema(example = "1")
    Long id;

    @Schema(example = "新示例标题")
    private String newTitle;

    @Schema(example = "新示例内容")
    private String newContent;

}
