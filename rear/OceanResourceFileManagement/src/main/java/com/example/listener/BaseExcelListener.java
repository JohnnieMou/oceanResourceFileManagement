package com.example.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.enums.DataTypeEnum;
import com.example.enums.StatusCodeEnum;
import com.example.exception.BizException;
import com.example.mapper.*;
import com.example.pojo.entity.Field;
import com.example.pojo.entity.Table;
import com.example.utils.FieldUtil;
import com.example.utils.SpringUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class BaseExcelListener implements ReadListener<LinkedHashMap<Integer, String>> {
    protected List<Map<String, String>> list = new ArrayList<>(); // 数据缓存
    protected final int batchSize = 1000; // 批量插入大小

    protected DynamicMapper dynamicMapper = SpringUtil.getBean(DynamicMapper.class);
    protected TableMapper tableMapper = SpringUtil.getBean(TableMapper.class);
    protected FieldMapper fieldMapper = SpringUtil.getBean(FieldMapper.class);
    protected SheetMapper sheetMapper = SpringUtil.getBean(SheetMapper.class);
    protected FileMappingMapper fileMappingMapper = SpringUtil.getBean(FileMappingMapper.class);
    protected FieldUtil fieldUtil = SpringUtil.getBean(FieldUtil.class);
    
    protected String tableEnName; // 表英文名
    protected String tableCnName; // 表中文名
    protected String sheetEnName; // Sheet英文名
    protected String sheetCnName;
    protected String finalTableName; // 最终表名
    protected Map<String, Boolean> dateFieldCache; // 字段类型缓存

    protected static final String[] excludeFields = {"id", "航次", "站位"}; // 排除字段

    @Override
    public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
        sheetCnName = context.readSheetHolder().getSheetName();

        if (tableMapper.selectOne(new LambdaQueryWrapper<Table>().eq(Table::getTableCnName, tableCnName)) == null) {
            throw new BizException(StatusCodeEnum.TABLE_NOT_CREATED);
        }
    }

    protected void initDateFieldCache() {
        dateFieldCache = new HashMap<>();
        List<Field> fields = fieldMapper.selectList(new LambdaQueryWrapper<Field>()
                .eq(Field::getTableEnName, tableEnName)
                .eq(Field::getSheetEnName, sheetEnName));
                
        for (Field field : fields) {
            dateFieldCache.put(field.getFieldEnName(), 
                DataTypeEnum.DATE.getEnName().equals(field.getDataType()));
        }
    }

    protected void insertWithRetry(List<Map<String, String>> dataList, int currentBatchSize) {
        try {
            dynamicMapper.save(finalTableName, dataList);
        } catch (Exception e) {
            if (currentBatchSize <= 1) {
                for (Map<String, String> map : dataList) {
                    try {
                        List<Map<String, String>> singleRecord = new ArrayList<>();
                        singleRecord.add(new HashMap<>(map));
                        dynamicMapper.save(finalTableName, singleRecord);
                    } catch (Exception ex) {
                        log.error("插入数据失败: {}", ex.getMessage());
                    }
                }
                return;
            }

            int halfSize = currentBatchSize / 2;
            int mid = dataList.size() / 2;
            insertWithRetry(dataList.subList(0, mid), halfSize);
            insertWithRetry(dataList.subList(mid, dataList.size()), halfSize);
        }
    }

    @Override
    public void invoke(LinkedHashMap<Integer, String> data, AnalysisContext analysisContext) {
        Map<String, String> fieldNameAndValue = fieldUtil.setFieldValue(data, dateFieldCache, sheetCnName);
        list.add(fieldNameAndValue);
        if (list.size() > batchSize) {
            insertWithRetry(list, batchSize);
            list = new ArrayList<>(batchSize);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); i += 100) {
                List<Map<String, String>> subList = list.subList(i, Math.min(i + 100, list.size()));
                insertWithRetry(subList, 100);
            }
        }
        list.clear();
    }
}