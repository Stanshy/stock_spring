
package com.chris.fin_shark.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * 異常記錄切面
 *
 * 統一記錄系統異常
 *
 * @author chris
 * @since 2025-12-24
 */
@Aspect
@Component
@Slf4j
public class ExceptionAspect {

    /**
     * 定義切入點: 所有模組的 Service 和 Job
     */
    @Pointcut("execution(* com.chris.fin_shark.*.service..*(..)) || " +
            "execution(* com.chris.fin_shark.*.job..*(..))")
    public void serviceAndJobPointcut() {
    }

    /**
     * 異常通知: 記錄異常詳情
     */
    @AfterThrowing(pointcut = "serviceAndJobPointcut()", throwing = "exception")
    public void logException(JoinPoint joinPoint, Throwable exception) {
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        log.error("========== Exception Occurred ==========");
        log.error("Class: {}", className);
        log.error("Method: {}", methodName);
        log.error("Exception Type: {}", exception.getClass().getName());
        log.error("Exception Message: {}", exception.getMessage());
        log.error("Stack Trace: ", exception);
        log.error("=========================================");

        // TODO: 可以在此處整合告警系統
        // 例如: 發送 Email、Slack 通知、寫入監控系統等
        // if (isCriticalException(exception)) {
        //     alertService.sendAlert(className, methodName, exception);
        // }
    }

    /**
     * 判斷是否為關鍵異常（需要立即告警）
     *
     * TODO: 各模組開發時，可以根據業務需求定義關鍵異常
     */
    private boolean isCriticalException(Throwable exception) {
        // 範例: 資料庫連線失敗、外部 API 完全無法訪問等
        return exception instanceof java.sql.SQLException ||
                exception instanceof org.springframework.dao.DataAccessException;
    }
}
