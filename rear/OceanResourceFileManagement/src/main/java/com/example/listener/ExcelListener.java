package com.example.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.util.ConverterUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.constant.FileStatusConstant;
import com.example.constant.SeparatorConstant;
import com.example.enums.DataTypeEnum;
import com.example.enums.StatusCodeEnum;
import com.example.exception.BizException;
import com.example.mapper.TableTagMapper;
import com.example.pojo.entity.Field;
import com.example.pojo.entity.FileMapping;
import com.example.pojo.entity.Sheet;
import com.example.pojo.entity.Table;
import com.example.utils.*;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@SuppressWarnings("all")
public class ExcelListener extends BaseExcelListener {
    private final String TRANSLATION = "translation"; // 翻译计时
    private final String INVOKE_HEAD = "invokeHead"; // 表头读取计时
    private final String INVOKE = "invoke"; // 数据读取计时
    private final String DO_AFTER_ALL_ANALYSED = "doAfterAllAnalysed"; // 数据插入计时
    private boolean autoLogEnabled; // 是否开启自动日志
    private TableTagMapper tableTagMapper = SpringUtil.getBean(TableTagMapper.class);
    private Map<String, LinkedHashMap<String, String>> fieldListMap; // 各sheet的字段列表
    private Map<String, String> fieldCnAndType; // 字段中文名和类型
    private Map<Integer, String> fieldHead; // 表头
    private boolean isCover; // 是否覆盖
    private ExecutionTimeRecordUtil timeRecorder; // 时间记录器

    public ExcelListener(String tableCnName, Map<String, LinkedHashMap<String, String>> fieldListMap, boolean autoLogEnabled, boolean isCover) {
        this.timeRecorder = new ExecutionTimeRecordUtil();
        this.tableCnName = tableCnName;

        timeRecorder.start(TRANSLATION);
        this.tableEnName = TranslateUtil.toEnglish(tableCnName);
        timeRecorder.stop(TRANSLATION);

        this.fieldListMap = fieldListMap;
        this.autoLogEnabled = autoLogEnabled;
        this.isCover = isCover;
    }

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        timeRecorder.start(INVOKE_HEAD);

        super.invokeHead(headMap, context);

        fieldHead = ConverterUtils.convertToStringMap(headMap, context); // 获取表头
        timeRecorder.start(TRANSLATION);
        LinkedHashMap<String, String> fieldCnAndEn = FieldUtil.fieldNameConvert(fieldHead); // 字段中文名和英文名
        sheetEnName = TranslateUtil.toEnglish(sheetCnName);
        timeRecorder.stop(TRANSLATION);


        finalTableName = tableEnName + SeparatorConstant.TABLE_SHEET_SEPARATOR + sheetEnName;
        if (finalTableName.length() > 64) {
            throw new BizException(StatusCodeEnum.TABLE_NAME_TOO_LONG);
        }

        // 覆盖上传
        if (isCover) {
            fieldCnAndType = fieldListMap.get(sheetCnName);
            if (fieldCnAndType == null) {
                throw new BizException("sheet: " + sheetCnName + " 不存在");
            }
            Map<Integer, String> geoFieldName = new HashMap<>(2);

            // 获取翻译后的字段名和类型
            List<String> columnNameAndTypeSQL = fieldCnAndType.keySet().stream()
                    .map(fieldCnName -> {
                        String fieldEnName = fieldCnAndEn.get(fieldCnName);
                        if (fieldCnName.contains(DataTypeEnum.LONGITUDE.getEnName())) {
                            geoFieldName.put(0, fieldEnName);
                        }
                        if (fieldCnName.contains(DataTypeEnum.LATITUDE.getEnName())) {
                            geoFieldName.put(1, fieldEnName);
                        }
                        String sqlDataType = DataTypeUtil.mapDataType(fieldCnAndType.get(fieldCnName));
                        return "`" + fieldEnName + "` " + sqlDataType;
                    }).collect(Collectors.toList());

            if (geoFieldName.size() == 1) {
                throw new BizException("经纬度都需上传，不能只上传一个");
            }

            // 删除表
            try {
                dynamicMapper.dropTable(finalTableName);

                sheetMapper.delete(new LambdaQueryWrapper<Sheet>()
                        .eq(Sheet::getTableEnName, tableEnName)
                        .eq(Sheet::getSheetEnName, sheetEnName));

                fieldMapper.delete(new LambdaQueryWrapper<Field>()
                        .eq(Field::getTableEnName, tableEnName)
                        .eq(Field::getSheetEnName, sheetEnName));
            } catch (Exception e) {
                log.info("表 " + finalTableName + " 不存在，创建表");
            }

            // 创建表
            try {
                dynamicMapper.createTable(finalTableName, columnNameAndTypeSQL, geoFieldName);
                sheetMapper.insert(Sheet.builder()
                        .tableEnName(tableEnName)
                        .sheetEnName(sheetEnName)
                        .sheetCnName(sheetCnName)
                        // TODO 经纬度判断
                        .containsGeo(false)
                        .build());
            } catch (Exception e) {
                System.err.println(e.getMessage());
                // 回滚操作，移除创建的记录
                dynamicMapper.dropTable(finalTableName);
                tableTagMapper.delete(tableMapper.selectOne(new LambdaQueryWrapper<Table>()
                        .eq(Table::getTableEnName, tableEnName)).getId());
                tableMapper.delete(new LambdaQueryWrapper<Table>().eq(Table::getTableEnName, tableEnName));
                sheetMapper.delete(new LambdaQueryWrapper<Sheet>().eq(Sheet::getTableEnName, tableEnName));
                fieldMapper.delete(new LambdaQueryWrapper<Field>().eq(Field::getTableEnName, tableEnName));
            }

            // 插入字段
            fieldCnAndEn.forEach((fieldCnName, fieldEnName) -> {
                for (String excludeField : excludeFields) {
                    if (fieldCnName.equals(excludeField)) {
                        fieldMapper.insert(
                                Field.builder()
                                        .tableEnName(tableEnName)
                                        .sheetEnName(sheetEnName)
                                        .fieldCnName(fieldCnName)
                                        .fieldEnName(fieldEnName)
                                        .dataType(DataTypeEnum.STRING.getEnName())
                                        .build()
                        );
                        return;
                    }
                }
                fieldMapper.insert(
                        Field.builder()
                                .tableEnName(tableEnName)
                                .sheetEnName(sheetEnName)
                                .fieldCnName(fieldCnName)
                                .fieldEnName(fieldEnName)
                                .dataType(fieldCnAndType.get(fieldCnName))
                                .build()
                );
            });
        }
        // 追加上传
        else {
            // 检查表是否存在
            if (dynamicMapper.checkTableExists(finalTableName) == null) {
                throw new BizException(StatusCodeEnum.PLEASE_OVERWRITE_UPLOAD);
            }

            // 更新文件映射表
            fileMappingMapper.update(new LambdaUpdateWrapper<FileMapping>()
                    .eq(FileMapping::getOriginalFileName, tableCnName)
                    .set(FileMapping::getStatus, FileStatusConstant.MERGING));
        }
        initDateFieldCache();
        timeRecorder.stop(INVOKE_HEAD);
    }

    @Override
    public void invoke(LinkedHashMap<Integer, String> data, AnalysisContext analysisContext) {
        timeRecorder.start(INVOKE);
        super.invoke(data, analysisContext);
        timeRecorder.stop(INVOKE);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        timeRecorder.start(DO_AFTER_ALL_ANALYSED);
        super.doAfterAllAnalysed(analysisContext);
        timeRecorder.stop(DO_AFTER_ALL_ANALYSED);

        if (autoLogEnabled) {
            log.info("总执行时间：" + timeRecorder.getTotalFormattedTime());
            log.info("详细执行时间 ↓↓↓\n" + timeRecorder.getDetailedTimings());
        }
    }
}
