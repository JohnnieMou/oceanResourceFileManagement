package com.example.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.annotation.Log;
import com.example.annotation.RequireRoles;
import com.example.constant.MessageConstant;
import com.example.constant.RoleConstant;
import com.example.enums.BusinessTypeEnum;
import com.example.pojo.dto.PageDTO;
import com.example.pojo.entity.Result;
import com.example.pojo.vo.OperLogVO;
import com.example.service.IOperLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * <p>
 * 操作日志记录 前端控制器
 * </p>
 *
 * @author 
 * @since 2024-10-13
 */
@RestController
@RequestMapping("/operLog")
@Api(tags = "操作日志相关接口")
@RequiredArgsConstructor
public class OperLogController {

    private final IOperLogService operLogService;

    @RequireRoles({ RoleConstant.ADMIN })
    @GetMapping("/page")
    @ApiOperation(value = "分页查询", notes = "倒序展示操作日志，即最新的在最前面")
    public Result<IPage<OperLogVO>> page(@Valid PageDTO pageDTO) {
        return Result.success(operLogService.pageQuery(pageDTO));
    }

    @RequireRoles({ RoleConstant.ADMIN })
    @Log(title = "清除日志", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/clear")
    @ApiOperation(value = "清除日志", notes = "清除所有日志")
    public Result<?> clear() {
        operLogService.clear();
        return Result.success(MessageConstant.DELETE_SUCCESS);
    }
}
