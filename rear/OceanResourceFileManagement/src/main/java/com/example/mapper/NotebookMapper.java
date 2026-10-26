package com.example.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.pojo.dto.PageDTO;
import com.example.pojo.entity.Notebook;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pojo.vo.NotebookInfoVO;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 记事本表 Mapper 接口
 * </p>
 *
 * @author ccc212
 * @since 2025-01-04
 */
@Mapper
public interface NotebookMapper extends BaseMapper<Notebook> {

    IPage<NotebookInfoVO> listNotebook(Page<Notebook> page, PageDTO pageDTO);
}
