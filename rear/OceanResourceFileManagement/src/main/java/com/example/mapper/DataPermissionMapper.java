package com.example.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.pojo.dto.PageDTO;
import com.example.pojo.entity.DataPermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pojo.vo.DataPermissionVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 数据权限表 Mapper 接口
 * </p>
 *
 * @author ccc212
 * @since 2025-01-13
 */
@Mapper
public interface DataPermissionMapper extends BaseMapper<DataPermission> {

    IPage<DataPermissionVO> pageQuery(Page<DataPermission> page, PageDTO pageDTO);

    List<String> listNoPermissionTables(Long userId);

    List<DataPermissionVO> getByUserId(Long userId);
}
