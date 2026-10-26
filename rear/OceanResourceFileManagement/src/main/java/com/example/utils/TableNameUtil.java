package com.example.utils;

import com.example.constant.SeparatorConstant;

public class TableNameUtil {

    public static String merge(String tableName, String sheetName) {
        return tableName + SeparatorConstant.TABLE_SHEET_SEPARATOR + sheetName;
    }

    public static String merge(String tableName, String sheetName, Integer voyage) {
        return tableName + SeparatorConstant.TABLE_SHEET_SEPARATOR
                + sheetName + SeparatorConstant.TABLE_SHEET_SEPARATOR
                + voyage;
    }

    public static String getTableName(String finalTableName) {
        String[] split = finalTableName.split(SeparatorConstant.TABLE_SHEET_SEPARATOR);
        return split[0];
    }

    public static String getSheetName(String finalTableName) {
        String[] split = finalTableName.split(SeparatorConstant.TABLE_SHEET_SEPARATOR);
        return split[1];
    }
}
