package com.chris.fin_shark.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 效能監控切面
 *
 * 記錄方法執行時間
 *
 * @author chris
 * @since 2025-12-24
 */
@Aspect
@Component
@Slf4j
public class PerformanceAspect {

    /**
     * 效能監控閾值（毫秒）
     * 超過此值會記錄警告日誌
     */
    private static final long SLOW_THRESHOLD = 1000;  // 1 秒

    /**
     * 定義切入點: Service 層的所有方法
     */
    @Pointcut("execution(* com.chris.fin_shark.*.service..*Service.*(..))")
    public void servicePointcut() {
    }

    /**
     * 環繞通知: 記錄方法執行時間
     */
    @Around("servicePointcut()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        try {
            // 執行目標方法
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;

            // 如果執行時間超過閾值，記錄警告
            if (executionTime > SLOW_THRESHOLD) {
                log.warn("Slow execution: {}.{} took {} ms",
                        className, methodName, executionTime);
            } else {
                log.debug("Execution time: {}.{} took {} ms",
                        className, methodName, executionTime);
            }

            return result;

        } catch (Throwable throwable) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Exception in {}.{} after {} ms",
                    className, methodName, executionTime, throwable);
            throw throwable;
        }
    }

    // TODO: 各模組開發時，可以針對特定操作設定不同的閾值
    // 範例:
    // @Around("execution(* com.chris.fin_shark.m06.service..*DataSyncService.*(..))")
    // public Object logDataSyncTime(ProceedingJoinPoint joinPoint) {
    //     // 資料同步允許較長的執行時間（例如 5 秒）
    // }
}
