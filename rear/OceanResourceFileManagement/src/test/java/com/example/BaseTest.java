package com.example;

import com.example.config.properties.JwtProperties;
import com.example.context.BaseContext;
import com.example.pojo.dto.user.UserLoginDTO;
import com.example.pojo.vo.UserLoginVO;
import com.example.service.IUserService;
import com.example.utils.JwtUtil;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public abstract class BaseTest {

    @Autowired
    IUserService userService;
    @Autowired
    JwtProperties jwtProperties;

    protected String token;

    @BeforeAll
    void login(@Autowired IUserService userService) {
        if (token == null) {
            UserLoginVO user = userService.login(UserLoginDTO.builder()
                    .username("admin")
                    .password("123456")
                    .build());

            token = user.getToken();

            BaseContext.setJwtClaims(JwtUtil.parseJWT(jwtProperties.getSecretKey(), token));
        }
    }
}