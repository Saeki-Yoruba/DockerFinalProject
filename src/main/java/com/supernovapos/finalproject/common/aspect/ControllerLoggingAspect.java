package com.supernovapos.finalproject.common.aspect;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;

import java.util.Arrays;

@Aspect
@Component
@Log4j2
public class ControllerLoggingAspect {

    // 攔截所有 Controller 層的方法
    @Pointcut("execution(* com.supernovapos.finalproject..controller..*(..))")
    public void controllerMethods() {}

    // 執行前
    @Before("controllerMethods()")
    public void logBefore(JoinPoint joinPoint) {
        log.info("Enter Controller: {}.{}() args={}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs()));
    }

    // 執行後成功返回
    @AfterReturning(pointcut = "controllerMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("Exit Controller: {}.{}() result={}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                result);
    }

    // 紀錄執行時間
    @Around("controllerMethods()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object proceed = joinPoint.proceed();
        long elapsedTime = System.currentTimeMillis() - start;

        log.info("Execution Time: {}.{}() = {} ms",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                elapsedTime);

        return proceed;
    }
}
