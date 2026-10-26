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
import com.example.pojo.entity.Field;
import com.example.pojo.entity.FileMapping;
import com.example.pojo.entity.Sheet;
import com.example.pojo.entity.Table;
import com.example.utils.DataTypeUtil;
import com.example.utils.FieldUtil;
import com.example.utils.TranslateUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class SheetListener extends BaseExcelListener {
    private Map<String, LinkedHashMap<String, String>> fieldListMap; // 各sheet的字段列表
    private Map<Integer, String> fieldHead; // 表头
    private Map<String, String> fieldCnAndType; // 字段中文名和类型

    public SheetListener(String tableCnName, Map<String, LinkedHashMap<String, String>> fieldListMap) {
        this.tableCnName = tableCnName;
        this.tableEnName = TranslateUtil.toEnglish(tableCnName);
        this.fieldListMap = fieldListMap;
    }

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        super.invokeHead(headMap, context);

        fieldHead = ConverterUtils.convertToStringMap(headMap, context); // 获取表头
        LinkedHashMap<String, String> fieldCnAndEn = FieldUtil.fieldNameConvert(fieldHead); // 字段中文名和英文名
        sheetEnName = TranslateUtil.toEnglish(sheetCnName);
        fieldCnAndType = fieldListMap.get(sheetCnName);

        if (fieldCnAndType == null) {
            throw new BizException("sheet: " + sheetCnName + " 不存在");
        }

        finalTableName = tableEnName + SeparatorConstant.TABLE_SHEET_SEPARATOR + sheetEnName;
        if (finalTableName.length() > 64) {
            throw new BizException(StatusCodeEnum.TABLE_NAME_TOO_LONG);
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

            // 更新文件映射表
            fileMappingMapper.update(new LambdaUpdateWrapper<FileMapping>()
                    .eq(FileMapping::getOriginalFileName, tableCnName)
                    .set(FileMapping::getStatus, FileStatusConstant.MERGING));
        } catch (Exception e) {
            System.err.println(e.getMessage());
            // 回滚操作，移除创建的记录
            dynamicMapper.dropTable(finalTableName);
            sheetMapper.delete(new LambdaQueryWrapper<Sheet>()
                    .eq(Sheet::getTableEnName, tableEnName)
                    .eq(Sheet::getSheetEnName, sheetEnName));
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
        initDateFieldCache();
    }
}
