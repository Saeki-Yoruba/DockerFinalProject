package com.supernovapos.finalproject.common.aspect;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Log4j2
public class LoggingAspect {

    // Controller 層
    @Pointcut("execution(* com.supernovapos.finalproject..controller..*(..))")
    public void controllerLayer() {}

    // Service 層
    @Pointcut("execution(* com.supernovapos.finalproject..service..*(..))")
    public void serviceLayer() {}

    // Repository 層
    @Pointcut("execution(* com.supernovapos.finalproject..repository..*(..))")
    public void repositoryLayer() {}

    // ======= 通用日誌 =======

    @Before("controllerLayer()")
    public void logBefore(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        log.info("Enter: {}.{}() args count={}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                args.length);
    }

    @AfterReturning(pointcut = "controllerLayer() || serviceLayer() || repositoryLayer()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("Exit: {}.{}() result={}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                result);
    }

    @Around("controllerLayer() || serviceLayer() || repositoryLayer()")
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
