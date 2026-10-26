package com.example.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.pojo.dto.user.UserPageDTO;
import com.example.pojo.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pojo.vo.UserInfoVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author
 * @since 2024-09-06
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    IPage<UserInfoVO> search(Page<User> page, @Param("userPageDTO") UserPageDTO userPageDTO);

}
