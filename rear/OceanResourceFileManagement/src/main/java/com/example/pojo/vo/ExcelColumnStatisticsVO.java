package com.example.pojo.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ExcelColumnStatisticsVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    // 列名
    private String columnName;

    // 最大值
    private Double columnMax;

    // 最小值
    private Double columnMin;

    // 平均值
    private Double columnAvg;

    // 总和
    private Double columnSum;

    // 个数
    private Long columnCount;

}
