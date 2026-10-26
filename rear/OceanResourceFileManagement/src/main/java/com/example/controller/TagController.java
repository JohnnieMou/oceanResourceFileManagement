package com.example.controller;


import com.example.annotation.Log;
import com.example.constant.MessageConstant;
import com.example.enums.BusinessTypeEnum;
import com.example.pojo.entity.Result;
import com.example.service.ITagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 
 * @since 2024-09-25
 */
@RestController
@RequestMapping("/tag")
@Api(tags = "标签相关接口")
@RequiredArgsConstructor
@Validated
public class TagController {

    private final ITagService tagService;

    @Log(title = "添加标签", businessType = BusinessTypeEnum.INSERT)
    @PostMapping("/create")
    @ApiOperation(value = "添加标签", notes = "添加不存在的标签")
    public Result<?> addTag(@RequestParam @NotBlank(message = "标签名不能为空") @ApiParam(example = "标签") String tagName) {
        tagService.addTag(tagName);
        return Result.success(MessageConstant.ADD_SUCCESS);
    }

    @Log(title = "删除标签", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/delete")
    @ApiOperation(value = "删除标签", notes = "删除存在的标签")
    public Result<?> deleteTag(@RequestParam @NotBlank(message = "标签名不能为空") @ApiParam(example = "标签") String tagName) {
        tagService.deleteTag(tagName);
        return Result.success(MessageConstant.DELETE_SUCCESS);
    }

    @Log(title = "修改标签", businessType = BusinessTypeEnum.UPDATE)
    @PutMapping("/update")
    @ApiOperation(value = "修改标签", notes = "修改存在的标签")
    public Result<?> updateTag(@RequestParam @NotBlank(message = "老标签名不能为空") @ApiParam(example = "标签") String oldTagName,
                            @RequestParam @NotBlank(message = "新标签名不能为空") @ApiParam(example = "新标签") String newTagName) {
        tagService.updateTag(oldTagName, newTagName);
        return Result.success(MessageConstant.UPDATE_SUCCESS);
    }

    @GetMapping("/list")
    @ApiOperation(value = "获取标签列表", notes = "获取所有标签的列表")
    public Result<?> listTag() {
        return Result.success(tagService.list());
    }

}
