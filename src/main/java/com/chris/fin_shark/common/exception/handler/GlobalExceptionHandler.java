package com.chris.fin_shark.common.exception.handler;

import com.chris.fin_shark.common.dto.ErrorDetail;
import com.chris.fin_shark.common.dto.ErrorResponse;
import com.chris.fin_shark.common.enums.ErrorCode;
import com.chris.fin_shark.common.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 全局異常處理器
 *
 * 統一處理所有異常，轉換為標準的 ErrorResponse 格式
 *
 * 遵守總綱 4.4.2 API Response 統一格式規範
 *
 * @author chris
 * @since 2025-12-24
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ========================================================================
    // 自訂異常處理
    // ========================================================================

    /**
     * 處理 BaseException 及其子類異常
     *
     * 所有自訂異常最終都會被這個方法捕獲
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(
            BaseException ex, HttpServletRequest request) {

        log.error("Business exception occurred: {}", ex.getMessage(), ex);

        ErrorDetail errorDetail = ErrorDetail.builder()
                .errorCode(ex.getErrorCode().getCode())
                .errorType(determineErrorType(ex.getHttpStatus()))
                .details(ex.getDetails())
                .field(ex.getField())
                .suggestion(ex.getSuggestion())
                .build();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(ex.getHttpStatus())
                .message(ex.getMessage())
                .error(errorDetail)
                .traceId(MDC.get("traceId"))
                .build();

        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }

    /**
     * 處理資料不存在異常
     *
     * HTTP 404
     */
    @ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDataNotFoundException(
            DataNotFoundException ex, HttpServletRequest request) {

        log.warn("Data not found: {}", ex.getMessage());

        ErrorDetail errorDetail = ErrorDetail.builder()
                .errorCode(ex.getErrorCode().getCode())
                .errorType("CLIENT_ERROR")
                .details(ex.getDetails())
                .field(ex.getField())
                .suggestion(ex.getSuggestion())
                .build();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(404)
                .message(ex.getMessage())
                .error(errorDetail)
                .traceId(MDC.get("traceId"))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * 處理資料驗證異常
     *
     * HTTP 422
     */
    @ExceptionHandler(DataValidationException.class)
    public ResponseEntity<ErrorResponse> handleDataValidationException(
            DataValidationException ex, HttpServletRequest request) {

        log.warn("Data validation failed: {}", ex.getMessage());

        ErrorDetail errorDetail = ErrorDetail.builder()
                .errorCode(ex.getErrorCode().getCode())
                .errorType("VALIDATION_ERROR")
                .details(ex.getDetails())
                .field(ex.getField())
                .suggestion(ex.getSuggestion())
                .build();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(422)
                .message(ex.getMessage())
                .error(errorDetail)
                .traceId(MDC.get("traceId"))
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }

    /**
     * 處理外部 API 異常
     *
     * HTTP 503
     */
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ErrorResponse> handleExternalApiException(
            ExternalApiException ex, HttpServletRequest request) {

        log.error("External API call failed: {} - {}", ex.getApiName(), ex.getMessage(), ex);

        String details = String.format("API: %s, URL: %s, Status: %s",
                ex.getApiName(),
                ex.getApiUrl(),
                ex.getExternalHttpStatus());

        ErrorDetail errorDetail = ErrorDetail.builder()
                .errorCode(ex.getErrorCode().getCode())
                .errorType("SERVER_ERROR")
                .details(details)
                .suggestion("Please try again later or contact support")
                .build();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(503)
                .message(ex.getMessage())
                .error(errorDetail)
                .traceId(MDC.get("traceId"))
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    // ========================================================================
    // Spring 框架異常處理
    // ========================================================================

    /**
     * 處理參數驗證異常 (@Valid)
     *
     * HTTP 400
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        log.warn("Validation failed: {}", ex.getMessage());

        // 取得第一個驗證錯誤
        FieldError fieldError = ex.getBindingResult().getFieldError();

        String field = fieldError != null ? fieldError.getField() : "unknown";
        String message = fieldError != null ? fieldError.getDefaultMessage() : "Validation failed";

        ErrorDetail errorDetail = ErrorDetail.builder()
                .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                .errorType("VALIDATION_ERROR")
                .details(message)
                .field(field)
                .suggestion("Please provide valid " + field)
                .build();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(400)
                .message("Invalid request parameters")
                .error(errorDetail)
                .traceId(MDC.get("traceId"))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 處理參數類型不匹配異常
     *
     * HTTP 400
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        log.warn("Parameter type mismatch: {}", ex.getMessage());

        String details = String.format("Parameter '%s' should be of type %s",
                ex.getName(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        ErrorDetail errorDetail = ErrorDetail.builder()
                .errorCode(ErrorCode.INVALID_REQUEST.getCode())
                .errorType("CLIENT_ERROR")
                .details(details)
                .field(ex.getName())
                .suggestion("Please provide correct parameter type")
                .build();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(400)
                .message("Invalid parameter type")
                .error(errorDetail)
                .traceId(MDC.get("traceId"))
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * 處理 404 Not Found 異常
     *
     * HTTP 404
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {

        log.warn("No handler found for: {} {}", ex.getHttpMethod(), ex.getRequestURL());

        ErrorDetail errorDetail = ErrorDetail.builder()
                .errorCode(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                .errorType("CLIENT_ERROR")
                .details("No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL())
                .suggestion("Please check the API endpoint")
                .build();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(404)
                .message("Endpoint not found")
                .error(errorDetail)
                .traceId(MDC.get("traceId"))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    // ========================================================================
    // 通用異常處理（兜底）
    // ========================================================================

    /**
     * 處理所有未被捕獲的異常
     *
     * HTTP 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error occurred", ex);

        ErrorDetail errorDetail = ErrorDetail.builder()
                .errorCode(ErrorCode.INTERNAL_ERROR.getCode())
                .errorType("SERVER_ERROR")
                .details(ex.getMessage())
                .suggestion("Please contact support if this error persists")
                .build();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(500)
                .message("Internal server error")
                .error(errorDetail)
                .traceId(MDC.get("traceId"))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    // ========================================================================
    // 輔助方法
    // ========================================================================

    /**
     * 根據 HTTP 狀態碼判斷錯誤類型
     */
    private String determineErrorType(Integer httpStatus) {
        if (httpStatus >= 400 && httpStatus < 500) {
            return "CLIENT_ERROR";
        } else if (httpStatus >= 500) {
            return "SERVER_ERROR";
        }
        return "UNKNOWN_ERROR";
    }

    // TODO: 各模組開發時，可以在此補充特定異常的處理方法
    // 範例:
    // @ExceptionHandler(SpecificModuleException.class)
    // public ResponseEntity<ErrorResponse> handleSpecificException(...) { ... }
}
