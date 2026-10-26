package com.example.aspect;

import com.example.annotation.RequireRoles;
import com.example.constant.JwtClaimsConstant;
import com.example.constant.RoleConstant;
import com.example.context.BaseContext;
import com.example.enums.StatusCodeEnum;
import com.example.exception.BizException;
import io.jsonwebtoken.Claims;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RoleCheckAspect {
    
    @Before("@annotation(requireRoles)")
    public void checkRole(RequireRoles requireRoles) {
        Claims claims = BaseContext.getJwtClaims();

        // 获取当前用户角色编号
        Long roleCode = claims.get(JwtClaimsConstant.ROLE_CODE, Long.class);
        
        // 如果是管理员，直接放行
        if (roleCode == RoleConstant.ADMIN) {
            return;
        }
        // 如果是游客，则需要判断是否允许游客访问
        else if (roleCode == RoleConstant.GUEST) {
            if (requireRoles.allowGuest()) {
                return;
            } else {
                throw new BizException(StatusCodeEnum.NO_PERMISSION);
            }
        }
        
        // 检查当前用户角色是否在允许的角色列表中
        for (long role : requireRoles.value()) {
            if (roleCode == role) {
                return;
            }
        }
        
        throw new BizException(StatusCodeEnum.NO_PERMISSION);
    }
}