package com.example.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ConverterUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.enums.DataTypeEnum;
import com.example.enums.StatusCodeEnum;
import com.example.exception.BizException;
import com.example.mapper.SheetMapper;
import com.example.mapper.TableMapper;
import com.example.pojo.entity.Sheet;
import com.example.pojo.entity.Table;
import com.example.utils.SpringUtil;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FieldListener implements ReadListener<Map<String, String>> {
    private Map<String, LinkedHashMap<String, String>> sheetFieldMap = null;
    private TableMapper tableMapper = SpringUtil.getBean(TableMapper.class);
    private SheetMapper sheetMapper = SpringUtil.getBean(SheetMapper.class);
    private String tableCnName = null;

    public FieldListener(String tableCnName) {
        this.tableCnName = tableCnName;
    }

    public FieldListener(Map<String, LinkedHashMap<String, String>> sheetFieldMap) {
        this.sheetFieldMap = sheetFieldMap;
    }

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        String sheetName = context.readSheetHolder().getSheetName(); // sheet名称
        Map<Integer, String> fields = ConverterUtils.convertToStringMap(headMap, context); // 获取表头

        // 判断表头第一个字段是否为航次
        if (!"航次".equals(fields.get(0))) {
            throw new BizException("表格不符合要求：第一列应为 “航次“");
        }

        if (tableCnName != null) {
            // 判断sheetName是否已存在
            String tableEnName = tableMapper.selectOne(new LambdaQueryWrapper<Table>().eq(Table::getTableCnName, tableCnName)).getTableEnName();
            sheetMapper.selectList(new LambdaQueryWrapper<Sheet>().eq(Sheet::getTableEnName, tableEnName)).forEach(sheet -> {
                if (sheet.getSheetCnName().equals(sheetName)) {
                    throw new BizException(StatusCodeEnum.SHEET_NAME_REPEATED_WHEN_APPEND);
                }
            });
        }

        if (sheetFieldMap != null) {
            LinkedHashMap<String, String> fieldCnAndType = fields.entrySet().stream() // 获取表头中文字段名，并默认类型为String
                    .collect(Collectors.toMap(
                            Map.Entry::getValue,
                            v -> DataTypeEnum.STRING.getEnName(),
                            (oldValue, newValue) -> oldValue,
                            LinkedHashMap::new
                    ));
            sheetFieldMap.put(sheetName, fieldCnAndType);
        }

    }

    @Override
    public void invoke(Map<String, String> stringStringMap, AnalysisContext analysisContext) {

    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
