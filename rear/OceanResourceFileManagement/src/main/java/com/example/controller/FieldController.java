package com.example.controller;


import com.example.annotation.NameConvert;
import com.example.annotation.Log;
import com.example.annotation.RequireRoles;
import com.example.constant.MessageConstant;
import com.example.constant.RoleConstant;
import com.example.enums.BusinessTypeEnum;
import com.example.pojo.dto.field.AddFieldDTO;
import com.example.pojo.dto.field.DeleteFieldDTO;
import com.example.pojo.dto.field.UpdateFieldDTO;
import com.example.pojo.entity.Result;
import com.example.pojo.vo.FieldVO;
import com.example.service.IFieldService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 
 * @since 2024-09-06
 */
@RestController
@RequestMapping("/field")
@Api(tags = "字段相关接口")
@RequiredArgsConstructor
public class FieldController {

    private final IFieldService fieldService;

    @Log(title = "添加字段", businessType = BusinessTypeEnum.INSERT)
    @PostMapping("/add")
    @ApiOperation(value = "添加字段", notes = "给指定表指定sheet创建不存在的字段")
    @NameConvert
    public Result<?> addField(@RequestBody @Valid AddFieldDTO addFieldDTO) {
        fieldService.addField(addFieldDTO);
        return Result.success(MessageConstant.FIELD_ADD_SUCCESS);
    }

    @RequireRoles(value = { RoleConstant.ADMIN }, allowGuest = true)
    @Log(title = "删除字段", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/delete")
    @ApiOperation(value = "删除字段", notes = "给指定表指定sheet删除存在的字段")
    @NameConvert
    public Result<?> deleteField(@RequestBody @Valid DeleteFieldDTO deleteFieldDTO) {
        fieldService.deleteField(deleteFieldDTO);
        return Result.success(MessageConstant.DELETE_SUCCESS);
    }

    @Log(title = "修改字段", businessType = BusinessTypeEnum.UPDATE)
    @PutMapping("/update")
    @ApiOperation(value = "修改字段", notes = "给指定表指定sheet修改存在的字段")
    @NameConvert
    public Result<?> updateField(@RequestBody @Valid UpdateFieldDTO updateFieldDTO) {
        fieldService.updateField(updateFieldDTO);
        return Result.success(MessageConstant.FIELD_UPDATE_SUCCESS);
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取字段列表", notes = "获取指定表指定sheet所有字段的列表")
    @NameConvert
    public Result<List<FieldVO>> listField(@RequestParam @ApiParam(example = "示例表") String tableName,
                                           @RequestParam @ApiParam(example = "Sheet1") String sheetName) {
        return Result.success(fieldService.listField(tableName, sheetName));
    }

}
