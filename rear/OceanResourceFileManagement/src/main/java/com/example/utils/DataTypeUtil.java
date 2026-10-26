package com.example.utils;

import com.example.enums.DataTypeEnum;
import com.example.mapper.DynamicMapper;
import com.example.mapper.FieldMapper;
import com.example.pojo.entity.Field;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataTypeUtil {

    private final DynamicMapper dynamicMapper;
    private final FieldMapper fieldMapper;

    // 判数值型的选取个数
    private static final int count = 3;
    private static DataTypeUtil instance;

    @PostConstruct
    public void init() {
        instance = this;
    }

    //排除字段
    private static final String[] excludeFields = {"id", "voyage_number", "station_number"};

    // 判断是否为数值型
    public static boolean isNumber(String finalTableName, String fieldEnName) {
        int index = 0;
        while (index < count) {
            String data = instance.dynamicMapper.getByIndex(index, finalTableName, fieldEnName);

            if (data == null || data.isEmpty()) {
                index++;
                continue;
            }

            try {
                Double.parseDouble(data);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    //记录信息
    public static void recordInfo(String finalTableName, String fieldEnName, String fieldCnName) {
        String tableName = TableNameUtil.getTableName(finalTableName);
        String sheetName = TableNameUtil.getSheetName(finalTableName);

        //fileName为排除字段EXCLUDE_FIELDS中的一个
        for (String excludeField : excludeFields) {
            if (fieldEnName.equals(excludeField)) {
                log.info("排除字段：" + fieldEnName);
                instance.fieldMapper.insert(
                        Field.builder()
                                .tableEnName(tableName)
                                .sheetEnName(sheetName)
                                .fieldCnName(fieldCnName)
                                .fieldEnName(fieldEnName)
                                .dataType(DataTypeEnum.STRING.getEnName())
                                .build()
                );
                return;
            }
        }

        if (fieldEnName.contains("date")) {
            log.info("日期字段：" + fieldEnName);
            instance.fieldMapper.insert(
                    Field.builder()
                            .tableEnName(tableName)
                            .sheetEnName(sheetName)
                            .fieldCnName(fieldCnName)
                            .fieldEnName(fieldEnName)
                            .dataType(DataTypeEnum.DATE.getEnName())
                            .build()
            );
        } else if (isNumber(finalTableName, fieldEnName)) {
            log.info("数值字段：" + fieldEnName);
            instance.fieldMapper.insert(
                    Field.builder()
                            .tableEnName(tableName)
                            .sheetEnName(sheetName)
                            .fieldCnName(fieldCnName)
                            .fieldEnName(fieldEnName)
                            .dataType(DataTypeEnum.NUMBER.getEnName())
                            .build()
            );
        } else {
            log.info("字符串字段：" + fieldEnName);
            instance.fieldMapper.insert(
                    Field.builder()
                            .tableEnName(tableName)
                            .sheetEnName(sheetName)
                            .fieldCnName(fieldCnName)
                            .fieldEnName(fieldEnName)
                            .dataType(DataTypeEnum.STRING.getEnName())
                            .build()
            );
        }
    }

    // 映射数据类型
    public static String mapDataType(String fieldType) {
        for (DataTypeEnum dataType : DataTypeEnum.values()) {
            if (dataType.getEnName().equalsIgnoreCase(fieldType)) {
                return dataType.getType();
            }
        }
        return DataTypeEnum.STRING.getType(); // 默认类型
    }

    // 将数据转换为List<List<String>>格式，日期类型格式化
    public static List<List<String>> dataToString(List<LinkedHashMap<String, Object>> data) {
        List<List<String>> rows = data.stream()
                .map(row -> row.values().stream()
                        .map(value -> {
                            if (value instanceof Date) {
                                return new SimpleDateFormat("yyyy.MM.dd").format(value);
                            }
                            return value == null ? "" : value.toString();
                        })
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
        return rows;
    }
}
