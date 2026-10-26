package com.example.mapper;

import com.example.pojo.entity.Table;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 
 * @since 2024-09-06
 */
@Mapper
public interface TableMapper extends BaseMapper<Table> {
    // 批量获取英文表名
    List<String> getTableEnNames(List<String> tableCnNames);
}
