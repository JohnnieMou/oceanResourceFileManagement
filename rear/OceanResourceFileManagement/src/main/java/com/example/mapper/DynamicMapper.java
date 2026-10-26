package com.example.mapper;

import com.example.pojo.vo.ExcelColumnStatisticsVO;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Mapper
public interface DynamicMapper {

    // 删除表
    @Delete("drop table ${finalTableName}")
    void dropTable(String finalTableName);

    // 创建表
    void createTable(String finalTableName, List<String> columns, Map<Integer, String> geoFieldName);

    // 添加数据
    void save(String finalTableName, List<Map<String, String>> list);

    // 逐个获取
    @Select("select `${fieldName}` from ${tableName} limit ${index},1")
    String getByIndex(int index, String tableName, String fieldName);

    // 获取表头
    List<String> getHead(String finalTableName);

    // 获取所有数据
    List<LinkedHashMap<String, Object>> getByTableName(String finalTableName, List<String> enHeads);

    // 获取所有前缀带有表名的最终表名
    List<String> findTablesWithPrefix(String tableName);

    // 分页查询
    Page<LinkedHashMap<String, Object>> pageQuery(String finalTableName);

    // 统计
    List<ExcelColumnStatisticsVO> statistics(String tableName, String sheetEnName, String finalTableName, List<String> numberfields);

    // 增加字段
    void addField(String finalTableName, String fieldEnName, String fieldType);

    // 删除字段
    void deleteField(String finalTableName, String fieldEnName);

    // 修改字段类型
    void updateFieldType(String finalTableName, String oldFieldEnName, String fieldType);

    // 检查表是否存在
    @Select("show tables like #{finalTableName}")
    String checkTableExists(String finalTableName);

    // 条件查询
    Page<LinkedHashMap<String, Object>> conditionQuery(
            String finalTableName,
            List<String> stringEnFields, List<String> fieldValues,
            List<String> numberEnFields, List<BigDecimal> numbers,
            List<String> rangeEnFields, List<BigDecimal> maxs, List<BigDecimal> mins,
            List<String> dateEnFields, List<LocalDate> begins, List<LocalDate> ends
   );

    // 获取上一次查询的总记录数
    long getTotalCount();

    // 重命名表
    void renameTable(String oldTableName, String newTableName);

    // 添加记录
    void addRecord(String finalTableName, LinkedHashMap<String, Object> record);

    // 删除记录
    @Delete("delete from ${finalTableName} where id = ${id}")
    void deleteRecord(String finalTableName, Long id);

    // 修改记录
    void updateRecord(String finalTableName, Long id,LinkedHashMap<String, Object> record);

    LinkedHashMap<String, Object> getRecordById(String finalTableName, Long id);
}
