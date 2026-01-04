package com.chris.fin_shark.common.aspect;

import com.chris.fin_shark.common.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 日誌切面
 *
 * 記錄 Controller 層的請求日誌
 *
 * @author chris
 * @since 2025-12-24
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    /**
     * 定義切入點: 所有 Controller 的 public 方法
     */
    @Pointcut("execution(public * com.chris.fin_shark.*.web..*Controller.*(..))")
    public void controllerPointcut() {
    }

    /**
     * 前置通知: 記錄請求資訊
     */
    @Before("controllerPointcut()")
    public void logBefore(JoinPoint joinPoint) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // 記錄請求資訊
            log.info("========== Request Start ==========");
            log.info("URL: {} {}", request.getMethod(), request.getRequestURL());
            log.info("IP: {}", getClientIp(request));
            log.info("Method: {}.{}",
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName());
            log.info("Args: {}", JsonUtil.toJson(joinPoint.getArgs()));
        }
    }

    /**
     * 返回通知: 記錄回應資訊
     */
    @AfterReturning(pointcut = "controllerPointcut()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("Response: {}", JsonUtil.toJson(result));
        log.info("========== Request End ==========");
    }

    /**
     * 異常通知: 記錄異常資訊
     */
    @AfterThrowing(pointcut = "controllerPointcut()", throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        log.error("Exception in {}.{}: {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                exception.getMessage(),
                exception);
        log.info("========== Request End (With Exception) ==========");
    }

    /**
     * 取得客戶端真實 IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
