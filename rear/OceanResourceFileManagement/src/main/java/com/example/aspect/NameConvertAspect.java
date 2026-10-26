package com.example.aspect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.annotation.NameConvert;
import com.example.enums.StatusCodeEnum;
import com.example.exception.BizException;
import com.example.mapper.FieldMapper;
import com.example.mapper.SheetMapper;
import com.example.mapper.TableMapper;
import com.example.pojo.dto.BaseDTO;
import com.example.pojo.entity.*;
import com.example.pojo.vo.FilterResultVO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
@RequiredArgsConstructor
public class NameConvertAspect {

    private final TableMapper tableMapper;
    private final SheetMapper sheetMapper;
    private final FieldMapper fieldMapper;

    private static final long EXPIRE_TIME = 2 * 60 * 60 * 1000; // 缓存过期时间，2小时

    private Map<String, CacheEntry> tableCache = new ConcurrentHashMap<>(); // 缓存表名
    private Map<String, CacheEntry> sheetCache = new ConcurrentHashMap<>(); // 缓存sheet名
    private Map<String, CacheEntry> fieldCache = new ConcurrentHashMap<>(); // 缓存字段名

    @Scheduled(fixedRate = EXPIRE_TIME)
    public void cleanCache() {
        tableCache.entrySet().removeIf(entry -> isExpired(entry.getValue()));
        sheetCache.entrySet().removeIf(entry -> isExpired(entry.getValue()));
        fieldCache.entrySet().removeIf(entry -> isExpired(entry.getValue()));
    }

    @Pointcut("execution(* com.example.controller.*.*(..)) && @annotation(com.example.annotation.NameConvert)")
    public void fieldNameConvertPointCut() {
    }

    @Around("fieldNameConvertPointCut()")
    @SneakyThrows
    public Object checkTableName(ProceedingJoinPoint joinPoint) {
        // 获取当前方法参数
        Object[] args = joinPoint.getArgs();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Parameter[] parameters = method.getParameters();
        NameConvert annotation = method.getAnnotation(NameConvert.class);

        boolean tableToEnglish = annotation.tableToEnglish();
        boolean sheetToEnglish = annotation.sheetToEnglish();
        boolean fieldToChinese = annotation.returnFieldToChinese();

        Object result;
        String tableEnName = null;
        String sheetEnName = null;

        for (int i = 0; i < args.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.getType().equals(String.class)) {
                if (tableToEnglish && parameter.getName().equals("tableName")) {
                    String tableCnName = (String) args[i];
                    tableEnName = getTableEnName(tableCnName);
                    args[i] = tableEnName;
                } else if (sheetToEnglish && parameter.getName().equals("sheetName")) {
                    String sheetCnName = (String) args[i];
                    sheetEnName = getSheetEnName(sheetCnName);
                    args[i] = sheetEnName;
                }
            } else if (BaseDTO.class.isAssignableFrom(parameter.getType())) {
                Object dto = args[i];
                for (Method dtoMethod : dto.getClass().getMethods()) {
                    if (tableToEnglish && (dtoMethod.getName().equals("getTableName") || dtoMethod.getName().equals("getOldTableName"))) {
                        String tableCnName = (String) dtoMethod.invoke(dto);
                        Method setTableNameMethod;
                        if (dtoMethod.getName().equals("getTableName")) {
                            setTableNameMethod = dto.getClass().getMethod("setTableName", String.class);
                        } else {
                            setTableNameMethod = dto.getClass().getMethod("setOldTableName", String.class);
                        }
                        tableEnName = getTableEnName(tableCnName);
                        setTableNameMethod.invoke(dto, tableEnName);
                    } else if (sheetToEnglish && dtoMethod.getName().equals("getSheetName")) {
                        String sheetCnName = (String) dtoMethod.invoke(dto);
                        Method setSheetName = dto.getClass().getMethod("setSheetName", String.class);
                        sheetEnName = getSheetEnName(sheetCnName);
                        setSheetName.invoke(dto, sheetEnName);
                    }
                }
            }
        }

        // 继续执行原方法，并传入修改后的参数
        result = joinPoint.proceed(args);

        if (fieldToChinese && tableEnName != null && sheetEnName != null) {
            if (result instanceof Result<?> resultData) {
                Object data = resultData.getData();

                if (data instanceof PageResult pageResult) {
                    convertFirstRecordFieldNames(pageResult, tableEnName);
                }
                else if (data instanceof FilterResultVO) {
                    PageResult pageResult = ((FilterResultVO) data).getPageResult();
                    convertFirstRecordFieldNames(pageResult, tableEnName);
                }
            }
        }

        return result;
    }

    private void convertFirstRecordFieldNames(PageResult pageResult, String tableEnName) {
        List<?> records = pageResult.getRecords();

        // 如果 records 非空且元素为 LinkedHashMap，则将第一条数据的字段名转换
        if (!records.isEmpty() && records.get(0) instanceof LinkedHashMap) {
            List<LinkedHashMap<String, Object>> dynamicDataList = (List<LinkedHashMap<String, Object>>) records;
            LinkedHashMap<String, Object> excelDynamicData = dynamicDataList.get(0);

            LinkedHashMap<String, Object> translatedMap = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : excelDynamicData.entrySet()) {
                String fieldEnName = entry.getKey();
                // id 不转换
                if ("id".equals(fieldEnName)) {
                    translatedMap.put(fieldEnName, entry.getValue());
                } else {
                    String fieldCnName = fieldMapper.selectList(new LambdaQueryWrapper<Field>()
                            .eq(Field::getTableEnName, tableEnName)
                            .eq(Field::getFieldEnName, fieldEnName)).get(0).getFieldCnName();
                    translatedMap.put(fieldCnName, entry.getValue());
                }
            }

            dynamicDataList.set(0, translatedMap);
        }
    }

    private boolean isExpired(CacheEntry entry) {
        return System.currentTimeMillis() - entry.getTimestamp() > 60 * 1000;
    }

    private String getTableEnName(String tableCnName) {
        if (StringUtils.isBlank(tableCnName)) {
            throw new BizException(StatusCodeEnum.TABLE_NAME_NOT_NULL);
        }
        if (tableCache.containsKey(tableCnName)) {
            return tableCache.get(tableCnName).getValue();
        }
        List<Table> tables = tableMapper.selectList(new LambdaQueryWrapper<Table>().eq(Table::getTableCnName, tableCnName));
        if (tables.isEmpty()) {
            throw new BizException(StatusCodeEnum.TABLE_NOT_EXISTS);
        }
        String tableEnName = tables.get(0).getTableEnName();
        tableCache.put(tableCnName, new CacheEntry(tableEnName));
        return tableEnName;
    }

    private String getSheetEnName(String sheetCnName) {
        if (StringUtils.isBlank(sheetCnName)) {
            throw new BizException(StatusCodeEnum.SHEET_NAME_NOT_NULL);
        }
        if (sheetCache.containsKey(sheetCnName)) {
            return sheetCache.get(sheetCnName).getValue();
        }
        List<Sheet> sheets = sheetMapper.selectList(new LambdaQueryWrapper<Sheet>().eq(Sheet::getSheetCnName, sheetCnName));
        if (sheets.isEmpty()) {
            throw new BizException(StatusCodeEnum.SHEET_NOT_EXISTS);
        }
        String sheetEnName = sheets.get(0).getSheetEnName();
        sheetCache.put(sheetCnName, new CacheEntry(sheetEnName));
        return sheetEnName;
    }

    private String getFieldEnName(String fieldCnName) {
        if (StringUtils.isBlank(fieldCnName)) {
            throw new BizException(StatusCodeEnum.FIELD_NAME_NOT_NULL);
        }
        if (fieldCache.containsKey(fieldCnName)) {
            return fieldCache.get(fieldCnName).getValue();
        }
        List<Field> fields = fieldMapper.selectList(new LambdaQueryWrapper<Field>().eq(Field::getFieldCnName, fieldCnName));
        if (fields.isEmpty()) {
            throw new BizException(StatusCodeEnum.FIELD_NOT_EXISTS);
        }
        String fieldEnName = fields.get(0).getFieldEnName();
        fieldCache.put(fieldCnName, new CacheEntry(fieldEnName));
        return fieldEnName;
    }

    private class CacheEntry {
        private final String value;
        private final long timestamp;

        public CacheEntry(String value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }

        public String getValue() {
            return value;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
