package com.example.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.annotation.DataPermission;
import com.example.constant.FileTypeConstant;
import com.example.constant.LockTypeConstant;
import com.example.constant.SeparatorConstant;
import com.example.enums.DataTypeEnum;
import com.example.enums.FilePathEnum;
import com.example.enums.RedisKeyEnum;
import com.example.enums.StatusCodeEnum;
import com.example.exception.BizException;
import com.example.listener.FieldListener;
import com.example.listener.SheetListener;
import com.example.manager.LockManager;
import com.example.mapper.*;
import com.example.pojo.dto.PageDTO;
import com.example.pojo.dto.sheet.ExcelPageDTO;
import com.example.pojo.dto.sheet.FilterDTO;
import com.example.pojo.entity.*;
import com.example.pojo.vo.AttachmentVO;
import com.example.pojo.vo.ExcelColumnStatisticsVO;
import com.example.pojo.vo.FilterResultVO;
import com.example.service.IFileMappingService;
import com.example.service.ISheetService;
import com.example.service.ITableService;
import com.example.strategy.storage.StorageStrategyContext;
import com.example.utils.DataTypeUtil;
import com.example.utils.TableNameUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
@RequiredArgsConstructor
@Slf4j
public class SheetServiceImpl extends ServiceImpl<SheetMapper, Sheet> implements ISheetService {

    private final DynamicMapper dynamicMapper;
    private final TableMapper tableMapper;
    private final ITableService tableService;
    private final SheetMapper sheetMapper;
    private final FieldMapper fieldMapper;
    private final HttpServletResponse response;
    private final StorageStrategyContext storageStrategyContext;
    private final LockManager lockManager;
    private final UserMapper userMapper;
    private final IFileMappingService fileMappingService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final VoyageAttachmentMapper voyageAttachmentMapper;

    @Override
    @Transactional(readOnly = true)
    public List<String> getSheets(String tableName) {
        List<Sheet> sheets = sheetMapper.selectList(new LambdaQueryWrapper<Sheet>().eq(Sheet::getTableEnName, tableName));
        List<String> sheetNames = sheets.stream().map(Sheet::getSheetCnName).toList();
        return sheetNames;
    }

    @Override
    @Transactional(readOnly = true)
    @DataPermission(tableEnName = "#excelPageDTO.tableName")
    public PageResult pageQuery(ExcelPageDTO excelPageDTO) {
        String tableName = excelPageDTO.getTableName();
        String sheetName = excelPageDTO.getSheetName();
        String finalTableName = TableNameUtil.merge(tableName, sheetName);

        PageHelper.startPage(excelPageDTO.getPage(), excelPageDTO.getPageSize());
        Page<LinkedHashMap<String, Object>> page;
        try {
            page = dynamicMapper.pageQuery(finalTableName);
        } catch (Exception e) {
            throw new BizException(StatusCodeEnum.TABLE_OR_SHEET_NOT_EXISTS_COMBINATION);
        }

        List<LinkedHashMap<String, Object>> resultList = page.getResult();
        return new PageResult(page.getTotal(), resultList);
    }

    @Override
    @Transactional(readOnly = true)
//    @Cacheable(value = "statistics", key = "T(com.example.utils.CacheUtil).getKeyWithUserId(#tableEnName, #sheetEnName)")
    @DataPermission(tableEnName = "#tableEnName")
    public List<ExcelColumnStatisticsVO> statistics(String tableEnName, String sheetEnName) {
        String finalTableName = TableNameUtil.merge(tableEnName, sheetEnName);
        List<Field> fields = fieldMapper.selectList(new LambdaQueryWrapper<Field>()
                .eq(Field::getTableEnName, tableEnName)
                .eq(Field::getSheetEnName, sheetEnName)
                .eq(Field::getDataType, DataTypeEnum.NUMBER.getEnName()));
        if (CollUtil.isEmpty(fields)) {
            throw new BizException(StatusCodeEnum.NO_NUMBER_FIELD);
        }
        List<String> numberfields = fields.stream().map(Field::getFieldEnName).toList();
        List<ExcelColumnStatisticsVO> statistics = dynamicMapper.statistics(tableEnName, sheetEnName, finalTableName, numberfields);
        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    @DataPermission(tableEnName = "#filterDTO.tableName")
    public FilterResultVO filter(FilterDTO filterDTO) {
        String tableEnName = filterDTO.getTableName();
        String sheetEnName = filterDTO.getSheetName();
        String finalTableName = TableNameUtil.merge(tableEnName, sheetEnName);

        int page = filterDTO.getPage();
        int pageSize = filterDTO.getPageSize();

        Page<LinkedHashMap<String, Object>> result;

        // 固定值查询(支持模糊查询)
        List<String> stringFields = filterDTO.getFields();
        List<String> stringEnFields = null;
        try {
            stringEnFields = stringFields.stream().map(field -> fieldMapper.selectOne(new LambdaQueryWrapper<Field>()
                    .eq(Field::getTableEnName, tableEnName)
                    .eq(Field::getSheetEnName, sheetEnName)
                    .eq(Field::getFieldCnName, field)).getFieldEnName()).toList();
        } catch (Exception e) {
            throw new BizException(StatusCodeEnum.FIELD_NAME_IS_NULL_OR_FIELD_NOT_EXISTS);
        }
        List<String> fieldValues = filterDTO.getFieldValues();

        // 精确数值查询
        List<String> numberFields = filterDTO.getNumberFields();
        List<String> numberEnFields = null;
        try {
            numberEnFields = numberFields.stream().map(field -> fieldMapper.selectOne(new LambdaQueryWrapper<Field>()
                    .eq(Field::getTableEnName, tableEnName)
                    .eq(Field::getSheetEnName, sheetEnName)
                    .eq(Field::getFieldCnName, field)).getFieldEnName()).toList();
        } catch (Exception e) {
            throw new BizException(StatusCodeEnum.FIELD_NAME_IS_NULL_OR_FIELD_NOT_EXISTS);
        }
        List<BigDecimal> numbers = filterDTO.getNumbers();

        // 范围查询
        List<String> rangeFields = filterDTO.getRangeFields();
        List<String> rangeEnFields = null;
        try {
            rangeEnFields = rangeFields.stream().map(field -> fieldMapper.selectOne(new LambdaQueryWrapper<Field>()
                    .eq(Field::getTableEnName, tableEnName)
                    .eq(Field::getSheetEnName, sheetEnName)
                    .eq(Field::getFieldCnName, field)).getFieldEnName()).toList();
        } catch (Exception e) {
            throw new BizException(StatusCodeEnum.FIELD_NAME_IS_NULL_OR_FIELD_NOT_EXISTS);
        }
        List<BigDecimal> maxs = filterDTO.getMaxs();
        List<BigDecimal> mins = filterDTO.getMins();
        maxs.replaceAll(max -> max == null ? BigDecimal.valueOf(Double.MAX_VALUE) : max);
        mins.replaceAll(min -> min == null ? BigDecimal.valueOf(Double.MIN_VALUE) : min);

        // 日期查询
        List<String> dateFields = filterDTO.getDateFields();
        List<String> dateEnFields = null;
        try {
            dateEnFields = dateFields.stream().map(field -> fieldMapper.selectOne(new LambdaQueryWrapper<Field>()
                    .eq(Field::getTableEnName, tableEnName)
                    .eq(Field::getSheetEnName, sheetEnName)
                    .eq(Field::getFieldCnName, field)).getFieldEnName()).toList();
        } catch (Exception e) {
            throw new BizException(StatusCodeEnum.FIELD_NAME_IS_NULL_OR_FIELD_NOT_EXISTS);
        }
        List<LocalDate> begins = filterDTO.getBegin();
        List<LocalDate> ends = filterDTO.getEnd();

        // 拼接查询条件
        result = dynamicMapper.conditionQuery(finalTableName,
                stringEnFields, fieldValues,
                numberEnFields, numbers,
                rangeEnFields, maxs, mins,
                dateEnFields, begins, ends
        ); // 不分页查询，获取所有结果
        long totalCount = dynamicMapper.getTotalCount();
        List<LinkedHashMap<String, Object>> allResult = result.getResult().stream().collect(Collectors.toList());
        List<LinkedHashMap<String, Object>> paginatedResult = result.getResult().stream()
                .skip((long) (page - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());
        PageResult pageResult = new PageResult(totalCount, paginatedResult);

        // 生成唯一的key并存储到Redis中
        String key = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(RedisKeyEnum.FILTER_RESULT.getKey() + ":" + key, allResult, 10, java.util.concurrent.TimeUnit.MINUTES);

        // 返回结果和key
        FilterResultVO filterResultVO = new FilterResultVO();
        filterResultVO.setPageResult(pageResult);
        filterResultVO.setDownloadKey(key);
        return filterResultVO;
    }

    @Override
    @Transactional(readOnly = true)
    @SneakyThrows
    @DataPermission(tableCnName = "#tableCnName")
    public void download(String tableCnName, String sheetCnName) {
        String tableEnName;
        String sheetEnName;

        // 检查表是否存在
        try {
            tableEnName = tableMapper.selectOne(new LambdaQueryWrapper<Table>().eq(Table::getTableCnName, tableCnName)).getTableEnName();
        } catch (Exception e) {
            throw new BizException(StatusCodeEnum.TABLE_NOT_EXISTS);
        }

        // 检查表和sheet的组合是否存在
        try {
            sheetEnName = sheetMapper.selectList(new LambdaQueryWrapper<Sheet>()
                            .eq(Sheet::getTableEnName, tableEnName)
                            .eq(Sheet::getSheetCnName, sheetCnName))
                    .get(0)
                    .getSheetEnName();
        } catch (Exception e) {
            throw new BizException(StatusCodeEnum.TABLE_OR_SHEET_NOT_EXISTS_COMBINATION);
        }

        // 获取最终表名
        String finalTableName = TableNameUtil.merge(tableEnName, sheetEnName);

        // 获取中文表头
        List<Field> fields = fieldMapper.selectList(new LambdaQueryWrapper<Field>()
                .eq(Field::getTableEnName, tableEnName)
                .eq(Field::getSheetEnName, sheetEnName));
        List<List<String>> cnHeads = fields.stream().map(field -> {
            String fieldCnName = field.getFieldCnName();
            return Collections.singletonList(fieldCnName);
        }).toList();

        // 获取无id列的数据
        List<String> enHeads = dynamicMapper.getHead(finalTableName);
        enHeads.remove("id");
        List<LinkedHashMap<String, Object>> data = dynamicMapper.getByTableName(finalTableName, enHeads);

        // 设置响应头
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode(tableCnName
                + SeparatorConstant.TABLE_SHEET_SEPARATOR
                + sheetCnName, StandardCharsets.UTF_8);
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 写入Excel
        OutputStream out = response.getOutputStream();
        ExcelWriter excelWriter = EasyExcel.write(out).build();

        try {
            WriteSheet writeSheet = EasyExcel.writerSheet(sheetCnName)
                    .head(cnHeads)
                    .build();
            List<List<String>> rows = DataTypeUtil.dataToString(data);
            excelWriter.write(rows, writeSheet);
            excelWriter.finish();
        } finally {
            // 确保资源被释放
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

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
            EasyExcel.read(inputStream, new FieldListener(fieldListMap)).doReadAll();
            storageStrategyContext.executeUploadExcelStrategy(FilePathEnum.EXCEL.getPath(), excel, "temp_" + tableName, FileTypeConstant.TEMP);
            return fieldListMap;
        } finally {
            // 确保释放锁
            lockManager.releaseLock(LockTypeConstant.TableLock, tableName);
        }
    }

    @Override
    @Transactional
    @SneakyThrows
    public void upload(Map<String, LinkedHashMap<String, String>> fieldListMap, String tableCnName) {
        Object lock = lockManager.getLock(LockTypeConstant.SheetLock, tableCnName);
        try {
            synchronized (lock) {
                String uploadName = "temp_" + tableCnName;
                MultipartFile excel = storageStrategyContext.executeGetExcelStrategy(FilePathEnum.EXCEL.getPath(), uploadName);
                String encryptedFileName = fileMappingService.getEncryptedFileName(uploadName);
                storageStrategyContext.executeDeleteStrategy(FilePathEnum.EXCEL.getPath(), encryptedFileName);
                // 检查是否有重复sheet名
                EasyExcel.read(excel.getInputStream(), new FieldListener(tableCnName)).doReadAll();
                // 追加sheet
                EasyExcel.read(excel.getInputStream(), new SheetListener(tableCnName, fieldListMap)).doReadAll();
            }
        } catch (Exception e) {
            if (e instanceof BizException) {
                throw e;
            }

            String message = e.getMessage();

            // 提取表名
            Pattern tablePattern = Pattern.compile("insert into (\\w+)");
            Matcher tableMatcher = tablePattern.matcher(message);

            // 提取错误值和字段名
            Pattern valuePattern = Pattern.compile("Incorrect \\w+ value: '(.+)' for column '(\\w+)'");
            Matcher valueMatcher = valuePattern.matcher(message);

            if (tableMatcher.find() && valueMatcher.find()) {
                String finalTableName = tableMatcher.group(1);
                String errorValue = valueMatcher.group(1);
                String fieldEnName = valueMatcher.group(2);

                String sheetEnName = TableNameUtil.getSheetName(finalTableName);
                String tableEnName = TableNameUtil.getTableName(finalTableName);
                String sheetCnName = sheetMapper.selectOne(new LambdaQueryWrapper<Sheet>()
                                .eq(Sheet::getTableEnName, tableEnName)
                                .eq(Sheet::getSheetEnName, sheetEnName))
                        .getSheetCnName();
                String fieldCnName = fieldMapper.selectOne(new LambdaQueryWrapper<Field>()
                                .eq(Field::getTableEnName, tableEnName)
                                .eq(Field::getSheetEnName, sheetEnName)
                                .eq(Field::getFieldEnName, fieldEnName))
                        .getFieldCnName();

                tableService.dropTable(tableEnName);
                throw new BizException(String.format(
                        "sheet " + "%s" + " 中的字段 " + "%s" + " 存在值 " + "%s" + " 格式不正确，请检查数据类型",
                        sheetCnName,
                        fieldCnName,
                        errorValue
                ));
            }
            throw e;
        } finally {
            lockManager.releaseLock(LockTypeConstant.SheetLock, tableCnName);
        }
    }

    @Override
    @Transactional(readOnly = true)
    @SneakyThrows
    public void getTemplate(String tableCnName, String sheetCnName) {
        String tableEnName;
        String sheetEnName;

        // 检查表是否存在
        try {
            tableEnName = tableMapper.selectOne(new LambdaQueryWrapper<Table>().eq(Table::getTableCnName, tableCnName)).getTableEnName();
        } catch (Exception e) {
            throw new BizException(StatusCodeEnum.TABLE_NOT_EXISTS);
        }

        // 检查表和sheet的组合是否存在
        try {
            sheetEnName = sheetMapper.selectList(new LambdaQueryWrapper<Sheet>()
                            .eq(Sheet::getTableEnName, tableEnName)
                            .eq(Sheet::getSheetCnName, sheetCnName))
                    .get(0)
                    .getSheetEnName();
        } catch (Exception e) {
            throw new BizException(StatusCodeEnum.TABLE_OR_SHEET_NOT_EXISTS_COMBINATION);
        }

        // 获取中文表头
        List<Field> fields = fieldMapper.selectList(new LambdaQueryWrapper<Field>()
                .eq(Field::getTableEnName, tableEnName)
                .eq(Field::getSheetEnName, sheetEnName));
        List<List<String>> cnHeads = fields.stream().map(field -> {
            String fieldCnName = field.getFieldCnName();
            return Collections.singletonList(fieldCnName);
        }).toList();

        // 设置响应头
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode(tableCnName
                + SeparatorConstant.TABLE_SHEET_SEPARATOR
                + sheetCnName, StandardCharsets.UTF_8);
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 写入Excel，只写入表头
        OutputStream out = response.getOutputStream();
        ExcelWriter excelWriter = EasyExcel.write(out).build();

        try {
            WriteSheet writeSheet = EasyExcel.writerSheet(sheetCnName)
                    .head(cnHeads)
                    .build();
            excelWriter.write(Collections.emptyList(), writeSheet); // 不写入数据，只写入表头
            excelWriter.finish();
        } finally {
            // 确保资源被释放
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }

    @Override
    public void delete(String tableName, String sheetName) {
        sheetMapper.delete(new LambdaQueryWrapper<Sheet>()
                .eq(Sheet::getTableEnName, tableName)
                .eq(Sheet::getSheetEnName, sheetName));
        fieldMapper.delete(new LambdaQueryWrapper<Field>()
                .eq(Field::getTableEnName, tableName)
                .eq(Field::getSheetEnName, sheetName));
        dynamicMapper.dropTable(TableNameUtil.merge(tableName, sheetName));
    }

    @Override
    @SneakyThrows
    public void uploadAttachment(MultipartFile file, String location, Integer voyage) {
        // 尝试获取锁
        String fileName = location + SeparatorConstant.FILE_NAME_SEPARATOR
                + voyage + SeparatorConstant.FILE_NAME_SEPARATOR
                + file.getOriginalFilename();
        if (!lockManager.tryLock(LockTypeConstant.TableLock, fileName)) {
            // 获取当前持有锁的用户信息
            LockInfo lockInfo = lockManager.getLockInfo(LockTypeConstant.TableLock, fileName)
                    .orElseThrow(() -> new BizException("获取锁信息失败"));
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getId, lockInfo.getUserId()));
            throw new BizException(String.format("用户%s正在导入相关附件，请稍后再试", user.getName()));
        }

        try {
            storageStrategyContext.executeUploadExcelStrategy(FilePathEnum.ATTACHMENT.getPath(), file, fileName, FileTypeConstant.ATTACHMENT);
            Long fileId = fileMappingService.getFileId(fileName);
            voyageAttachmentMapper.insert(VoyageAttachment.builder()
                    .location(location)
                    .voyage(voyage)
                    .fileId(fileId)
                    .build());
        } finally {
            // 确保释放锁
            lockManager.releaseLock(LockTypeConstant.TableLock, fileName);
        }
    }

    @Override
    public void deleteAttachment(List<Integer> ids) {
        if (CollUtil.isEmpty(ids)) {
            throw new BizException(StatusCodeEnum.FILE_NOT_EXISTS);
        }

        // 获取文件映射id
        List<Long> fileMappingIds = voyageAttachmentMapper.selectList(new LambdaQueryWrapper<VoyageAttachment>()
                .in(VoyageAttachment::getId, ids))
                .stream()
                .map(VoyageAttachment::getFileId)
                .toList();

        if (CollUtil.isEmpty(fileMappingIds)) {
            throw new BizException(StatusCodeEnum.FILE_NOT_EXISTS);
        }

        // 批量查询文件映射
        List<FileMapping> fileMappings = fileMappingService.lambdaQuery()
                .in(FileMapping::getId, fileMappingIds)
                .list();

        // 批量删除文件
        if (CollUtil.isNotEmpty(fileMappings)) {
            storageStrategyContext.executeBatchDeleteStrategy(
                    FilePathEnum.ATTACHMENT.getPath(),
                    fileMappings.stream().map(FileMapping::getEncryptedFileName).toList()
            );
        }

        // 批量删除航次附件记录
        voyageAttachmentMapper.deleteByIds(ids);
    }

    @Override
    public void downloadAttachment(Long id) {
        VoyageAttachment voyageAttachment = voyageAttachmentMapper.selectById(id);
        if (voyageAttachment == null) {
            throw new BizException(StatusCodeEnum.FILE_NOT_EXISTS);
        }

        FileMapping fileMapping = fileMappingService.lambdaQuery().eq(FileMapping::getId, voyageAttachment.getFileId()).one();
        if (fileMapping == null) {
            throw new BizException(StatusCodeEnum.FILE_NOT_EXISTS);
        }
        String fileName = fileMapping.getOriginalFileName();
        storageStrategyContext.executeDownloadStrategy(FilePathEnum.ATTACHMENT.getPath(), fileName, response);
    }

    @Override
    public PageResult attachmentPage(PageDTO pageDTO) {
        PageHelper.startPage(pageDTO.getPage(), pageDTO.getPageSize());
        Page<AttachmentVO> page = voyageAttachmentMapper.pageQuery();
        page.getResult().forEach(attachmentVO -> {
            attachmentVO.setFileLink(storageStrategyContext.executeGetPresignUrlStrategy(
                    FilePathEnum.ATTACHMENT.getPath(),
                    attachmentVO.getFileName(),
                    response
            ));
        });
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional(readOnly = true)
    @SneakyThrows
    public void downloadFilterResult(String tableEnName, String sheetCnName, String key) {
        String sheetEnName = sheetMapper.selectOne(new LambdaQueryWrapper<Sheet>()
                        .eq(Sheet::getTableEnName, tableEnName)
                        .eq(Sheet::getSheetCnName, sheetCnName))
                .getSheetEnName();
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

        // 从Redis中获取条件查询结果
        List<LinkedHashMap<String, Object>> data = (List<LinkedHashMap<String, Object>>) redisTemplate.opsForValue().get(RedisKeyEnum.FILTER_RESULT.getKey() + ":" + key);
        if (CollUtil.isEmpty(data)) {
            throw new BizException(StatusCodeEnum.RESULT_NOT_EXISTS);
        }
        // 除掉id字段
        // TODO 优化除掉id的方式
        data.forEach(record -> record.remove("id"));

        // 设置响应头
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("条件查询结果", StandardCharsets.UTF_8);
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 写入Excel
        OutputStream out = response.getOutputStream();
        ExcelWriter excelWriter = EasyExcel.write(out).build();

        try {
            WriteSheet writeSheet = EasyExcel.writerSheet(sheetCnName)
                    .head(cnHeads)
                    .build();
            List<List<String>> rows = DataTypeUtil.dataToString(data);
            excelWriter.write(rows, writeSheet);
            excelWriter.finish();
        } finally {
            // 确保资源被释放
            if (excelWriter != null) {
                excelWriter.finish();
            }
        }
    }
}
