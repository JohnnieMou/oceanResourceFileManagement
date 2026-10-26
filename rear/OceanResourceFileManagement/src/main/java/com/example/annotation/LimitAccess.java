package com.example.annotation;

import com.example.enums.RedisKeyEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LimitAccess {

    /**
     * 限制访问的key
     * @return
     */
    RedisKeyEnum key();

    /**
     * 限制访问时间
     * @return
     */
    int times();
}
