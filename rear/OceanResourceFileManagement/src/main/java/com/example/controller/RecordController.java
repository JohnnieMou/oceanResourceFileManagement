package com.example.controller;

import com.example.annotation.NameConvert;
import com.example.annotation.Log;
import com.example.annotation.RequireRoles;
import com.example.constant.MessageConstant;
import com.example.constant.RoleConstant;
import com.example.enums.BusinessTypeEnum;
import com.example.pojo.dto.record.AddRecordDTO;
import com.example.pojo.dto.record.DeleteRecordDTO;
import com.example.pojo.dto.record.UpdateRecordDTO;
import com.example.pojo.entity.Result;
import com.example.service.IRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/record")
@Api(tags = "记录相关接口")
@RequiredArgsConstructor
public class RecordController {

    private final IRecordService recordService;

    @Log(title = "添加记录", businessType = BusinessTypeEnum.INSERT)
    @PostMapping("/add")
    @ApiOperation(value = "添加记录", notes = "添加记录，记录无需携带id")
    @NameConvert
    public Result<?> addRecord(@RequestBody @Valid AddRecordDTO addRecordDTO) {
        recordService.addRecord(addRecordDTO);
        return Result.success(MessageConstant.ADD_SUCCESS);
    }

    @RequireRoles(value = { RoleConstant.ADMIN }, allowGuest = true)
    @Log(title = "删除记录", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/delete")
    @ApiOperation(value = "删除记录", notes = "删除记录")
    @NameConvert
    public Result<?> deleteRecord(@RequestBody @Valid DeleteRecordDTO deleteRecordDTO) {
        recordService.deleteRecord(deleteRecordDTO);
        return Result.success(MessageConstant.DELETE_SUCCESS);
    }

    @Log(title = "修改记录", businessType = BusinessTypeEnum.UPDATE)
    @PutMapping("/update")
    @ApiOperation(value = "修改记录", notes = "修改记录，记录无需携带id，将id值存到id键中")
    @NameConvert
    public Result<?> updateRecord(@RequestBody @Valid UpdateRecordDTO updateRecordDTO) {
        recordService.updateRecord(updateRecordDTO);
        return Result.success(MessageConstant.UPDATE_SUCCESS);
    }

    @GetMapping("/getById")
    @ApiOperation(value = "根据id获取记录", notes = "根据id获取记录")
    @NameConvert
    public Result<LinkedHashMap<String, Object>> getRecordById(@RequestParam @Schema(example = "示例表") String tableName,
                                                               @RequestParam @Schema(example = "Sheet1") String sheetName,
                                                               @RequestParam @Schema(example = "1") Long id) {
        return Result.success(recordService.getRecordById(tableName, sheetName, id));
    }
}
