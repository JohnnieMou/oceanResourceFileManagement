package com.example.controller;


import com.example.annotation.LimitAccess;
import com.example.annotation.Log;
import com.example.annotation.NameConvert;
import com.example.annotation.RequireRoles;
import com.example.constant.MessageConstant;
import com.example.constant.RoleConstant;
import com.example.enums.BusinessTypeEnum;
import com.example.enums.RedisKeyEnum;
import com.example.pojo.dto.table.AddTableDTO;
import com.example.pojo.dto.table.UpdateTableDTO;
import com.example.pojo.entity.Result;
import com.example.pojo.vo.TableInfoVO;
import com.example.service.ITableService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author
 * @since 2024-09-06
 */
@RestController
@RequestMapping("/table")
@Api(tags = "表相关接口")
@RequiredArgsConstructor
@Validated
public class TableController {

    private final ITableService tableService;

    @Log(title = "创建表", businessType = BusinessTypeEnum.CREATE)
    @PostMapping("/create")
    @ApiOperation(value = "创建表", notes = "上传Excel前需要先创建表；标签要选择已创建的标签（可以用获取标签列表接口得到所有标签供用户选择）")
    public Result<?> createTable(@RequestBody @Valid AddTableDTO addTableDTO) {
        tableService.addTable(addTableDTO);
        return Result.success(MessageConstant.ADD_SUCCESS);
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取表列表", notes = "获取所有表的列表")
    public Result<List<TableInfoVO>> listTable() {
        return Result.success(tableService.listTable());
    }

    @LimitAccess(key = RedisKeyEnum.UPLOAD_EXCEL, times = 10)
    @Log(title = "上传（确认表字段类型）", businessType = BusinessTypeEnum.CONFIRM)
    @PostMapping("/uploadConfirm/{tableName}")
    @ApiOperation(value = "上传（确认表字段类型）", notes = "上传Excel前需要先确认表字段类型，返回的字段类型是Excel中每个Sheet的表头的字段类型（默认全为字符串），用户可以修改后再上传")
    public Result<Map<String, LinkedHashMap<String, String>>> uploadConfirm(@RequestParam MultipartFile excel,
                                                                            @PathVariable @ApiParam(example = "示例表") String tableName) {
        return Result.success(tableService.uploadConfirm(excel, tableName));
    }

    @Log(title = "上传（覆盖）", businessType = BusinessTypeEnum.COVER)
    @PostMapping("/uploadCover/{tableName}")
    @ApiOperation(value = "上传（覆盖）", notes = "第一次上传必须为覆盖上传，将删除原来表的所有内容然后换为新上传表的内容")
    public Result<?> uploadCover(
            @RequestBody Map<String, LinkedHashMap<String, String>> fieldListMap,
            @PathVariable @NotBlank(message = "表名不能为空") @ApiParam(example = "示例表") String tableName) {
        tableService.upload(fieldListMap, tableName, true);
        return Result.success(MessageConstant.UPLOAD_SUCCESS);
    }

    @Log(title = "上传（追加）", businessType = BusinessTypeEnum.APPEND)
    @PostMapping("/uploadAppend/{tableName}")
    @ApiOperation(value = "上传（追加）", notes = "将保留原来表的内容，然后将新上传表的内容追加到原来表的后面（目前只支持同字段的表的追加）,字段类型继承原表的")
    public Result<?> uploadAppend(@PathVariable @ApiParam(example = "示例表") String tableName) {
        tableService.upload(null, tableName, false);
        return Result.success(MessageConstant.UPLOAD_SUCCESS);
    }

    @Log(title = "下载整个Excel", businessType = BusinessTypeEnum.DOWNLOAD)
    @PostMapping("/download")
    @ApiOperation(value = "下载整个Excel", notes = "下载整个Excel")
    public void download(@RequestParam @NotBlank(message = "表名不能为空") @ApiParam(example = "示例表") String tableName) {
        tableService.download(tableName);
    }

    @Log(title = "获取整个Excel的模板", businessType = BusinessTypeEnum.DOWNLOAD)
    @PostMapping("/getTemplate")
    @ApiOperation(value = "获取整个Excel的模板", notes = "获取整个Excel的模板")
    public void getTemplate(@RequestParam @NotBlank(message = "表名不能为空") @ApiParam(example = "示例表") String tableName) {
        tableService.getTemplate(tableName);
    }

    @RequireRoles(value = { RoleConstant.ADMIN }, allowGuest = true)
    @Log(title = "删除表", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/{tableName}")
    @ApiOperation(value = "删除表", notes = "物理删除")
    @NameConvert(sheetToEnglish = false)
    public Result<?> deleteTable(@PathVariable @ApiParam(example = "示例表") String tableName) {
        tableService.dropTable(tableName);
        return Result.success(MessageConstant.DELETE_SUCCESS);
    }

    @GetMapping("/check/{tableName}")
    @ApiOperation(value = "检查表是否存在", notes = "检查表的存在，以防用户进入到未创建的表的界面（可以存入到本地，然后删除表或修改信息时清理）")
    @NameConvert(sheetToEnglish = false)
    public Result<?> checkTable(@PathVariable @ApiParam(example = "示例表") String tableName) {
        return Result.success();
    }

    @GetMapping("/info")
    @ApiOperation(value = "获取表信息", notes = "获取指定表的信息")
    public Result<TableInfoVO> getTableInfo(@RequestParam @NotBlank(message = "表名不能为空") @ApiParam(example = "示例表") String tableName) {
        return Result.success(tableService.getTableInfo(tableName));
    }

    @Log(title = "修改信息", businessType = BusinessTypeEnum.UPDATE)
    @PutMapping("/update")
    @ApiOperation(value = "修改信息", notes = "修改表名、表描述、表标签；返回表已存在表示新表已有同名表，返回表不存在表示旧表不存在")
    public Result<?> updateTable(@RequestBody @Valid UpdateTableDTO updateTableDTO) {
        tableService.updateTable(updateTableDTO);
        return Result.success(MessageConstant.UPDATE_SUCCESS);
    }
}
