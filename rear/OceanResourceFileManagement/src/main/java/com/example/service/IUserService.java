package com.example.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.pojo.dto.user.UpdateUserDTO;
import com.example.pojo.dto.user.UserLoginDTO;
import com.example.pojo.dto.user.UserPageDTO;
import com.example.pojo.dto.user.UserRegisterDTO;
import com.example.pojo.entity.User;
import com.example.pojo.vo.UserInfoVO;
import com.example.pojo.vo.UserLoginVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author ccc212
 * @since 2024-09-06
 */
public interface IUserService extends IService<User> {
    UserLoginVO login(UserLoginDTO userLoginDTO);

    Long register(UserRegisterDTO userRegisterDTO);

    IPage<UserInfoVO> search(UserPageDTO userPageDTO);

    void updateUser(UpdateUserDTO updateUserDTO);

    void updatePassword(String oldPassword, String newPassword);

    void deleteByIds(List<Long> ids);

    UserInfoVO getByUserId(Long id);
}
