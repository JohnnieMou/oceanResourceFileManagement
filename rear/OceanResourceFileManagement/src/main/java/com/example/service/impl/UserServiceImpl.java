package com.example.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import com.example.pojo.entity.Role;
import com.example.pojo.entity.User;
import com.example.pojo.vo.UserInfoVO;
import com.example.pojo.vo.UserLoginVO;
import com.example.service.IUserService;
import com.example.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author
 * @since 2024-09-06
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public UserLoginVO login(UserLoginDTO userLoginDTO) {

        String username = userLoginDTO.getUsername();
        String password = userLoginDTO.getPassword();

        // 获取用户
        User user;
        try {
            user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        } catch (Exception e) {
            throw new BizException(StatusCodeEnum.LOGIN_FAILED);
        }

        // 判断用户是否存在
        if (user == null) {
            throw new BizException(StatusCodeEnum.USER_NOT_FOUND);
        }

        // 判断密码是否正确
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(user.getPassword())) {
            throw new BizException(StatusCodeEnum.PASSWORD_ERROR);
        }

        // 判断有效期是否过期
        if (user.getValidStartTime() != null && user.getValidEndTime() != null) {
            if (LocalDate.now().isBefore(user.getValidStartTime())
                    || LocalDate.now().equals(user.getValidEndTime())
                    || LocalDate.now().isAfter(user.getValidEndTime())) {
                throw new BizException(StatusCodeEnum.USER_EXPIRED);
            }
        }

        // 更新登录时间
        userMapper.update(new LambdaUpdateWrapper<User>()
                .eq(User::getUsername, username)
                .set(User::getLastLoginTime, LocalDateTime.now()));

        // 生成token
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, user.getId());
        claims.put(JwtClaimsConstant.NAME, user.getName());
        claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
        claims.put(JwtClaimsConstant.ROLE_CODE, user.getRoleCode());
        claims.put(JwtClaimsConstant.VALID_START_TIME, user.getValidStartTime().toString());
        claims.put(JwtClaimsConstant.VALID_END_TIME, user.getValidEndTime().toString());
        String token = JwtUtil.createJWT(
                jwtProperties.getSecretKey(),
                jwtProperties.getTtl(),
                claims
        );

        return UserLoginVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .roleCode(user.getRoleCode())
                .validStartTime(user.getValidStartTime())
                .validEndTime(user.getValidEndTime())
                .token(token)
                .lastLoginTime(user.getLastLoginTime())
                .build();
    }

    @Override
    @Transactional
    public Long register(UserRegisterDTO userRegisterDTO) {
        String username = userRegisterDTO.getUsername();
        User existUser = lambdaQuery().eq(User::getUsername, username).one();

        if (existUser != null) {
            throw new BizException(StatusCodeEnum.USERNAME_EXIST);
        }

        // 默认名字为用户名，角色为普通用户
        User user = User.builder()
                .name(username)
                .username(username)
                .password(DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes()))
                .roleCode(RoleConstant.GUEST)
                .createTime(LocalDateTime.now())
                .lastLoginTime(LocalDateTime.now())
                .build();

        save(user);

        return user.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public IPage<UserInfoVO> search(UserPageDTO userPageDTO) {
        Page<User> page = new Page<>(userPageDTO.getCurrent(), userPageDTO.getPageSize());
        return userMapper.search(page, userPageDTO);
    }

    @Override
    @Transactional
    public void updateUser(UpdateUserDTO updateUserDTO) {
        Long targetUserId = updateUserDTO.getId();
        if (lambdaQuery().eq(User::getId, targetUserId).one() == null) {
            throw new BizException(StatusCodeEnum.USER_NOT_FOUND);
        }

        if (updateUserDTO.getRoleCode() != null) {
            if (BaseContext.getJwtClaims().get(JwtClaimsConstant.ROLE_CODE, Long.class) != RoleConstant.ADMIN) {
                throw new BizException("修改用户角色需管理员权限");
            }

            if (roleMapper.selectOne(new LambdaQueryWrapper<Role>().eq(Role::getId, updateUserDTO.getRoleCode())) == null) {
                throw new BizException(StatusCodeEnum.ROLE_NOT_EXISTS);
            }
        }

        User user = BeanUtil.copyProperties(updateUserDTO, User.class);
        updateById(user);
    }

    @Override
    @Transactional
    public void updatePassword(String oldPassword, String newPassword) {
        Long id = BaseContext.getUserId();
        User user = getById(id);
        if (!user.getPassword().equals(DigestUtils.md5DigestAsHex(oldPassword.getBytes()))) {
            throw new BizException(StatusCodeEnum.PASSWORD_ERROR);
        }
        update(new LambdaUpdateWrapper<User>()
                .eq(User::getId, id)
                .set(User::getPassword, DigestUtils.md5DigestAsHex(newPassword.getBytes())));
    }

    @Override
    public void deleteByIds(List<Long> ids) {
        Claims claims = BaseContext.getJwtClaims();
        Long currentId = claims.get(JwtClaimsConstant.USER_ID, Long.class);
        Integer roleCode = claims.get(JwtClaimsConstant.ROLE_CODE, Integer.class);

        for (Long id : ids) {
            // 判断用户是否存在
            if (lambdaQuery().eq(User::getId, id).one() == null) {
                throw new BizException(StatusCodeEnum.USER_NOT_FOUND);
            }

            // 非管理员不能删除其他用户
            if (!id.equals(currentId) && roleCode != RoleConstant.ADMIN) {
                throw new BizException("删除用户需管理员权限");
            }

            // 将用户有效期截止时间设为当前时间
            userMapper.update(new LambdaUpdateWrapper<User>()
                    .eq(User::getId, id)
                    .set(User::getValidEndTime, LocalDate.now()));
        }
    }

    @Override
    public UserInfoVO getByUserId(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BizException(StatusCodeEnum.USER_NOT_FOUND);
        }
        return BeanUtil.copyProperties(user, UserInfoVO.class);
    }
}
