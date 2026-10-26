package com.example.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.config.properties.JwtProperties;
import com.example.constant.JwtClaimsConstant;
import com.example.constant.RoleConstant;
import com.example.context.BaseContext;
import com.example.enums.StatusCodeEnum;
import com.example.exception.BizException;
import com.example.mapper.RoleMapper;
import com.example.mapper.UserMapper;
import com.example.pojo.dto.user.UpdateUserDTO;
import com.example.pojo.dto.user.UserLoginDTO;
import com.example.pojo.dto.user.UserPageDTO;
import com.example.pojo.dto.user.UserRegisterDTO;
import com.example.pojo.vo.UserInfoVO;
import com.example.pojo.vo.UserLoginVO;
import com.example.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 确保 @BeforeAll 和 @AfterAll 可以使用注入的对象
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // 确保测试方法按顺序执行
public class UserServiceImplTest {

    @Autowired
    UserMapper userMapper;
    @Autowired
    RoleMapper roleMapper;
    @Autowired
    JwtProperties jwtProperties;
    @Autowired
    UserServiceImpl userServiceImpl;

    private Long exampleId;

    @BeforeAll
    void init() {
        // 注册
        userServiceImpl.register(UserRegisterDTO.builder()
                .username("example")
                .password("example")
                .build());

        // 登录
        UserLoginVO user = userServiceImpl.login(UserLoginDTO.builder()
                .username("example")
                .password("example")
                .build());

        exampleId = user.getId();

        // 生成claims
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        claims.put(JwtClaimsConstant.NAME, user.getName());
        claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
        claims.put(JwtClaimsConstant.ROLE_CODE, user.getRoleCode());
        claims.put(JwtClaimsConstant.VALID_START_TIME, user.getValidStartTime().toString());
        claims.put(JwtClaimsConstant.VALID_END_TIME, user.getValidEndTime().toString());

        // 设置claims
        BaseContext.setJwtClaims(
                JwtUtil.parseJWT(
                        jwtProperties.getSecretKey(), JwtUtil.createJWT(
                                jwtProperties.getSecretKey(),
                                jwtProperties.getTtl(),
                                claims)
                )
        );
    }

    @AfterAll
    void clear() {
        // 删除用户
        try {
            userServiceImpl.removeById(exampleId);
        } finally {
            System.out.println("example不存在");
        }
    }

    @Test
    @Order(1)
    public void testRegister() throws Exception {
        // 用户名重复
        BizException exception = Assertions.assertThrows(BizException.class, () ->
                userServiceImpl.register(UserRegisterDTO.builder()
                        .username("example")
                        .password("example")
                        .build()));
        Assertions.assertEquals(StatusCodeEnum.USERNAME_EXIST.getDesc(), exception.getMessage());
    }

    @Test
    @Order(2)
    public void testLogin() throws Exception {
        // 用户名不存在
        BizException exception = Assertions.assertThrows(BizException.class, () ->
                userServiceImpl.login(UserLoginDTO.builder()
                        .username("notExist")
                        .password("notExist")
                        .build()));
        Assertions.assertEquals(StatusCodeEnum.USER_NOT_FOUND.getDesc(), exception.getMessage());

        // 密码错误
        exception = Assertions.assertThrows(BizException.class, () ->
                userServiceImpl.login(UserLoginDTO.builder()
                        .username("example")
                        .password("errorPwd")
                        .build()));
        Assertions.assertEquals(StatusCodeEnum.PASSWORD_ERROR.getDesc(), exception.getMessage());

        // 账号过期
        exception = Assertions.assertThrows(BizException.class, () ->
                userServiceImpl.login(UserLoginDTO.builder()
                        .username("expired")
                        .password("123456")
                        .build()));
        Assertions.assertEquals(StatusCodeEnum.USER_EXPIRED.getDesc(), exception.getMessage());
    }

    @Test
    public void testSearch() throws Exception {
        // 分页查询
        IPage<UserInfoVO> result = userServiceImpl.search(UserPageDTO.builder()
                .current(1)
                .pageSize(10)
                .build());
        Assertions.assertNotEquals(null, result);

        // 条件查询
        result = userServiceImpl.search(UserPageDTO.builder()
                .current(1)
                .pageSize(10)
                .id(1L)
                .build());
        Assertions.assertNotEquals(null, result);

        result = userServiceImpl.search(UserPageDTO.builder()
                .current(1)
                .pageSize(10)
                .name("管理员")
                .build());
        Assertions.assertNotEquals(null, result);

        result = userServiceImpl.search(UserPageDTO.builder()
                .current(1)
                .pageSize(10)
                .username("admin")
                .build());
        Assertions.assertNotEquals(null, result);

        result = userServiceImpl.search(UserPageDTO.builder()
                .current(1)
                .pageSize(10)
                .id(1L)
                .name("管理员")
                .username("admin")
                .build());
        Assertions.assertNotEquals(null, result);
    }

    @Test
    public void testUpdateUser() throws Exception {
        // 修改成功
        Assertions.assertDoesNotThrow(() ->
                userServiceImpl.updateUser(UpdateUserDTO.builder()
                        .id(exampleId)
                        .name("test")
                        .build())
        );

        // 用户不存在
        BizException exception = Assertions.assertThrows(BizException.class, () ->
                userServiceImpl.updateUser(UpdateUserDTO.builder()
                        .id(100000L)
                        .name("test")
                        .username("test")
                        .roleCode(RoleConstant.ADMIN)
                        .build())
        );
        Assertions.assertEquals(StatusCodeEnum.USER_NOT_FOUND.getDesc(), exception.getMessage());

        // 修改用户角色需管理员权限
        exception = Assertions.assertThrows(BizException.class, () ->
                userServiceImpl.updateUser(UpdateUserDTO.builder()
                        .id(exampleId)
                        .roleCode(1L)
                        .build())
        );
        Assertions.assertEquals("修改用户角色需管理员权限", exception.getMessage());
    }

    @Test
    public void testUpdatePassword() throws Exception {
        // 密码错误
        BizException exception = Assertions.assertThrows(BizException.class, () ->
                userServiceImpl.updatePassword("oldPassword", "newPassword"));
        Assertions.assertEquals(StatusCodeEnum.PASSWORD_ERROR.getDesc(), exception.getMessage());

        // 修改成功
        Assertions.assertDoesNotThrow(() ->
                userServiceImpl.updatePassword("example", "123456"));
    }

    @Test
    public void testDeleteByIds() throws Exception {
        // 用户不存在
        BizException exception = Assertions.assertThrows(BizException.class, () ->
                userServiceImpl.deleteByIds(List.of(100000L)));
        Assertions.assertEquals(StatusCodeEnum.USER_NOT_FOUND.getDesc(), exception.getMessage());

        // 非管理员删除其他用户
        exception = Assertions.assertThrows(BizException.class, () ->
                userServiceImpl.deleteByIds(List.of(2L)));
        Assertions.assertEquals("删除用户需管理员权限", exception.getMessage());

        // 删除成功
        Assertions.assertDoesNotThrow(() ->
                userServiceImpl.deleteByIds(List.of(exampleId)));
    }

    @Test
    public void testGetById() {
        // 查询成功
        Assertions.assertDoesNotThrow(() ->
                userServiceImpl.getByUserId(1L));

        // 用户不存在
        BizException exception = Assertions.assertThrows(BizException.class, () ->
                userServiceImpl.getByUserId(100000L));
        Assertions.assertEquals(StatusCodeEnum.USER_NOT_FOUND.getDesc(), exception.getMessage());
    }


}