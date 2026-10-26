package com.example.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface TableTagMapper {

    @Insert("insert into t_table_tag(table_id, tag_id) values(#{tableId}, #{tagId})")
    void insert(Long tableId, Long tagId);

    void insertBatch(Long tableId, List<Long> tagIds);

    @Delete("delete from t_table_tag where table_id = #{tableId}")
    void delete(Long tableId);

    @Select("select t.tag_name from t_table_tag tt join t_tag t on tt.table_id = #{tableId} and tt.tag_id = t.id")
    List<String> selectList(Long tableId);
}
