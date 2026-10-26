package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.constant.LockTypeConstant;
import com.example.enums.StatusCodeEnum;
import com.example.exception.BizException;
import com.example.handler.DatabaseErrorHandler;
import com.example.manager.LockManager;
import com.example.mapper.DynamicMapper;
import com.example.mapper.FieldMapper;
import com.example.mapper.TableMapper;
import com.example.pojo.dto.field.AddFieldDTO;
import com.example.pojo.dto.field.DeleteFieldDTO;
import com.example.pojo.dto.field.UpdateFieldDTO;
import com.example.pojo.entity.Field;
import com.example.pojo.entity.Table;
import com.example.pojo.vo.FieldVO;
import com.example.service.IFieldService;
import com.example.utils.DataTypeUtil;
import com.example.utils.TableNameUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author
 * @since 2024-09-06
 */
@Service
@RequiredArgsConstructor
public class FieldServiceImpl extends ServiceImpl<FieldMapper, Field> implements IFieldService {

    private final DynamicMapper dynamicMapper;
    private final FieldMapper fieldMapper;
    private final LockManager lockManager;
    private final TableMapper tableMapper;
    private final DatabaseErrorHandler databaseErrorHandler;

    @Override
    @Transactional
    public void addField(AddFieldDTO addFieldDTO) {
        String tableEnName = addFieldDTO.getTableName();
        String sheetEnName = addFieldDTO.getSheetName();
        String fieldCnName = addFieldDTO.getFieldName();
        String fieldType = addFieldDTO.getFieldType();

        // 判断字段是否存在
        if (fieldMapper.selectOne(new LambdaQueryWrapper<Field>()
                .eq(Field::getTableEnName, tableEnName)
                .eq(Field::getSheetEnName, sheetEnName)
                .eq(Field::getFieldCnName, fieldCnName)) != null) {
            throw new BizException(StatusCodeEnum.FIELD_ALREADY_EXISTS);
        }

        Object lock = lockManager.getLock(LockTypeConstant.FieldLock, tableEnName + "_" + sheetEnName + "_" + fieldCnName);
        try {
            synchronized (lock) {
                String finalTableName = TableNameUtil.merge(tableEnName, sheetEnName);

                // 选取符合条件且id最大的记录的字段英文名
                String fieldEnName = fieldMapper.selectOne(new LambdaQueryWrapper<Field>()
                        .eq(Field::getTableEnName, tableEnName)
                        .eq(Field::getSheetEnName, sheetEnName)
                        .orderByDesc(Field::getId)
                        .last("limit 1"))
                        .getFieldEnName();

                // 将fieldEnName中_后面的数字加一
                fieldEnName = fieldEnName.substring(0, fieldEnName.lastIndexOf("_") + 1)
                        + (Integer.parseInt(fieldEnName.substring(fieldEnName.lastIndexOf("_") + 1)) + 1);

                // 添加字段
                try {
                    dynamicMapper.addField(finalTableName, fieldEnName, DataTypeUtil.mapDataType(fieldType));
                    fieldMapper.insert(Field.builder()
                            .tableEnName(tableEnName)
                            .sheetEnName(sheetEnName)
                            .fieldCnName(fieldCnName)
                            .fieldEnName(fieldEnName)
                            .dataType(fieldType)
                            .build());
                } catch (Exception e) {
                    try {
                        dynamicMapper.deleteField(finalTableName, fieldEnName);
                        fieldMapper.delete(new LambdaQueryWrapper<Field>()
                                .eq(Field::getTableEnName, tableEnName)
                                .eq(Field::getSheetEnName, sheetEnName)
                                .eq(Field::getFieldEnName, fieldEnName));
                    } catch (Exception ex) {
                        log.error("回滚字段失败", ex);
                    }
                    throw new BizException("添加字段失败：" + e.getMessage());
                }
            }
        } finally {
            lockManager.releaseLock(LockTypeConstant.FieldLock, tableEnName + "_" + sheetEnName + "_" + fieldCnName);
        }
    }

    @Override
    @Transactional
    public void deleteField(DeleteFieldDTO deleteFieldDTO) {
        String tableEnName = deleteFieldDTO.getTableName();
        String sheetEnName = deleteFieldDTO.getSheetName();
        String fieldCnName = deleteFieldDTO.getFieldName();
        Field field = fieldMapper.selectOne(new LambdaQueryWrapper<Field>()
                .eq(Field::getTableEnName, tableEnName)
                .eq(Field::getSheetEnName, sheetEnName)
                .eq(Field::getFieldCnName, fieldCnName));

        // 判断字段是否存在
        if (field == null) {
            throw new BizException(StatusCodeEnum.FIELD_NOT_EXISTS);
        }

        Object lock = lockManager.getLock(LockTypeConstant.FieldLock, tableEnName + "_" + sheetEnName + "_" + fieldCnName);
        try {
            synchronized (lock) {
                String finalTableName = TableNameUtil.merge(tableEnName, sheetEnName);
                String fieldEnName = field.getFieldEnName();

                // 删除字段
                try {
                    dynamicMapper.deleteField(finalTableName, fieldEnName);
                    fieldMapper.delete(new LambdaQueryWrapper<Field>()
                            .eq(Field::getTableEnName, tableEnName)
                            .eq(Field::getSheetEnName, sheetEnName)
                            .eq(Field::getFieldEnName, fieldEnName));
                } catch (Exception e) {
                    try {
                        dynamicMapper.addField(finalTableName, fieldEnName, getOriginalFieldType(tableEnName, sheetEnName, fieldEnName));
                    } catch (Exception ex) {
                        log.error("回滚字段删除失败", ex);
                    }
                    throw new BizException("删除字段失败：" + e.getMessage());
                }
            }
        } finally {
            lockManager.releaseLock(LockTypeConstant.FieldLock, tableEnName + "_" + sheetEnName + "_" + fieldCnName);
        }
    }

    @Override
    @Transactional
    public void updateField(UpdateFieldDTO updateFieldDTO) {
        String tableEnName = updateFieldDTO.getTableName();
        String sheetEnName = updateFieldDTO.getSheetName();
        String oldFieldCnName = updateFieldDTO.getOldFieldName();
        String newFieldCnName = updateFieldDTO.getNewFieldName();
        String fieldType = updateFieldDTO.getFieldType();

        // 如果新字段名和字段类型都为空，则不进行任何操作
        if (StringUtils.isAllBlank(newFieldCnName, fieldType) ||
                oldFieldCnName.equals(newFieldCnName) && StringUtils.isBlank(fieldType)) {
            throw new BizException("无任何变化");
        }

        // 判断旧字段是否存在
        if (fieldMapper.selectOne(new LambdaQueryWrapper<Field>()
                .eq(Field::getTableEnName, tableEnName)
                .eq(Field::getSheetEnName, sheetEnName)
                .eq(Field::getFieldCnName, oldFieldCnName)) == null) {
            throw new BizException(StatusCodeEnum.FIELD_NOT_EXISTS);
        }

        // 判断新字段是否已存在
        if (fieldMapper.selectOne(new LambdaQueryWrapper<Field>()
                .eq(Field::getTableEnName, tableEnName)
                .eq(Field::getSheetEnName, sheetEnName)
                .eq(Field::getFieldCnName, newFieldCnName)) != null && !oldFieldCnName.equals(newFieldCnName)) {
            throw new BizException(StatusCodeEnum.FIELD_ALREADY_EXISTS);
        }

        Object lock = lockManager.getLock(LockTypeConstant.FieldLock, tableEnName + "_" + sheetEnName + "_" + oldFieldCnName);
        try {
            synchronized (lock) {
                String tableCnName = tableMapper.selectOne(new LambdaQueryWrapper<Table>()
                        .eq(Table::getTableEnName, tableEnName))
                        .getTableCnName();
                String finalTableName = TableNameUtil.merge(tableEnName, sheetEnName);
                boolean fieldChange = !StringUtils.isBlank(newFieldCnName) && !oldFieldCnName.equals(newFieldCnName);
                boolean dataTypeChange = !StringUtils.isBlank(fieldType);
                String oldFieldEnName = fieldMapper.selectOne(new LambdaQueryWrapper<Field>()
                                .eq(Field::getTableEnName, tableEnName)
                                .eq(Field::getSheetEnName, sheetEnName)
                                .eq(Field::getFieldCnName, oldFieldCnName))
                        .getFieldEnName();

                // 字段名和字段类型都修改
                if (fieldChange && dataTypeChange) {
                    // 更新字段类型
                    try {
                        dynamicMapper.updateFieldType(finalTableName, oldFieldEnName, DataTypeUtil.mapDataType(fieldType));
                    } catch (Exception e) {
                        databaseErrorHandler.handleDatabaseError(e, tableCnName, null);
//                        throw new BizException(StatusCodeEnum.DATA_TYPE_NOT_MATCH);
                    }

                    // 更新字段名
                    fieldMapper.update(new LambdaUpdateWrapper<Field>()
                            .eq(Field::getTableEnName, tableEnName)
                            .eq(Field::getSheetEnName, sheetEnName)
                            .eq(Field::getFieldCnName, oldFieldCnName)
                            .set(Field::getFieldCnName, newFieldCnName)
                            .set(Field::getDataType, fieldType));
                }
                // 字段类型修改
                else if (dataTypeChange) {
                    // 更新字段类型
                    try {
                        dynamicMapper.updateFieldType(finalTableName, oldFieldEnName, DataTypeUtil.mapDataType(fieldType));
                    } catch (Exception e) {
                        databaseErrorHandler.handleDatabaseError(e, tableCnName, null);
//                        throw new BizException(StatusCodeEnum.DATA_TYPE_NOT_MATCH);
                    }
                    fieldMapper.update(new LambdaUpdateWrapper<Field>()
                            .eq(Field::getTableEnName, tableEnName)
                            .eq(Field::getSheetEnName, sheetEnName)
                            .eq(Field::getFieldCnName, oldFieldCnName)
                            .set(Field::getDataType, fieldType));
                }
                // 字段名修改
                else if (fieldChange) {
                    // 更新字段名
                    fieldMapper.update(new LambdaUpdateWrapper<Field>()
                            .eq(Field::getTableEnName, tableEnName)
                            .eq(Field::getSheetEnName, sheetEnName)
                            .eq(Field::getFieldCnName, oldFieldCnName)
                            .set(Field::getFieldCnName, newFieldCnName));
                }
            }
        } finally {
            lockManager.releaseLock(LockTypeConstant.FieldLock, tableEnName + "_" + sheetEnName + "_" + oldFieldCnName);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FieldVO> listField(String tableName, String sheetName) {
        List<Field> fields = fieldMapper.selectList(new LambdaQueryWrapper<Field>()
                .eq(Field::getTableEnName, tableName)
                .eq(Field::getSheetEnName, sheetName)
        );

        return fields.stream().map(field -> FieldVO.builder()
                .fieldName(field.getFieldCnName())
                .fieldType(field.getDataType())
                .build()).toList();
    }

    private String getOriginalFieldType(String tableEnName, String sheetEnName, String fieldEnName) {
        Field field = fieldMapper.selectOne(new LambdaQueryWrapper<Field>()
                .eq(Field::getTableEnName, tableEnName)
                .eq(Field::getSheetEnName, sheetEnName)
                .eq(Field::getFieldEnName, fieldEnName));
        return DataTypeUtil.mapDataType(field.getDataType());
    }
}
