package com.example.aspect;

import com.example.annotation.LimitAccess;
import com.example.constant.JwtClaimsConstant;
import com.example.context.BaseContext;
import com.example.enums.RedisKeyEnum;
import com.example.enums.StatusCodeEnum;
import com.example.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * AOP类（通知类）
 */
@Component
@Aspect
@RequiredArgsConstructor
public class LimitAccessAspect {

    private final RedisTemplate redisTemplate;

    @Pointcut("@annotation(com.example.annotation.LimitAccess)")
    public void pt(){};

    @Around("pt()")
    public Object aopAround(ProceedingJoinPoint pjp) throws Throwable {
        // 获取注解
        Signature signature = pjp.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        LimitAccess limitAccess = methodSignature.getMethod().getAnnotation(LimitAccess.class);

        // 获取注解上面的属性
        int limit = limitAccess.times();
        RedisKeyEnum redisKeyEnum = limitAccess.key();
        String key = redisKeyEnum.getKey() + ":" + BaseContext.getJwtClaims().get(JwtClaimsConstant.USER_ID);

        // 根据key去找Redis中的值
        Object o = redisTemplate.opsForValue().get(key);

        // 如果不存在，说明是首次访问，存入Redis，过期时间为limitAccess中的time
        if (o == null) {
            redisTemplate.opsForValue().set(key, "", limit, TimeUnit.SECONDS);
            // 执行切入点的方法
            return pjp.proceed();
        } else {
            // 如果存在，说明不是首次访问，抛出异常
            throw new BizException(StatusCodeEnum.TOO_MANY_REQUESTS);
        }
    }
}
