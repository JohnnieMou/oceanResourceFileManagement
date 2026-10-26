package com.example.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Data
public class PageDTO extends BaseDTO{

    @Min(value = 1, message = "页码最小为1")
    @Schema(example = "1")
    private int page;

    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    @Schema(example = "10")
    private int pageSize;

}
