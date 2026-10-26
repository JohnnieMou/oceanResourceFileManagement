package com.example.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DataTypeEnum {
    NUMBER("number", "数值", "DECIMAL"),
    STRING("string", "字符串", "TEXT"),
    DATE("date", "日期", "DATE"),

//    // 设定经纬度
//    SET_LONGITUDE("set_longitude", "设定经度", "DECIMAL(9,6)"),
//    SET_LATITUDE("set_latitude", "设定纬度", "DECIMAL(9,6)"),
//
//    // 下网经纬度
//    DOWN_LONGITUDE("down_longitude", "下网经度", "DECIMAL(9,6)"),
//    DOWN_LATITUDE("down_latitude", "下网纬度", "DECIMAL(9,6)"),
//
//    // 起网经纬度
//    UP_LONGITUDE("up_longitude", "起网经度", "DECIMAL(9,6)"),
//    UP_LATITUDE("up_latitude", "起网纬度", "DECIMAL(9,6)"),

    LONGITUDE("longitude", "经度", "DECIMAL(9,6)"),
    LATITUDE("latitude", "纬度", "DECIMAL(9,6)");
    private final String enName;
    private final String cnName;
    private final String type;
}
