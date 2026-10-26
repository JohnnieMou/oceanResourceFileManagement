package com.example.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.annotation.DataPermission;
import com.example.config.properties.CacheProperties;
import com.example.config.properties.LogProperties;
import com.example.constant.*;
import com.example.context.BaseContext;
import com.example.enums.FilePathEnum;
import com.example.enums.StatusCodeEnum;
import com.example.exception.BizException;
import com.example.listener.ExcelListener;
import com.example.listener.FieldListener;
import com.example.manager.LockManager;
import com.example.mapper.*;
import com.example.pojo.dto.table.AddTableDTO;
import com.example.pojo.dto.table.UpdateTableDTO;
import com.example.pojo.entity.*;
import com.example.pojo.vo.TableInfoVO;
import com.example.service.IFileMappingService;
import com.example.service.ITableService;
import com.example.strategy.storage.StorageStrategyContext;
import com.example.utils.DataTypeUtil;
import com.example.utils.TableNameUtil;
import com.example.utils.TranslateUtil;
import com.example.handler.DatabaseErrorHandler;
import io.jsonwebtoken.Claims;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author
 * @since 2024-09-06
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TableServiceImpl extends ServiceImpl<TableMapper, Table> implements ITableService {

    private final DynamicMapper dynamicMapper;
    private final TableMapper tableMapper;
    private final SheetMapper sheetMapper;
    private final FieldMapper fieldMapper;
    private final TagMapper tagMapper;
    private final TableTagMapper tableTagMapper;
    private final LogProperties logProperties;
    private final HttpServletResponse response;
    private final RedisTemplate redisTemplate;
    private final CacheProperties cacheProperties;
    private final StorageStrategyContext storageStrategyContext;
    private final IFileMappingService fileMappingService;
    private final LockManager lockManager;
    private final UserMapper userMapper;
    private final DataPermissionMapper dataPermissionMapper;
    private final DatabaseErrorHandler databaseErrorHandler;

    @Override
    @Transactional
    @SneakyThrows
    public Map<String, LinkedHashMap<String, String>> uploadConfirm(MultipartFile excel, String tableName) {
        // 检查表是否存在
        Table table = tableMapper.selectOne(new LambdaQueryWrapper<Table>().eq(Table::getTableCnName, tableName));
        if (table == null) {
            throw new BizException(StatusCodeEnum.TABLE_NOT_EXISTS);
        }

        // 尝试获取锁
        if (!lockManager.tryLock(LockTypeConstant.TableLock, tableName)) {
            // 获取当前持有锁的用户信息
            LockInfo lockInfo = lockManager.getLockInfo(LockTypeConstant.TableLock, tableName)
                    .orElseThrow(() -> new BizException("获取锁信息失败"));
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, lockInfo.getUserId()));
            throw new BizException(String.format("用户%s正在导入该表，请稍后再试", user.getName()));
        }

        try {
            @Cleanup
            InputStream inputStream = excel.getInputStream();
            Map<String, LinkedHashMap<String, String>> fieldListMap = new HashMap<>();

            // 获取文件类型
            String extension = FilenameUtils.getExtension(excel.getOriginalFilename());
            ExcelTypeEnum excelType;

            // 判断文件类型
            if ("xls".equalsIgnoreCase(extension)) {
                excelType = ExcelTypeEnum.XLS;
            } else if ("xlsx".equalsIgnoreCase(extension)) {
                excelType = ExcelTypeEnum.XLSX;
            } else {
                throw new IllegalArgumentException("Unsupported file type: " + extension);
            }

            // 根据类型读取文件
            EasyExcel.read(inputStream, new FieldListener(fieldListMap))
                    .excelType(excelType)
                    .doReadAll();
            storageStrategyContext.executeUploadExcelStrategy(FilePathEnum.EXCEL.getPath(), excel, tableName, FileTypeConstant.EXCEL);
            return fieldListMap;
        } finally {
            // 确保释放锁
            lockManager.releaseLock(LockTypeConstant.TableLock, tableName);
        }
    }

    @Override
    @Transactional
    @SneakyThrows
    public void upload(Map<String, LinkedHashMap<String, String>> fieldListMap, String tableCnName, boolean isCover) {
        Object lock = lockManager.getLock(LockTypeConstant.TableLock, tableCnName);
        try {
            synchronized (lock) {
                MultipartFile excel = storageStrategyContext.executeGetExcelStrategy(FilePathEnum.EXCEL.getPath(), tableCnName);
                EasyExcel.read(excel.getInputStream(), new ExcelListener(tableCnName, fieldListMap, logProperties.isEnable(), isCover)).doReadAll();
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw e;
            }
            
            databaseErrorHandler.handleDatabaseError(e, tableCnName, this::dropTable);

            // 如果没有匹配到预期的错误格式，则抛出原始异常并回滚
            String tableEnName = tableMapper.selectOne(new LambdaQueryWrapper<Table>()
                            .eq(Table::getTableCnName, tableCnName))
                    .getTableEnName();
            dropTable(tableEnName);
            throw e;
        } finally {
            lockManager.releaseLock(LockTypeConstant.TableLock, tableCnName);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @SneakyThrows
    @DataPermission(tableCnName = "#tableCnName")
    public void download(String tableCnName) {
        String tableEnName;

        // 检查表是否存在
        try {
            tableEnName = tableMapper.selectOne(new LambdaQueryWrapper<Table>()
                            .eq(Table::getTableCnName, tableCnName))
                    .getTableEnName();
        } catch (Exception e) {
            throw new BizException(StatusCodeEnum.TABLE_NOT_EXISTS);
        }

        // 获取所有sheet
        List<Sheet> sheets = sheetMapper.selectList(new LambdaQueryWrapper<Sheet>()
                .eq(Sheet::getTableEnName, tableEnName));
        if (sheets.isEmpty()) {
            throw new BizException(StatusCodeEnum.TABLE_HAS_NO_SHEET);
        }

        // 设置响应头
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode(tableCnName, StandardCharsets.UTF_8);
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 写入Excel（多个sheet）
        OutputStream out = response.getOutputStream();
        ExcelWriter excelWriter = EasyExcel.write(out).build();

        try {
            // 遍历每个sheet写入数据
            for (Sheet sheet : sheets) {
                String sheetEnName = sheet.getSheetEnName();
                String sheetCnName = sheet.getSheetCnName();
                String finalTableName = TableNameUtil.merge(tableEnName, sheetEnName);

                // 获取中文表头
                List<Field> fields = fieldMapper.selectList(new LambdaQueryWrapper<Field>()
                        .eq(Field::getTableEnName, tableEnName)
                        .eq(Field::getSheetEnName, sheetEnName));
                List<List<String>> cnHeads = fields.stream()
                        .map(field -> Collections.singletonList(field.getFieldCnName()))
                        .toList();

                // 获取无id列的数据
                List<String> enHeads = dynamicMapper.getHead(finalTableName);
                enHeads.remove("id");
                List<LinkedHashMap<String, Object>> data = dynamicMapper.getByTableName(finalTableName, enHeads);

                // 创建新的sheet并写入数据
                WriteSheet writeSheet = EasyExcel.writerSheet(sheetCnName)
                        .head(cnHeads)
                        .build();

                // 将数据转换为List<List<String>>格式，日期类型格式化
                List<List<String>> rows = DataTypeUtil.dataToString(data);

                excelWriter.write(rows, writeSheet);
            }
        } finally {
            // 确保资源被释放
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }


    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public void dropTable(String tableEnName) {
        // 判断表是否存在
        if (tableMapper.selectOne(new LambdaQueryWrapper<Table>().eq(Table::getTableEnName, tableEnName)) == null) {
            throw new BizException(StatusCodeEnum.TABLE_NOT_EXISTS);
        }

        // 删除缓存
//        redisTemplate.delete(RedisKeyEnum.TABLE_LIST.getKey());

        String tableCnName = tableMapper.selectOne(new LambdaQueryWrapper<Table>()
                .eq(Table::getTableEnName, tableEnName)).getTableCnName();

        // 各表中删除相关数据
        tableTagMapper.delete(tableMapper.selectOne(new LambdaQueryWrapper<Table>()
                .eq(Table::getTableEnName, tableEnName)).getId());
        tableMapper.delete(new LambdaQueryWrapper<Table>().eq(Table::getTableEnName, tableEnName));
        sheetMapper.delete(new LambdaQueryWrapper<Sheet>().eq(Sheet::getTableEnName, tableEnName));
        fieldMapper.delete(new LambdaQueryWrapper<Field>().eq(Field::getTableEnName, tableEnName));
        dataPermissionMapper.delete(new LambdaQueryWrapper<com.example.pojo.entity.DataPermission>().eq(com.example.pojo.entity.DataPermission::getTableCnName, tableCnName));

        // 具体的每个sheet表删除
        List<String> tableNames = dynamicMapper.findTablesWithPrefix(tableEnName);
        tableNames.forEach(dynamicMapper::dropTable);

        // 删除文件
        FileMapping file = fileMappingService.lambdaQuery().eq(FileMapping::getOriginalFileName, tableCnName).one();
        if (file != null) {
            storageStrategyContext.executeDeleteStrategy(FilePathEnum.EXCEL.getPath(), file.getEncryptedFileName());
        }
    }

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public void addTable(AddTableDTO addTableDTO) {
        // 删除缓存
//        redisTemplate.delete(RedisKeyEnum.TABLE_LIST.getKey());

        // 判断表是否存在
        String tableCnName = addTableDTO.getTableName();
        if (tableMapper.selectOne(new LambdaQueryWrapper<Table>().eq(Table::getTableCnName, tableCnName)) != null) {
            throw new BizException(StatusCodeEnum.TABLE_ALREADY_EXISTS);
        }

        // 创建表实体
        Claims claims = BaseContext.getJwtClaims();
        Table table = Table.builder()
                .tableEnName(TranslateUtil.toEnglish(tableCnName))
                .tableCnName(tableCnName)
                .description(addTableDTO.getDescription())
                .isGuest(claims.get(JwtClaimsConstant.ROLE_CODE, Long.class) == RoleConstant.GUEST)
                .createUser(claims.get(JwtClaimsConstant.USER_ID, Long.class))
                .build();

        // 插入表
        tableMapper.insert(table);

        // 处理标签
        Long tableId = table.getId();
        List<String> tags = addTableDTO.getTags();
        if (CollUtil.isNotEmpty(tags)) {
            for (String tag : tags) {
                Tag theTag = tagMapper.selectOne(new LambdaQueryWrapper<Tag>().eq(Tag::getTagName, tag));
                if (theTag == null) {
                    tableTagMapper.delete(tableId);
                    tableMapper.deleteById(tableId);
                    throw new BizException(StatusCodeEnum.TAG_NOT_EXISTS);
                }
                tableTagMapper.insert(tableId, theTag.getId());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<TableInfoVO> listTable() {
        Boolean isGuest = BaseContext.getJwtClaims().get(JwtClaimsConstant.ROLE_CODE, Long.class) == RoleConstant.GUEST;
        // 非游客取缓存，缓存是所有的表
//        if (!isGuest) {
//            List<TableInfoVO> cache = (List<TableInfoVO>) redisTemplate.opsForValue().get(RedisKeyEnum.TABLE_LIST.getKey());
//            if (CollUtil.isNotEmpty(cache)) {
//                return cache;
//            }
//        }

        // 定义表列表，游客只能看到自己的表，非游客可以看到所有表
        List<Table> tables;
        if (isGuest) {
            tables = tableMapper.selectList(new LambdaQueryWrapper<Table>()
                    .eq(Table::getIsGuest, CommonConstant.True)
                    .eq(Table::getCreateUser, BaseContext.getUserId()));
        } else {
            // 非游客查询所有表
            tables = tableMapper.selectList(null);
        }

        // 将表实体列表转换为表信息视图对象列表
        List<TableInfoVO> result = tables == null ? Collections.emptyList() :
                tables.stream().map(this::buildTableInfoVO).collect(Collectors.toList());

        long ttl = cacheProperties.getTimeToLive();
//        redisTemplate.opsForValue().set(RedisKeyEnum.TABLE_LIST.getKey(), result, ttl, TimeUnit.MILLISECONDS);

        return result;
    }

    @Override
    @Transactional
    public void updateTable(UpdateTableDTO updateTableDTO) {
        String oldTableCnName = updateTableDTO.getOldTableName();

        // 检查旧表是否存在
        if (tableMapper.selectOne(new LambdaQueryWrapper<Table>().eq(Table::getTableCnName, oldTableCnName)) == null) {
            throw new BizException(StatusCodeEnum.TABLE_NOT_EXISTS);
        }

        String oleTableEnName = tableMapper.selectOne(new LambdaQueryWrapper<Table>()
                        .eq(Table::getTableCnName, oldTableCnName))
                .getTableEnName();

        // 检查新表名是否已存在
        String newTableCnName = updateTableDTO.getNewTableName();
        if (tableMapper.selectOne(new LambdaQueryWrapper<Table>().eq(Table::getTableCnName, newTableCnName)) != null) {
            throw new BizException(StatusCodeEnum.TABLE_ALREADY_EXISTS);
        }

        // 清空缓存
//        redisTemplate.delete(RedisKeyEnum.TABLE_LIST.getKey());

        String description = updateTableDTO.getDescription();
        List<String> tagNames = updateTableDTO.getTags();

        // 更新表与标签的关系
        if (CollUtil.isNotEmpty(tagNames)) {
            Long tableId = tableMapper.selectOne(new LambdaQueryWrapper<Table>()
                    .eq(Table::getTableEnName, oleTableEnName)).getId();
            tableTagMapper.delete(tableId);
            List<Long> tagIds = new ArrayList<>();
            for (String tagName : tagNames) {
                Tag tag = tagMapper.selectOne(new LambdaQueryWrapper<Tag>()
                        .eq(Tag::getTagName, tagName));
                if (tag != null) {
                    tagIds.add(tag.getId());
                }
                // 如果标签不存在，则创建新标签
                else {
                    tag = Tag.builder()
                            .tagName(tagName)
                            .build();
                    tagMapper.insertAndGetId(tag);
                    Long newTagId = tag.getId();
                    tagIds.add(newTagId);
                }
            }
            tableTagMapper.insertBatch(tableId, tagIds);
        }

        // 更新摘要
        if (!StringUtils.isBlank(description)) {
            tableMapper.update(new LambdaUpdateWrapper<Table>()
                    .eq(Table::getTableEnName, oleTableEnName)
                    .set(Table::getDescription, description));
        }

        // 更新表名
        if (!StringUtils.isBlank(newTableCnName)) {
            if (!oldTableCnName.equals(newTableCnName)) {
                String newTableEnName = TranslateUtil.toEnglish(newTableCnName);
                // table表中更新表名
                tableMapper.update(new LambdaUpdateWrapper<Table>()
                        .eq(Table::getTableEnName, oleTableEnName)
                        .set(Table::getTableEnName, newTableEnName)
                        .set(Table::getTableCnName, newTableCnName));
                // sheet表中更新表名
                sheetMapper.update(new LambdaUpdateWrapper<Sheet>()
                        .eq(Sheet::getTableEnName, oleTableEnName)
                        .set(Sheet::getTableEnName, newTableEnName));
                // field表中更新表名
                fieldMapper.update(new LambdaUpdateWrapper<Field>()
                        .eq(Field::getTableEnName, oleTableEnName)
                        .set(Field::getTableEnName, newTableEnName));
                // file_mapping表中更新表名
                fileMappingService.update(new LambdaUpdateWrapper<FileMapping>()
                        .eq(FileMapping::getOriginalFileName, oldTableCnName)
                        .set(FileMapping::getOriginalFileName, newTableCnName));

                // 具体的每个sheet表更新表名
                List<String> tableNameWithSheets = dynamicMapper.findTablesWithPrefix(oleTableEnName);
                for (String tableNameWithSheet : tableNameWithSheets) {
                    dynamicMapper.renameTable(tableNameWithSheet, tableNameWithSheet.replace(oleTableEnName, newTableEnName));
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TableInfoVO getTableInfo(String tableName) {
        Table table = tableMapper.selectOne(new LambdaQueryWrapper<Table>()
                .eq(Table::getTableCnName, tableName));
        if (table == null) {
            throw new BizException(StatusCodeEnum.TABLE_NOT_EXISTS);
        }
        return TableInfoVO.builder()
                .tableName(tableName)
                .description(table.getDescription())
                .tags(tableTagMapper.selectList(table.getId()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @SneakyThrows
    public void getTemplate(String tableCnName) {
        String tableEnName;

        // 检查表是否存在
        try {
            tableEnName = tableMapper.selectOne(new LambdaQueryWrapper<Table>()
                            .eq(Table::getTableCnName, tableCnName))
                    .getTableEnName();
        } catch (Exception e) {
            throw new BizException(StatusCodeEnum.TABLE_NOT_EXISTS);
        }

        // 获取所有sheet
        List<Sheet> sheets = sheetMapper.selectList(new LambdaQueryWrapper<Sheet>()
                .eq(Sheet::getTableEnName, tableEnName));
        if (sheets.isEmpty()) {
            throw new BizException(StatusCodeEnum.TABLE_HAS_NO_SHEET);
        }

        // 设置响应头
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode(tableCnName, StandardCharsets.UTF_8);
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 写入Excel（多个sheet）
        OutputStream out = response.getOutputStream();
        ExcelWriter excelWriter = EasyExcel.write(out).build();

        try {
            // 遍历每个sheet写入数据
            for (Sheet sheet : sheets) {
                String sheetEnName = sheet.getSheetEnName();
                String sheetCnName = sheet.getSheetCnName();

                // 获取中文表头
                List<Field> fields = fieldMapper.selectList(new LambdaQueryWrapper<Field>()
                        .eq(Field::getTableEnName, tableEnName)
                        .eq(Field::getSheetEnName, sheetEnName));
                List<List<String>> cnHeads = fields.stream()
                        .map(field -> Collections.singletonList(field.getFieldCnName()))
                        .toList();

                // 创建新的sheet并写入数据
                WriteSheet writeSheet = EasyExcel.writerSheet(sheetCnName)
                        .head(cnHeads)
                        .build();
                excelWriter.write(Collections.emptyList(), writeSheet);
            }
        } finally {
            // 确保资源被释放
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    private TableInfoVO buildTableInfoVO(Table table) {
        Long tableId = table.getId();
        List<String> tags = tableTagMapper.selectList(tableId);
        return TableInfoVO.builder()
                .tableName(table.getTableCnName())
                .description(table.getDescription())
                .tags(tags)
                .build();
    }


}
