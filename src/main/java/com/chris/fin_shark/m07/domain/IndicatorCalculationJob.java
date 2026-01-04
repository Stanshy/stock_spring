package com.chris.fin_shark.m07.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 指標計算 Job 執行記錄實體
 * <p>
 * 對應資料表: indicator_calculation_jobs
 * 功能編號: F-M07-013
 * </p>
 * <p>
 * 設計說明:
 * <ul>
 *   <li>記錄每次指標計算 Job 的執行狀態與統計資訊</li>
 *   <li>支援冪等性檢查與錯誤追蹤</li>
 *   <li>使用 JSONB 儲存統計資訊，提供彈性擴充</li>
 * </ul>
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Entity
@Table(name = "indicator_calculation_jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorCalculationJob {

    /**
     * Job ID（自增主鍵）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "job_id")
    private Long jobId;

    /**
     * Job 類型
     */
    @Column(name = "job_type", length = 50)
    @Builder.Default
    private String jobType = "CALCULATE_INDICATORS";

    /**
     * 計算日期
     */
    @Column(name = "calculation_date", nullable = false)
    private LocalDate calculationDate;

    // ========== Job 參數 ==========

    /**
     * 股票清單
     * <p>
     * PostgreSQL TEXT[] 陣列
     * </p>
     */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "stock_list", columnDefinition = "text[]")
    private String[] stockList;

    /**
     * 指標優先級
     * <p>
     * P0, P1, P2
     * </p>
     */
    @Column(name = "indicator_priority", length = 10)
    private String indicatorPriority;

    // ========== 執行資訊 ==========

    /**
     * 執行狀態
     * <p>
     * PENDING, RUNNING, SUCCESS, FAILED, CANCELLED
     * </p>
     */
    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "PENDING";

    /**
     * 開始時間
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * 結束時間
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 執行時長（秒）
     */
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    // ========== 統計資訊 (JSONB) ==========

    /**
     * 統計資訊 (JSONB)
     * <p>
     * 範例:
     * <pre>
     * {
     *   "total_stocks": 1800,
     *   "success_count": 1785,
     *   "failed_count": 15,
     *   "indicators_calculated": ["MA", "EMA", "RSI"],
     *   "average_time_per_stock_ms": 250
     * }
     * </pre>
     * </p>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "statistics", columnDefinition = "jsonb")
    @Builder.Default
    private Map<String, Object> statistics = Map.of();

    // ========== 錯誤資訊 ==========

    /**
     * 錯誤訊息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 錯誤堆疊追蹤
     */
    @Column(name = "error_stack_trace", columnDefinition = "TEXT")
    private String errorStackTrace;

    // ========== 審計欄位 ==========

    /**
     * 建立時間
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 建立者
     */
    @Column(name = "created_by", length = 50)
    @Builder.Default
    private String createdBy = "SYSTEM";

    /**
     * 新增前自動設定建立時間
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
