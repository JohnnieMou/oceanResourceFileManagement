package com.example.pojo.dto.field;

import com.example.pojo.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateFieldDTO extends BaseDTO {

    @NotBlank(message = "表名不能为空")
    @Schema(example = "示例表")
    private String tableName;

    @NotBlank(message = "sheet名不能为空")
    @Schema(example = "Sheet1")
    private String sheetName;

    @NotBlank(message = "老字段名不能为空")
    @Schema(example = "示例字段")
    private String oldFieldName;

    @Schema(example = "新示例字段")
    private String newFieldName;

    // 数据类型： string number date
    @Schema(example = "number")
    private String fieldType;
}
