package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.pojo.dto.PageDTO;
import com.example.pojo.entity.Notebook;
import com.example.pojo.entity.OperLog;
import com.example.pojo.vo.OperLogVO;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 操作日志记录 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2024-10-08
 */
@Mapper
public interface OperLogMapper extends BaseMapper<OperLog> {

    IPage<OperLogVO> pageQuery(Page<OperLog> page, PageDTO pageDTO);

}
