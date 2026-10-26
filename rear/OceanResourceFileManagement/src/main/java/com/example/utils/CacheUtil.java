package com.example.utils;

import com.example.constant.SeparatorConstant;
import com.example.context.BaseContext;

public class CacheUtil {

    public static String getKey(String tableEnName, String sheetEnName, String str) {
        return tableEnName +
                SeparatorConstant.TABLE_SHEET_SEPARATOR +
                sheetEnName +
                SeparatorConstant.TABLE_SHEET_SEPARATOR +
                str;
    }

    public static String getKeyWithUserId(String tableEnName, String sheetEnName) {
        return tableEnName +
                SeparatorConstant.TABLE_SHEET_SEPARATOR +
                sheetEnName +
                SeparatorConstant.TABLE_SHEET_SEPARATOR +
                BaseContext.getUserId();
    }
}
