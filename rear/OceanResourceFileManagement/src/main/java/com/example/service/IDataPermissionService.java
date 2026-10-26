package com.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.pojo.dto.PageDTO;
import com.example.pojo.dto.dataPermission.AddDataPermissionDTO;
import com.example.pojo.dto.dataPermission.DataPermissionDTO;
import com.example.pojo.dto.dataPermission.UpdateDataPermissionDTO;
import com.example.pojo.entity.DataPermission;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.pojo.vo.DataPermissionVO;

import java.util.List;

/**
 * <p>
 * 数据权限表 服务类
 * </p>
 *
 * @author ccc212
 * @since 2025-01-13
 */
public interface IDataPermissionService extends IService<DataPermission> {

    void addDataPermission(AddDataPermissionDTO addDataPermissionDTO);

    void deleteDataPermission(DataPermissionDTO dataPermissionDTO);

    void updateDataPermission(UpdateDataPermissionDTO updateDataPermissionDTO);

    IPage<DataPermissionVO> listDataPermission(PageDTO pageDTO);

    List<String> listNoPermissionTables(Long userId);

    List<DataPermissionVO> getByUserId(Long userId);
}
