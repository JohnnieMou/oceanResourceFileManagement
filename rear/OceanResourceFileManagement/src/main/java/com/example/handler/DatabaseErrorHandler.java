package com.example.handler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.enums.StatusCodeEnum;
import com.example.exception.BizException;
import com.example.mapper.FieldMapper;
import com.example.mapper.SheetMapper;
import com.example.pojo.entity.Field;
import com.example.pojo.entity.Sheet;
import com.example.utils.TableNameUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据库错误处理工具类，用于解析数据库异常并生成友好的错误消息
 */
@Component
@RequiredArgsConstructor
public class DatabaseErrorHandler {

    private final SheetMapper sheetMapper;
    private final FieldMapper fieldMapper;

    /**
     * 处理数据库错误，提取错误信息并生成友好的异常消息
     * @param e 原始异常
     * @param tableCnName 表中文名
     * @param dropTableCallback 删除表的回调函数
     */
    public void handleDatabaseError(Exception e, String tableCnName, Consumer<String> dropTableCallback) {
        String message = e.getMessage();
        
        // 提取表名（针对 insert 错误）
        Pattern tablePattern = Pattern.compile("insert into (\\w+)");
        Matcher tableMatcher = tablePattern.matcher(message);

        // 提取错误值和字段名（针对一般数据类型错误）
        Pattern valuePattern = Pattern.compile("Incorrect \\w+ value: '(.+)' for column '(\\w+)'");
        Matcher valueMatcher = valuePattern.matcher(message);

        // 提取表名（对于 alter table 语句）
        Pattern alterTablePattern = Pattern.compile("alter table ([\\w_]+) modify");
        Matcher alterTableMatcher = alterTablePattern.matcher(message.toLowerCase());

        // 提取日期错误信息（针对日期格式错误）
        Pattern datePattern = Pattern.compile("Incorrect date value: '([^']+)' for column '([^']+)'");
        Matcher dateMatcher = datePattern.matcher(message);
        
        // 处理日期类型错误
        if (dateMatcher.find()) {
            String errorValue = dateMatcher.group(1);
            String fieldEnName = dateMatcher.group(2);
            String finalTableName = "";
            
            // 尝试从 alter table 语句获取表名
            if (alterTableMatcher.find()) {
                finalTableName = alterTableMatcher.group(1);
            } 
            // 如果上面没找到，尝试从 insert into 获取表名
            else if (tableMatcher.find()) {
                finalTableName = tableMatcher.group(1);
            }
            
            if (!finalTableName.isEmpty()) {
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

                if (dropTableCallback != null) {
                    dropTableCallback.accept(tableEnName);
                }
                throw new BizException(String.format(
                        "sheet “%s” 中的字段 “%s” 存在值 “%s” 格式不正确，请使用正确的日期格式(如: YYYY.MM.DD)；或者下载后覆盖上传，选用日期类型，格式会自动转换",
                        sheetCnName,
                        fieldCnName,
                        errorValue
                ));
            }
        }
        // 处理一般数据类型错误
        else if (tableMatcher.find() && valueMatcher.find()) {
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

            dropTableCallback.accept(tableEnName);
            throw new BizException(String.format(
                    "sheet “%s” 中的字段 “%s” 存在值 “%s” 格式不正确，请检查数据类型",
                    sheetCnName,
                    fieldCnName,
                    errorValue
            ));
        }
    }
}