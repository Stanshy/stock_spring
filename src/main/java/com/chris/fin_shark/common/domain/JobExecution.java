package com.chris.fin_shark.common.domain;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Job 執行記錄實體
 * <p>
 * 對應資料表: job_executions
 * 記錄所有 Job 的執行狀態、統計資訊、錯誤訊息
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Entity
@Table(name = "job_executions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobExecution {

    /** 執行 ID（自動遞增主鍵） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "execution_id")
    private Long executionId;

    /** Job 名稱 */
    @Column(name = "job_name", length = 100, nullable = false)
    private String jobName;

    /** Job 類型 */
    @Column(name = "job_type", length = 50, nullable = false)
    private String jobType;

    /** Job 狀態（PENDING/RUNNING/SUCCESS/FAILED/CANCELLED） */
    @Column(name = "job_status", length = 20, nullable = false)
    private String jobStatus;

    /** 執行參數（JSONB） */
    @Type(JsonBinaryType.class)
    @Column(name = "parameters", columnDefinition = "jsonb")
    private Map<String, Object> parameters;

    // ========== 執行時間資訊 ==========

    /** 開始時間 */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /** 結束時間 */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /** 執行時長（毫秒） */
    @Column(name = "duration_ms")
    private Long durationMs;

    // ========== 執行統計 ==========

    /** 總筆數 */
    @Column(name = "total_items")
    private Integer totalItems;

    /** 已處理筆數 */
    @Column(name = "processed_items")
    private Integer processedItems;

    /** 成功筆數 */
    @Column(name = "success_items")
    private Integer successItems;

    /** 失敗筆數 */
    @Column(name = "failed_items")
    private Integer failedItems;

    // ========== 錯誤資訊 ==========

    /** 錯誤訊息 */
    @Column(name = "error_message", columnDefinition = "text")
    private String errorMessage;

    /** 錯誤堆疊追蹤 */
    @Column(name = "error_stack_trace", columnDefinition = "text")
    private String errorStackTrace;

    // ========== 重試機制 ==========

    /** 重試次數 */
    @Column(name = "retry_count")
    private Integer retryCount;

    /** 最大重試次數 */
    @Column(name = "max_retry")
    private Integer maxRetry;

    /** 父執行 ID（重試時指向原始執行） */
    @Column(name = "parent_execution_id")
    private Long parentExecutionId;

    // ========== 觸發資訊 ==========

    /** 觸發類型（SCHEDULED/MANUAL/EVENT/RETRY） */
    @Column(name = "trigger_type", length = 20, nullable = false)
    private String triggerType;

    /** 觸發者（使用者 ID 或系統） */
    @Column(name = "triggered_by", length = 50)
    private String triggeredBy;

    // ========== 審計欄位 ==========

    /** 建立時間 */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 更新時間 */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 新增前自動設定時間戳
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * 更新前自動設定更新時間
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
