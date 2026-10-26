package com.example.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.annotation.Log;
import com.example.annotation.RequireRoles;
import com.example.constant.MessageConstant;
import com.example.constant.RoleConstant;
import com.example.enums.BusinessTypeEnum;
import com.example.enums.StatusCodeEnum;
import com.example.exception.BizException;
import com.example.pojo.dto.user.UpdateUserDTO;
import com.example.pojo.dto.user.UserLoginDTO;
import com.example.pojo.dto.user.UserPageDTO;
import com.example.pojo.dto.user.UserRegisterDTO;
import com.example.pojo.entity.Result;
import com.example.pojo.entity.User;
import com.example.pojo.vo.UserInfoVO;
import com.example.pojo.vo.UserLoginVO;
import com.example.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
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
@RequestMapping("/user")
@Api(tags = "用户相关接口")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final IUserService userService;

    @Log(title = "用户登录", businessType = BusinessTypeEnum.LOGIN)
    @PostMapping("/login")
    @ApiOperation(value = "用户登录", notes = "登录后会返回token，调用其他接口时需在请求头中携带该token，角色编号1为管理员，2为普通用户")
    public Result<UserLoginVO> login(@RequestBody @Valid UserLoginDTO userLoginDTO) {
        return Result.success(userService.login(userLoginDTO));
    }

    @Log(title = "用户注册", businessType = BusinessTypeEnum.REGISTER)
    @PostMapping("/register")
    @ApiOperation(value = "用户注册", notes = "用户名是唯一的，如果碰到重复的用户名，后端会提示用户名已存在，默认角色为普通用户")
    public Result<?> register(@RequestBody @Valid UserRegisterDTO userRegisterDTO) {
        userService.register(userRegisterDTO);
        return Result.success(MessageConstant.REGISTER_SUCCESS);
    }

    @Log(title = "删除用户", businessType = BusinessTypeEnum.DELETE)
    @DeleteMapping("/delete")
    @ApiOperation(value = "删除用户", notes = "删除用户，需要携带token，支持用户删除自己账号，也支持管理员删除用户账号")
    public Result<?> delete(@RequestParam(required = false) @NotEmpty(message = "用户id不能为空") List<Long> ids) {
        userService.deleteByIds(ids);
        return Result.success(MessageConstant.DELETE_SUCCESS);
    }

    @Log(title = "修改用户信息", businessType = BusinessTypeEnum.UPDATE)
    @PutMapping("/update")
    @ApiOperation(value = "修改用户信息", notes = "修改用户信息，需要携带token，支持用户修改自己信息，也支持管理员修改用户信息；能够修改角色，但管理员才能够修改；如果不是管理员，roleCode字段穿个空字符串或者不传就行")
    public Result<?> update(@RequestBody @Valid UpdateUserDTO updateUserDTO) {
        userService.updateUser(updateUserDTO);
        return Result.success(MessageConstant.UPDATE_SUCCESS);
    }

    @PutMapping("/updatePassword")
    @ApiOperation(value = "修改密码", notes = "修改密码，需要携带token，需要传入原密码进行校验，只能修改自己的密码")
    public Result<?> updatePassword(@RequestParam(required = false) @NotBlank(message = "旧密码不能为空") @Schema(example = "password") String oldPassword,
                                    @RequestParam(required = false) @NotBlank(message = "新密码不能为空") @Schema(example = "123456") String newPassword) {
        userService.updatePassword(oldPassword, newPassword);
        return Result.success(MessageConstant.UPDATE_SUCCESS);
    }

    @RequireRoles({ RoleConstant.ADMIN })
    @GetMapping("/get")
    @ApiOperation(value = "获取用户信息", notes = "获取用户信息，需要携带token，分页获取，且支持条件查询，无条件即为正常查询；需要管理员权限；页码默认为1，每页大小默认为10")
    public Result<IPage<UserInfoVO>> get(UserPageDTO userPageDTO) {
        return Result.success(userService.search(userPageDTO));
    }

    @GetMapping("/get/{id}")
    @ApiOperation(value = "根据id获取用户信息", notes = "根据id获取用户信息，需要携带token")
    public Result<UserInfoVO> getById(@PathVariable Long id) {
        return Result.success(userService.getByUserId(id));
    }
}
