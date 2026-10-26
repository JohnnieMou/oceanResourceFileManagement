package com.example.pojo.dto.record;

import com.example.pojo.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;

@Data
public class UpdateRecordDTO extends BaseDTO {

    @NotBlank(message = "表名不能为空")
    @Schema(example = "示例表")
    private String tableName;

    @NotBlank(message = "sheet名不能为空")
    @Schema(example = "Sheet1")
    private String sheetName;

    @NotNull(message = "记录id不能为空")
    @Schema(example = "1")
    private Long id;

    @NotNull(message = "记录不能为空")
    LinkedHashMap<String, Object> record;
}
