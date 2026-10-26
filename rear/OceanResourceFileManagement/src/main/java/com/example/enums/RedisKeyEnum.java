package com.example.enums;

public enum RedisKeyEnum {

    TABLE_LIST("table:list"),
    UPLOAD_EXCEL("upload:excel"),
    FILTER_RESULT("filter:result");

    private final String key;

    RedisKeyEnum(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
