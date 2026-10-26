package com.example.context;

import com.example.constant.JwtClaimsConstant;
import com.example.pojo.entity.DataPermission;
import io.jsonwebtoken.Claims;

public class BaseContext {

    private static final ThreadLocal<Claims> JWT_CLAIMS = new ThreadLocal<>();
    private static final ThreadLocal<DataPermission> DATA_PERMISSION = new ThreadLocal<>();

    /**
     * JWT相关操作
     */
    public static void setJwtClaims(Claims claims) {
        JWT_CLAIMS.set(claims);
    }

    public static Claims getJwtClaims() {
        return JWT_CLAIMS.get();
    }

    public static void removeJwtClaims() {
        JWT_CLAIMS.remove();
    }

    public static Long getUserId() {
        return getJwtClaims().get(JwtClaimsConstant.USER_ID, Long.class);
    }

    /**
     * 数据权限相关操作
     */
    public static void setDataPermission(DataPermission permission) {
        DATA_PERMISSION.set(permission);
    }

    public static DataPermission getDataPermission() {
        return DATA_PERMISSION.get();
    }

    public static void removeDataPermission() {
        DATA_PERMISSION.remove();
    }

}
