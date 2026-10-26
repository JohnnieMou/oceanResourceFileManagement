package com.example.pojo.dto.field;

import com.example.pojo.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class DeleteFieldDTO extends BaseDTO {

    @NotBlank(message = "表名不能为空")
    @Schema(example = "示例表")
    private String tableName;

    @NotBlank(message = "sheet名不能为空")
    @Schema(example = "Sheet1")
    private String sheetName;

    @NotBlank(message = "字段名不能为空")
    @Schema(example = "示例字段")
    private String fieldName;
}
