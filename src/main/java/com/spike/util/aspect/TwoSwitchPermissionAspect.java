package com.spike.util.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @className: PermissionAspect
 * @description: 权限控制切面
 * @author: spike.william
 * @date: 2022/11/23 14:56
 **/

@Slf4j
@Aspect
@Component
public class TwoSwitchPermissionAspect  {

    @Resource
    private JdbcTemplate jdbcTemplate;

    //List方法切点
    @Pointcut("execution(* com.authine.cloudpivot.ext.modules.twoSwitchSystem.controller.twoSwitchSystemController.*(..))")
    void powerCheck() {
    }

    @Around(value = "powerCheck()")
    public Object powerCheck(ProceedingJoinPoint jp) throws Throwable {
        List<Map<String, Object>> result = queryPower(null);
        String methodName =jp.getSignature().getName();
        //执行原方法
        Object obj = jp.proceed(jp.getArgs());
        return obj;
    }

    private List<Map<String, Object>> queryPower(String userId){
        //Get tableName
        return null;
    }

}
