package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.enums.StatusCodeEnum;
import com.example.exception.BizException;
import com.example.mapper.DynamicMapper;
import com.example.mapper.FieldMapper;
import com.example.pojo.dto.record.AddRecordDTO;
import com.example.pojo.dto.record.DeleteRecordDTO;
import com.example.pojo.dto.record.UpdateRecordDTO;
import com.example.pojo.entity.Field;
import com.example.service.IRecordService;
import com.example.utils.TableNameUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
public class RecordServiceImpl implements IRecordService {

    private final DynamicMapper dynamicMapper;
    private final FieldMapper fieldMapper;

    @Override
    @Transactional
    public void addRecord(AddRecordDTO addRecordDTO) {
        String tableEnName = addRecordDTO.getTableName();
        String sheetEnName = addRecordDTO.getSheetName();
        String finalTableName = TableNameUtil.merge(tableEnName, sheetEnName);
        if (dynamicMapper.checkTableExists(finalTableName) == null) {
            throw new BizException(StatusCodeEnum.TABLE_NOT_EXISTS);
        }
        LinkedHashMap<String, Object> record = addRecordDTO.getRecord();
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        // 将record中的键值转为英文
        record.forEach((fieldCnName, value) -> {
            Field field = fieldMapper.selectOne(new LambdaQueryWrapper<Field>()
                    .eq(Field::getTableEnName, tableEnName)
                    .eq(Field::getSheetEnName, sheetEnName)
                    .eq(Field::getFieldCnName, fieldCnName));
            if (field == null) {
                throw new BizException(StatusCodeEnum.FIELD_NOT_EXISTS);
            }
            result.put(field.getFieldEnName(), value);
        });
        dynamicMapper.addRecord(finalTableName, result);
    }

    @Override
    @Transactional
    public void deleteRecord(DeleteRecordDTO deleteRecordDTO) {
        String finalTableName = TableNameUtil.merge(deleteRecordDTO.getTableName(), deleteRecordDTO.getSheetName());
        if (dynamicMapper.checkTableExists(finalTableName) == null) {
            throw new BizException(StatusCodeEnum.TABLE_NOT_EXISTS);
        }
        dynamicMapper.deleteRecord(finalTableName, deleteRecordDTO.getId());
    }

    @Override
    @Transactional
    public void updateRecord(UpdateRecordDTO updateRecordDTO) {
        String tableEnName = updateRecordDTO.getTableName();
        String sheetEnName = updateRecordDTO.getSheetName();
        String finalTableName = TableNameUtil.merge(tableEnName, sheetEnName);
        if (dynamicMapper.checkTableExists(finalTableName) == null) {
            throw new BizException(StatusCodeEnum.TABLE_NOT_EXISTS);
        }
        LinkedHashMap<String, Object> record = updateRecordDTO.getRecord();
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        // 将record中的键值转为英文
        record.forEach((fieldCnName, value) -> {
            Field field = fieldMapper.selectOne(new LambdaQueryWrapper<Field>()
                    .eq(Field::getTableEnName, tableEnName)
                    .eq(Field::getSheetEnName, sheetEnName)
                    .eq(Field::getFieldCnName, fieldCnName));
            if (field == null) {
                throw new BizException(StatusCodeEnum.FIELD_NOT_EXISTS);
            }
            result.put(field.getFieldEnName(), value);
        });
        dynamicMapper.updateRecord(finalTableName, updateRecordDTO.getId(), result);
    }

    @Override
    @Transactional(readOnly = true)
    public LinkedHashMap<String, Object> getRecordById(String tableName, String sheetName, Long id) {
        String finalTableName = TableNameUtil.merge(tableName, sheetName);
        if (dynamicMapper.checkTableExists(finalTableName) == null) {
            throw new BizException(StatusCodeEnum.TABLE_NOT_EXISTS);
        }
        LinkedHashMap<String, Object> recordInfo = dynamicMapper.getRecordById(finalTableName, id);
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();

        // id除外的键改为中文
        recordInfo.forEach((fieldEnName, value) -> {
            if (!"id".equals(fieldEnName)) {
                String fieldCnName = fieldMapper.selectOne(new LambdaQueryWrapper<Field>()
                        .eq(Field::getTableEnName, tableName)
                        .eq(Field::getSheetEnName, sheetName)
                        .eq(Field::getFieldEnName, fieldEnName))
                        .getFieldCnName();
                result.put(fieldCnName, value);
            } else {
                result.put(fieldEnName, value);
            }
        });
        return result;
    }
}
