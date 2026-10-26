package com.example.pojo.dto.sheet;

import com.example.pojo.dto.BaseDTO;
import com.example.pojo.dto.PageDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class FilterDTO extends PageDTO {

    @NotBlank(message = "表名不能为空")
    @Schema(example = "示例表")
    private String tableName;

    @NotBlank(message = "sheet名不能为空")
    @Schema(example = "Sheet1")
    private String sheetName;

    // 固定值字段
    private List<String> fields;

    // 固定值
    private List<String> fieldValues;

    // 精确数值字段
    private List<String> numberFields;

    // 精确数值
    private List<BigDecimal> numbers;

    // 范围字段
    private List<String> rangeFields;

    // 范围最小值
    private List<BigDecimal> mins;

    // 范围最大值
    private List<BigDecimal> maxs;

    // 日期字段
    private List<String> dateFields;

    // 日期起点
    private List<LocalDate> begin;

    // 日期终点
    private List<LocalDate> end;
}
