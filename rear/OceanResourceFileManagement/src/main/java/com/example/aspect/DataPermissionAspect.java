package com.example.aspect;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.annotation.DataPermission;
import com.example.constant.JwtClaimsConstant;
import com.example.context.BaseContext;
import com.example.enums.StatusCodeEnum;
import com.example.exception.BizException;
import com.example.mapper.DataPermissionMapper;
import com.example.mapper.TableMapper;
import com.example.pojo.entity.Table;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DataPermissionAspect {

    private final DataPermissionMapper dataPermissionMapper;
    private final TableMapper tableMapper;
    private ExpressionParser parser = new SpelExpressionParser();
    private DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Before("@annotation(dataPermission)")
    public void checkPermission(JoinPoint joinPoint, DataPermission dataPermission) {
        // 获取方法参数值和参数名
        Object[] args = joinPoint.getArgs();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = nameDiscoverer.getParameterNames(methodSignature.getMethod());

        // 构造SpEL上下文
        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < paramNames.length; i++) {
            context.setVariable(paramNames[i], args[i]);
        }

        // 解析SpEL表达式获取表名
        String cnNameSpEl = dataPermission.tableCnName();
        String tableCnName;
        if (StringUtils.isBlank(cnNameSpEl)) {
            String enNameSpEl = dataPermission.tableEnName();
            if (StringUtils.isBlank(enNameSpEl)) {
                return;
            }
            String tableEnName = parser.parseExpression(enNameSpEl).getValue(context, String.class);
            tableCnName = tableMapper.selectOne(new LambdaQueryWrapper<Table>().eq(Table::getTableEnName, tableEnName)).getTableCnName();
        }
        else {
            tableCnName = parser.parseExpression(cnNameSpEl).getValue(context, String.class);
        }

        // 获取当前用户ID和表中文名
        Long userId = BaseContext.getUserId();

        // 获取用户对该表的权限
        com.example.pojo.entity.DataPermission permission = dataPermissionMapper.selectOne(
                new LambdaQueryWrapper<com.example.pojo.entity.DataPermission>()
                        .eq(com.example.pojo.entity.DataPermission::getUserId, userId)
                        .eq(com.example.pojo.entity.DataPermission::getTableCnName, tableCnName)
        );

//        if (permission == null) {
//            throw new BizException(StatusCodeEnum.NO_PERMISSION);
//        }

        // 将权限信息存储到ThreadLocal中，供后续SQL执行时使用
        BaseContext.setDataPermission(permission);
    }
}