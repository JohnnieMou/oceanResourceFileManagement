package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.constant.RoleConstant;
import com.example.enums.StatusCodeEnum;
import com.example.exception.BizException;
import com.example.mapper.DataPermissionMapper;
import com.example.mapper.TableMapper;
import com.example.mapper.UserMapper;
import com.example.pojo.dto.PageDTO;
import com.example.pojo.dto.dataPermission.AddDataPermissionDTO;
import com.example.pojo.dto.dataPermission.DataPermissionDTO;
import com.example.pojo.dto.dataPermission.UpdateDataPermissionDTO;
import com.example.pojo.entity.DataPermission;
import com.example.pojo.entity.Table;
import com.example.pojo.entity.User;
import com.example.pojo.vo.DataPermissionVO;
import com.example.service.IDataPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 数据权限表 服务实现类
 * </p>
 *
 * @author ccc212
 * @since 2025-01-13
 */
@Service
@RequiredArgsConstructor
public class DataPermissionServiceImpl extends ServiceImpl<DataPermissionMapper, DataPermission> implements IDataPermissionService {

    private final DataPermissionMapper dataPermissionMapper;
    private final TableMapper tableMapper;
    private final UserMapper userMapper;

    @Override
    public void addDataPermission(AddDataPermissionDTO addDataPermissionDTO) {
        // 检查用户是否存在
        Long userId = addDataPermissionDTO.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException(StatusCodeEnum.USER_NOT_FOUND);
        }

        // 若用户为游客，则改为普通用户
        if (user.getRoleCode() == RoleConstant.GUEST) {
            user.setRoleCode(RoleConstant.USER);
            userMapper.updateById(user);
        }

        // 可查看年份和航次范围
        Integer yearStart = addDataPermissionDTO.getYearStart() != null ? addDataPermissionDTO.getYearStart() : 0;
        Integer yearEnd = addDataPermissionDTO.getYearEnd() != null ? addDataPermissionDTO.getYearEnd() : 9999;
        Integer voyageStart = addDataPermissionDTO.getVoyageStart() != null ? addDataPermissionDTO.getVoyageStart() : 0;
        Integer voyageEnd = addDataPermissionDTO.getVoyageEnd() != null ? addDataPermissionDTO.getVoyageEnd() : 999999;

        // 检查年份和航次范围是否正确
        if (yearStart > yearEnd) {
            throw new BizException(StatusCodeEnum.YEAR_START_GREATER_THAN_YEAR_END);
        }
        if (voyageStart > voyageEnd) {
            throw new BizException(StatusCodeEnum.VOYAGE_START_GREATER_THAN_VOYAGE_END);
        }

        // 检查表是否存在
        List<String> tableCnNames = addDataPermissionDTO.getTableNames();
        tableCnNames.forEach(tableCnName -> {
            Table table = tableMapper.selectOne(new LambdaQueryWrapper<Table>().eq(Table::getTableCnName, tableCnName));
            if (table == null) {
                throw new BizException(StatusCodeEnum.TABLE_NOT_EXISTS);
            }

            // 插入数据权限
            dataPermissionMapper.insert(DataPermission.builder()
                    .userId(userId)
                    .tableCnName(tableCnName)
                    .yearStart(yearStart)
                    .yearEnd(yearEnd)
                    .voyageStart(voyageStart)
                    .voyageEnd(voyageEnd)
                    .build());
        });
    }

    @Override
    public void deleteDataPermission(DataPermissionDTO dataPermissionDTO) {
        // 检查用户是否存在
        Long userId = checkUserExists(dataPermissionDTO);

        // 检查表是否存在
        List<String> tableCnNames = dataPermissionDTO.getTableNames();
        tableCnNames.forEach(tableCnName -> {
            Table table = tableMapper.selectOne(new LambdaQueryWrapper<Table>().eq(Table::getTableCnName, tableCnName));
            if (table == null) {
                throw new BizException(StatusCodeEnum.TABLE_NOT_EXISTS);
            }

            // 删除数据权限
            dataPermissionMapper.delete(new LambdaQueryWrapper<DataPermission>()
                    .eq(DataPermission::getUserId, userId)
                    .eq(DataPermission::getTableCnName, tableCnName));
        });
    }

    @Override
    public void updateDataPermission(UpdateDataPermissionDTO updateDataPermissionDTO) {
        // 检查用户是否存在
        Long userId = checkUserExists(updateDataPermissionDTO);

        // 是否指定表名
        List<String> tableCnNames = updateDataPermissionDTO.getTableNames();
        if (tableCnNames == null) {
            // 不指定则全部更新
            dataPermissionMapper.update(new LambdaUpdateWrapper<DataPermission>()
                    .eq(DataPermission::getUserId, userId)
                    .set(updateDataPermissionDTO.getYearStart() != null, 
                         DataPermission::getYearStart, 
                         updateDataPermissionDTO.getYearStart())
                    .set(updateDataPermissionDTO.getYearEnd() != null, 
                         DataPermission::getYearEnd, 
                         updateDataPermissionDTO.getYearEnd())
                    .set(updateDataPermissionDTO.getVoyageStart() != null, 
                         DataPermission::getVoyageStart, 
                         updateDataPermissionDTO.getVoyageStart())
                    .set(updateDataPermissionDTO.getVoyageEnd() != null, 
                         DataPermission::getVoyageEnd, 
                         updateDataPermissionDTO.getVoyageEnd()));
        }
        else {
            dataPermissionMapper.update(new LambdaUpdateWrapper<DataPermission>()
                    .eq(DataPermission::getUserId, userId)
                    .in(DataPermission::getTableCnName, tableCnNames)
                    .set(updateDataPermissionDTO.getYearStart() != null,
                            DataPermission::getYearStart,
                            updateDataPermissionDTO.getYearStart())
                    .set(updateDataPermissionDTO.getYearEnd() != null,
                            DataPermission::getYearEnd,
                            updateDataPermissionDTO.getYearEnd())
                    .set(updateDataPermissionDTO.getVoyageStart() != null,
                            DataPermission::getVoyageStart,
                            updateDataPermissionDTO.getVoyageStart())
                    .set(updateDataPermissionDTO.getVoyageEnd() != null,
                            DataPermission::getVoyageEnd,
                            updateDataPermissionDTO.getVoyageEnd()));
        }
    }

    @Override
    public IPage<DataPermissionVO> listDataPermission(PageDTO pageDTO) {
        Page<DataPermission> page = new Page<>(pageDTO.getPage(), pageDTO.getPageSize());
        IPage<DataPermissionVO> resultPage = dataPermissionMapper.pageQuery(page, pageDTO);
        return resultPage;
    }

    @Override
    public List<String> listNoPermissionTables(Long userId) {
        return dataPermissionMapper.listNoPermissionTables(userId);
    }

    @Override
    public List<DataPermissionVO> getByUserId(Long userId) {
        return dataPermissionMapper.getByUserId(userId);
    }

    // 检查用户是否存在
    private Long checkUserExists(DataPermissionDTO dto) {
        Long userId = dto.getUserId();
        if (userMapper.selectById(userId) == null) {
            throw new BizException(StatusCodeEnum.USER_NOT_FOUND);
        }
        return userId;
    }
}
