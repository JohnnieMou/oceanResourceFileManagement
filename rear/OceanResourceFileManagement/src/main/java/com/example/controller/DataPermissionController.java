package com.example.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.annotation.Log;
import com.example.annotation.RequireRoles;
import com.example.constant.MessageConstant;
import com.example.constant.RoleConstant;
import com.example.enums.BusinessTypeEnum;
import com.example.pojo.dto.PageDTO;
import com.example.pojo.dto.dataPermission.AddDataPermissionDTO;
import com.example.pojo.dto.dataPermission.DataPermissionDTO;
import com.example.pojo.dto.dataPermission.UpdateDataPermissionDTO;
import com.example.pojo.entity.Result;
import com.example.pojo.vo.DataPermissionVO;
import com.example.service.IDataPermissionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 数据权限表 前端控制器
 * </p>
 *
 * @author ccc212
 * @since 2025-01-13
 */
@RestController
@RequestMapping("/data-permission")
@Api(tags = "数据权限相关接口")
@RequiredArgsConstructor
public class DataPermissionController {

    private final IDataPermissionService dataPermissionService;

    @RequireRoles({ RoleConstant.ADMIN })
    @Log(title = "添加数据权限", businessType = BusinessTypeEnum.INSERT)
    @PostMapping("/add")
    @ApiOperation(value = "添加数据权限", notes = "可批量添加；对游客指定哪些表的站位和年份在哪些范围可以查看，添加权限后游客变为普通用户;开始年份不填默认为 0，结束年份不填默认为 9999，开始航次不填默认为0，结束航次不填默认为999999")
    public Result<?> addDataPermission(@RequestBody AddDataPermissionDTO addDataPermissionDTO) {
        dataPermissionService.addDataPermission(addDataPermissionDTO);
        return Result.success(MessageConstant.ADD_SUCCESS);
    }

    @RequireRoles({ RoleConstant.ADMIN })
    @Log(title = "删除数据权限", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/delete")
    @ApiOperation(value = "删除数据权限", notes = "可批量删除；删除指定用户原有的数据权限，全部删完将变回游客")
    public Result<?> deleteDataPermission(@RequestBody DataPermissionDTO dataPermissionDTO) {
        dataPermissionService.deleteDataPermission(dataPermissionDTO);
        return Result.success(MessageConstant.DELETE_SUCCESS);
    }

    @RequireRoles({ RoleConstant.ADMIN })
    @Log(title = "修改数据权限", businessType = BusinessTypeEnum.UPDATE)
    @PutMapping("/update")
    @ApiOperation(value = "修改数据权限", notes = "根据用户id和表名，修改起止年份和起止航次；不给表名则，对所有表统一修改")
    public Result<?> updateDataPermission(@RequestBody UpdateDataPermissionDTO updateDataPermissionDTO) {
        dataPermissionService.updateDataPermission(updateDataPermissionDTO);
        return Result.success(MessageConstant.UPDATE_SUCCESS);
    }

    @RequireRoles({ RoleConstant.ADMIN })
    @GetMapping("/list")
    @ApiOperation(value = "获取数据权限列表", notes = "分页查询用户的数据权限")
    public Result<IPage<DataPermissionVO>> listDataPermission(PageDTO pageDTO) {
        return Result.success(dataPermissionService.listDataPermission(pageDTO));
    }

    @RequireRoles({ RoleConstant.ADMIN })
    @GetMapping("/listNoPermissionTables/{userId}")
    @ApiOperation(value = "获取未授权表列表", notes = "获取用户还未拥有权限的表列表")
    public Result<List<String>> listNoPermissionTables(@PathVariable Long userId) {
        return Result.success(dataPermissionService.listNoPermissionTables(userId));
    }

    @RequireRoles({ RoleConstant.ADMIN })
    @GetMapping("/getByUserId/{userId}")
    @ApiOperation(value = "获取用户数据权限", notes = "根据用户id获取用户数据权限")
    public Result<List<DataPermissionVO>> getByUserId(@PathVariable Long userId) {
        return Result.success(dataPermissionService.getByUserId(userId));
    }
}
