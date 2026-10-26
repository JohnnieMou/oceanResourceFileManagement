package com.example.pojo.dto.table;

import com.example.pojo.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateTableDTO extends BaseDTO {

    @NotBlank(message = "旧表名不能为空")
    @Schema(example = "示例表")
    private String oldTableName;

    @Schema(example = "新示例表")
    private String newTableName;

    @Schema(example = "新摘要")
    private String description;

    @Schema( example = "['标签1', '标签2', '标签3']")
    private List<String> tags;
}
