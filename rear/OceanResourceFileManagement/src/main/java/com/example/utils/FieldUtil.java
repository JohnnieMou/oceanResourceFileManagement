package com.example.utils;

import com.alibaba.excel.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.exception.BizException;
import com.example.mapper.FieldMapper;
import com.example.pojo.entity.Field;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class FieldUtil {

    private final FieldMapper fieldMapper;
    private static FieldUtil instance;

    @PostConstruct
    public void init() {
        instance = this;
    }

    /**
     * 将字段名转换为顺序字段名
     */
    public static LinkedHashMap<String, String> fieldNameConvert(Map<Integer, String> fieldHead) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>(fieldHead.size());
        fieldHead.forEach((index, fieldName) -> {
            result.put(fieldName, "field_" + index);
        });
        return result;
    }
    
    /**
     * 设置字段值，处理日期类型的字段
     */
    public Map<String, String> setFieldValue(LinkedHashMap<Integer, String> data, Map<String, Boolean> dateFieldCache, String sheetCnName) {
        Map<String, String> result = new HashMap<>(data.size());
        data.forEach((key, value) -> {
            String fieldName = "field_" + key;
            if (StringUtils.isBlank(value)) {
                result.put(fieldName, null);
            } else {
                // 使用缓存检查字段类型
                if (Boolean.TRUE.equals(dateFieldCache.get(fieldName))) {
                    try {
                        result.put(fieldName, formatDateValue(value, sheetCnName, fieldName));
                    } catch (Exception e) {
                        throw e;
                    }
                } else {
                    result.put(fieldName, value);
                }
            }
        });
        return result;
    }

    /**
     * 格式化日期值为统一格式
     */
    public String formatDateValue(String value, String sheetCnName, String fieldEnName) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        // 移除所有空格
        value = value.trim().replaceAll("\\s+", "");

        // 处理范围日期，取最早的日期
        if (value.contains("-")) {
            value = value.split("-")[0];
        }

        // 处理 yyyy.MM.dd 格式
        if (value.matches("\\d{4}\\.\\d{1,2}\\.\\d{1,2}")) {
            String[] parts = value.split("\\.");
            return String.format("%s.%02d.%02d", parts[0],
                Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        }

        // 处理 yyyy/MM/dd 格式
        if (value.matches("\\d{4}/\\d{1,2}/\\d{1,2}")) {
            String[] parts = value.split("/");
            return String.format("%s.%02d.%02d", parts[0],
                    Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        }

        // 处理 yyyy.MM 格式
        if (value.matches("\\d{4}\\.\\d{1,2}")) {
            String[] parts = value.split("\\.");
            return String.format("%s.%02d.01", parts[0], Integer.parseInt(parts[1]));
        }

        // 处理 yyyyMMdd 格式
        if (value.matches("\\d{8}")) {
            return String.format("%s.%s.%s",
                value.substring(0, 4),
                value.substring(4, 6),
                value.substring(6, 8));
        }

        // 处理 yyyyMM 格式
        if (value.matches("\\d{6}")) {
            return String.format("%s.%s.01",
                value.substring(0, 4),
                value.substring(4, 6));
        }

        String fieldCnName = fieldMapper.selectOne(new LambdaQueryWrapper<Field>()
                .eq(Field::getFieldEnName, fieldEnName))
                .getFieldCnName();
        // 如果都不匹配，抛出异常
        throw new BizException(String.format(
                "sheet “%s” 中的字段 “%s” 存在值 “%s” 格式不正确，请使用正确的日期格式(如: YYYY.MM.DD)",
                sheetCnName,
                fieldCnName,
                value
        ));
    }
}