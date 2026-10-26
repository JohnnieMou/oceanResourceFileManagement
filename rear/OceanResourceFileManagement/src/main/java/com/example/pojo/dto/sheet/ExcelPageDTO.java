package com.example.pojo.dto.sheet;

import com.example.pojo.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ExcelPageDTO extends PageDTO {

    @NotBlank(message = "表名不能为空")
    @Schema(example = "示例表", required = true)
    private String tableName;

    @NotBlank(message = "sheet名不能为空")
    @Schema(example = "Sheet1", required = true)
    private String sheetName;
}
