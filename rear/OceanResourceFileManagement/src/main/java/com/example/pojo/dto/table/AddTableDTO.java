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
public class AddTableDTO extends BaseDTO {

    @NotBlank(message = "表名不能为空")
    @Schema(example = "示例表")
    private String tableName;

    @Schema(example = "摘要")
    private String description;

    @Schema(example = "['标签1',\"标签2\"]")
    private List<String> tags;
}
