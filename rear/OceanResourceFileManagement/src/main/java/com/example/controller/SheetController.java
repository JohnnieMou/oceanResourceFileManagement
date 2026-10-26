package com.example.controller;


import com.example.annotation.LimitAccess;
import com.example.annotation.Log;
import com.example.annotation.NameConvert;
import com.example.annotation.RequireRoles;
import com.example.constant.MessageConstant;
import com.example.constant.RoleConstant;
import com.example.enums.BusinessTypeEnum;
import com.example.enums.RedisKeyEnum;
import com.example.pojo.dto.PageDTO;
import com.example.pojo.dto.sheet.ExcelPageDTO;
import com.example.pojo.dto.sheet.FilterDTO;
import com.example.pojo.entity.PageResult;
import com.example.pojo.entity.Result;
import com.example.pojo.vo.ExcelColumnStatisticsVO;
import com.example.pojo.vo.FilterResultVO;
import com.example.service.ISheetService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
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
@RequestMapping("/sheet")
@Api(tags = "sheet相关接口")
@RequiredArgsConstructor
@Validated
public class SheetController {

    private final ISheetService sheetService;

    @GetMapping
    @ApiOperation(value = "获取sheet列表", notes = "获取指定表的所有sheet名的列表")
    @NameConvert(sheetToEnglish = false)
    public Result<List<String>> getSheets(@RequestParam @ApiParam(example = "示例表") String tableName) {
        return Result.success(sheetService.getSheets(tableName));
    }

    @GetMapping("/page")
    @ApiOperation(value = "分页查询", notes = "分页、动态渲染都行")
    @NameConvert(returnFieldToChinese = true)
    public Result<PageResult> page(@Valid ExcelPageDTO excelPageDTO) {
        return Result.success(sheetService.pageQuery(excelPageDTO));
    }

    @PostMapping("/statistices")
    @ApiOperation(value = "数值型列统计", notes = "返回数据类型为number的最大值、最小值、平均值、总和、个数")
    @NameConvert
    public Result<List<ExcelColumnStatisticsVO>> statistics(
            @RequestParam @ApiParam(example = "示例表") String tableName,
            @RequestParam @ApiParam(example = "Sheet1") String sheetName) {
        return Result.success(sheetService.statistics(tableName, sheetName));
    }

    @PostMapping("/filter")
    @ApiOperation(value = "条件查询", notes = "支持模糊查询、数值和日期的范围查询、数值的精确查询；返回内容还包括下载的key")
    @NameConvert(returnFieldToChinese = true)
    public Result<FilterResultVO> filter(@RequestBody @Valid FilterDTO filterDTO) {
        return Result.success(sheetService.filter(filterDTO));
    }

    @Log(title = "下载单个sheet", businessType = BusinessTypeEnum.DOWNLOAD)
    @PostMapping("/download")
    @ApiOperation(value = "下载单个sheet", notes = "下载指定sheet（支持追加或更新表内容后的下载）")
    public void download(@RequestParam @NotBlank(message = "表名不能为空") @ApiParam(example = "示例表") String tableName,
                         @RequestParam @NotBlank(message = "sheet名不能为空") @ApiParam(example = "Sheet1") String sheetName) {
        sheetService.download(tableName, sheetName);
    }

    @Log(title = "下载条件查询结果", businessType = BusinessTypeEnum.DOWNLOAD)
    @PostMapping("/downloadFilterResult")
    @ApiOperation(value = "下载条件查询结果", notes = "下载条件查询后的结果，结果将从Redis中获取并转换为Excel格式")
    @NameConvert(sheetToEnglish = false)
    public void downloadFilterResult(@RequestParam @NotBlank(message = "表名不能为空") @ApiParam(example = "示例表") String tableName,
                                     @RequestParam @NotBlank(message = "sheet名不能为空") @ApiParam(example = "Sheet1") String sheetName,
                                     @RequestParam @NotBlank(message = "查询结果键不能为空") @ApiParam(example = "abc123") String key) {
        sheetService.downloadFilterResult(tableName, sheetName, key);
    }

    @Log(title = "获取单个sheet的模板", businessType = BusinessTypeEnum.DOWNLOAD)
    @PostMapping("/getTemplate")
    @ApiOperation(value = "获取单个sheet的模板", notes = "下载指定sheet（支持追加或更新表内容后的下载）")
    public void getTemplate(@RequestParam @NotBlank(message = "表名不能为空") @ApiParam(example = "示例表") String tableName,
                            @RequestParam @NotBlank(message = "sheet名不能为空") @ApiParam(example = "Sheet1") String sheetName) {
        sheetService.getTemplate(tableName, sheetName);
    }

    @LimitAccess(key = RedisKeyEnum.UPLOAD_EXCEL, times = 10)
    @Log(title = "上传sheet（确认表字段类型）", businessType = BusinessTypeEnum.CONFIRM)
    @PostMapping("/uploadConfirm/{tableName}")
    @ApiOperation(value = "上传sheet（确认表字段类型）", notes = "上传sheet并返回字段名和类型，来修改并确认类型进行上传；输入的表名是被追加表的表名")
    public Result<Map<String, LinkedHashMap<String, String>>> add(@RequestParam MultipartFile excel,
                                                                  @PathVariable @ApiParam(example = "示例表") String tableName) {
        return Result.success(sheetService.uploadConfirm(excel, tableName));
    }

    @Log(title = "上传sheet（追加）", businessType = BusinessTypeEnum.APPEND)
    @PostMapping("/uploadAppendSheet/{tableName}")
    @ApiOperation(value = "上传sheet（追加）", notes = "在原有表基础上追加sheet，sheet名不能与原有的重复")
    public Result<?> upload(@RequestBody Map<String, LinkedHashMap<String, String>> fieldListMap,
                            @PathVariable @ApiParam(example = "示例表") String tableName) {
        sheetService.upload(fieldListMap, tableName);
        return Result.success(MessageConstant.UPLOAD_SUCCESS);
    }

    @RequireRoles({RoleConstant.ADMIN})
    @Log(title = "删除sheet", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/delete/{tableName}/{sheetName}")
    @ApiOperation(value = "删除sheet", notes = "删除指定表的指定sheet")
    @NameConvert
    public Result<?> delete(@PathVariable @ApiParam(example = "示例表") String tableName,
                            @PathVariable @ApiParam(example = "Sheet1") String sheetName) {
        sheetService.delete(tableName, sheetName);
        return Result.success(MessageConstant.DELETE_SUCCESS);
    }

    @Log(title = "上传附件", businessType = BusinessTypeEnum.UPLOAD)
    @PostMapping("/uploadAttachment")
    @ApiOperation(value = "上传附件", notes = "给指定地点的航次上传附件；支持同样地点航次上传多个文件（是分开一个一个传）")
    public Result<?> uploadAttachment(@RequestParam MultipartFile file,
                                      @RequestParam @NotBlank(message = "地点不能为空") @ApiParam(example = "广东") String location,
                                      @RequestParam @NotNull(message = "航次不能为空") @ApiParam(example = "202503") Integer voyage) {
        sheetService.uploadAttachment(file, location, voyage);
        return Result.success(MessageConstant.UPLOAD_SUCCESS);
    }

    @Log(title = "下载附件", businessType = BusinessTypeEnum.DOWNLOAD)
    @GetMapping("/downloadAttachment/{id}")
    @ApiOperation(value = "下载附件", notes = "根据附件id下载附件")
    public void downloadAttachment(@PathVariable @NotNull(message = "附件id不能为空") @ApiParam(example = "1") Long id) {
        sheetService.downloadAttachment(id);
    }

    @Log(title = "删除附件", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/deleteAttachment")
    @ApiOperation(value = "删除附件", notes = "根据附件id删除附件；支持批量删除")
    public Result<?> deleteAttachment(@RequestParam @NotEmpty(message = "附件id不能为空") @ApiParam(example = "1, 2") List<Integer> ids) {
        sheetService.deleteAttachment(ids);
        return Result.success(MessageConstant.DELETE_SUCCESS);
    }

    @GetMapping("/attachment/page")
    @ApiOperation(value = "分页查询附件列表", notes = "分页查询附件列表，其中附件以链接形式返回")
    public Result<PageResult> attachmentPage(@Valid PageDTO pageDTO) {
        return Result.success(sheetService.attachmentPage(pageDTO));
    }

}
