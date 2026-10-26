package com.example.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.annotation.Log;
import com.example.annotation.RequireRoles;
import com.example.constant.MessageConstant;
import com.example.constant.RoleConstant;
import com.example.enums.BusinessTypeEnum;
import com.example.pojo.dto.PageDTO;
import com.example.pojo.dto.notebook.AddNotebookDTO;
import com.example.pojo.dto.notebook.UpdateNotebookDTO;
import com.example.pojo.entity.Result;
import com.example.pojo.vo.NotebookInfoVO;
import com.example.service.INotebookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

/**
 * <p>
 * 记事表 前端控制器
 * </p>
 *
 * @author ccc212
 * @since 2025-01-04
 */
@RestController
@RequestMapping("/notebook")
@Api(tags = "记事本相关接口")
@RequiredArgsConstructor
public class NotebookController {

    private final INotebookService notebookService;

    @RequireRoles({RoleConstant.ADMIN, RoleConstant.USER})
    @Log(title = "添加记事", businessType = BusinessTypeEnum.INSERT)
    @PostMapping("/add")
    @ApiOperation(value = "添加记事", notes = "标题可以重复，标题和内容不能为空")
    public Result<?> addNotebook(@RequestBody @Valid AddNotebookDTO addNotebookDTO) {
        notebookService.addNotebook(addNotebookDTO);
        return Result.success(MessageConstant.ADD_SUCCESS);
    }

    @RequireRoles({RoleConstant.ADMIN, RoleConstant.USER})
    @Log(title = "删除记事", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/delete")
    @ApiOperation(value = "删除记事", notes = "根据记事id删除记事")
    public Result<?> deleteNotebook(@RequestParam @NotBlank(message = "标题名不能为空") @ApiParam(example = "实例标题") Long id) {
        notebookService.deleteNotebook(id);
        return Result.success(MessageConstant.DELETE_SUCCESS);
    }

    @RequireRoles({RoleConstant.ADMIN, RoleConstant.USER})
    @Log(title = "修改记事", businessType = BusinessTypeEnum.UPDATE)
    @PutMapping("/update")
    @ApiOperation(value = "修改记事", notes = "必须传记事id，新标题和内容，传了就会修改，没传的不改动")
    public Result<?> updateNotebook(@RequestBody @Valid UpdateNotebookDTO updateNotebookDTO) {
        notebookService.updateNotebook(updateNotebookDTO);
        return Result.success(MessageConstant.UPDATE_SUCCESS);
    }

    @RequireRoles({RoleConstant.ADMIN, RoleConstant.USER})
    @GetMapping("/list")
    @ApiOperation(value = "获取记事列表", notes = "分页查询记事")
    public Result<IPage<NotebookInfoVO>> listNotebook(@Valid PageDTO pageDTO) {
        return Result.success(notebookService.listNotebook(pageDTO));
    }

}
