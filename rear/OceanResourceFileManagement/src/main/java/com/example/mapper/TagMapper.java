package com.example.mapper;

import com.example.pojo.entity.Tag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 
 * @since 2024-09-25
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    Long insertAndGetId(Tag tag);
}
