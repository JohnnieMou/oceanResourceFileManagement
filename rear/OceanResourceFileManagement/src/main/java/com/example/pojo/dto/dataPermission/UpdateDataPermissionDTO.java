package com.example.pojo.dto.dataPermission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UpdateDataPermissionDTO extends DataPermissionDTO {
    /**
     * 开始年份
     */
    @Schema(example = "2020")
    private Integer yearStart;

    /**
     * 结束年份
     */
    @Schema(example = "2030")
    private Integer yearEnd;

    /**
     * 开始航次
     */
    @Schema(example = "202001")
    private Integer voyageStart;

    /**
     * 结束航次
     */
    @Schema(example = "203012")
    private Integer voyageEnd;
}
