package com.example.controller;

import com.example.BaseTest;
import com.example.config.properties.JwtProperties;
import com.example.constant.RoleConstant;
import com.example.enums.StatusCodeEnum;
import com.example.pojo.dto.user.UpdateUserDTO;
import com.example.pojo.dto.user.UserLoginDTO;
import com.example.pojo.dto.user.UserPageDTO;
import com.example.pojo.dto.user.UserRegisterDTO;
import com.example.pojo.entity.User;
import com.example.service.IUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Slf4j
@AutoConfigureMockMvc // 启用 MockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // 确保 @BeforeAll 和 @AfterAll 可以使用注入的对象
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // 确保测试方法按顺序执行
public class UserControllerTest extends BaseTest {

    @Autowired
    IUserService userService;
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    UserController userController;
    @Autowired
    JwtProperties jwtProperties;

    private Long test10Id;

    @AfterAll
    void clear() {
        // 删除用户
        try {
            Long id = userService.lambdaQuery().eq(User::getUsername, "test10").one().getId();
            userService.removeById(id);
        } finally {
            System.out.println("test10不存在");
        }
    }

    @Test
    @Order(1)
    public void testRegister() throws Exception {
        // 注册成功
        UserRegisterDTO userRegisterDTO = new UserRegisterDTO("test10", "test10");
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegisterDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("注册成功"));
        test10Id = userService.lambdaQuery().eq(User::getUsername, "test10").one().getId();

        // 缺用户名
        userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setPassword("test10");
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegisterDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("用户名不能为空"));

        // 缺密码
        userRegisterDTO = new UserRegisterDTO();
        userRegisterDTO.setUsername("test10");
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegisterDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("密码不能为空"));

        // 缺用户名和密码
        userRegisterDTO = new UserRegisterDTO();
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRegisterDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(-10003));
    }

    @Test
    @Order(2)
    public void testLogin() throws Exception {
        // 登录成功
        UserLoginDTO userLoginDTO = new UserLoginDTO("test10", "test10");
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLoginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.token").isNotEmpty());

        // 缺用户名
        userLoginDTO = new UserLoginDTO();
        userLoginDTO.setPassword("test10");
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLoginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("用户名不能为空"));

        // 缺密码
        userLoginDTO = new UserLoginDTO();
        userLoginDTO.setUsername("test10");
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLoginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("密码不能为空"));

        // 缺用户名和密码
        userLoginDTO = new UserLoginDTO();
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userLoginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(-10003));
    }

    @Test
    public void testUpdate() throws Exception {
        // 修改成功
        UpdateUserDTO updateUserDTO = new UpdateUserDTO();
        updateUserDTO.setId(1L);
        updateUserDTO.setRoleCode(RoleConstant.ADMIN);
        mockMvc.perform(put("/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(jwtProperties.getTokenName(), token)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("修改成功"));

        // 缺用户id
        updateUserDTO = new UpdateUserDTO();
        mockMvc.perform(put("/user/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(jwtProperties.getTokenName(), token)
                        .content(objectMapper.writeValueAsString(updateUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("用户id不能为空"));
    }

    @Test
    public void testUpdatePassword() throws Exception {
        // 修改成功
        mockMvc.perform(put("/user/updatePassword")
                        .param("oldPassword", "123456")
                        .param("newPassword", "123456")
                        .header(jwtProperties.getTokenName(), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("修改成功"));

        // 缺旧密码
        mockMvc.perform(put("/user/updatePassword")
                        .param("newPassword", "123456")
                        .header(jwtProperties.getTokenName(), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("旧密码不能为空"));

        // 缺新密码
        mockMvc.perform(put("/user/updatePassword")
                        .param("oldPassword", "123456")
                        .header(jwtProperties.getTokenName(), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("新密码不能为空"));

        // 缺旧密码和新密码
        mockMvc.perform(put("/user/updatePassword")
                        .header(jwtProperties.getTokenName(), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(-10003));
    }

    @Test
    public void testGet() throws Exception {
        // 获取成功
        UserPageDTO userPageDTO = UserPageDTO.builder()
                .current(1)
                .pageSize(10)
                .build();
        mockMvc.perform(get("/user/get")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(jwtProperties.getTokenName(), token)
                        .content(objectMapper.writeValueAsString(userPageDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records").isNotEmpty());

        // 无参数
        userPageDTO = new UserPageDTO();
        mockMvc.perform(get("/user/get")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(jwtProperties.getTokenName(), token)
                        .content(objectMapper.writeValueAsString(userPageDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records").isNotEmpty());
    }

    @Test
    public void testDelete() throws Exception {
        // 删除成功
        List<Long> ids = List.of(test10Id);
        String idsParam = ids.stream()
                             .map(String::valueOf)
                             .collect(Collectors.joining(","));
        mockMvc.perform(delete("/user/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(jwtProperties.getTokenName(), token)
                        .param("ids", idsParam))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("删除成功"));

        // id为空
        ids = List.of();
        mockMvc.perform(delete("/user/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(jwtProperties.getTokenName(), token)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("用户id不能为空"));
    }

    @Test
    public void testGetById() throws Exception {
        // 获取成功
        mockMvc.perform(get("/user/get/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(jwtProperties.getTokenName(), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isNotEmpty());

        // 用户不存在
        mockMvc.perform(get("/user/get/{id}", 100000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(jwtProperties.getTokenName(), token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(StatusCodeEnum.USER_NOT_FOUND.getDesc()));

    }

}