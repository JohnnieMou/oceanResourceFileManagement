package com.example.pojo.dto.dataPermission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
@NoArgsConstructor
public class DataPermissionDTO {

    /**
     * 用户id
     */
    @Schema(example = "1")
    private Long userId;

    /**
     * 表中文名称
     */
    @Schema(example = "['示例表1', '示例表2']")
    private List<String> tableNames;
}
