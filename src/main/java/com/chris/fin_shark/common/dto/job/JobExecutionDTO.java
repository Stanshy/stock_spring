package com.chris.fin_shark.common.dto.job;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Job 執行記錄資料傳輸物件
 * <p>
 * 遵守總綱 4.5 Job/排程模型規範
 * 統一的 Job 執行記錄格式，供所有模組使用
 * </p>
 * <p>
 * 對應資料表: job_executions
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JobExecutionDTO {

    /**
     * 執行 ID（主鍵）
     */
    @JsonProperty("execution_id")
    private Long executionId;

    /**
     * Job 名稱
     * <p>
     * 例如: SYNC_STOCK_PRICES, CALCULATE_INDICATORS
     * </p>
     */
    @JsonProperty("job_name")
    private String jobName;

    /**
     * Job 類型
     * <p>
     * 例如: DATA_SYNC, CALCULATION, SIGNAL_DETECTION
     * </p>
     */
    @JsonProperty("job_type")
    private String jobType;

    /**
     * Job 狀態
     * <p>
     * 值: PENDING, RUNNING, SUCCESS, FAILED, CANCELLED
     * </p>
     */
    @JsonProperty("job_status")
    private String jobStatus;

    /**
     * 執行參數（JSONB）
     * <p>
     * 例如: {"trade_date": "2024-12-22", "stock_ids": ["2330", "2317"]}
     * </p>
     */
    @JsonProperty("parameters")
    private Map<String, Object> parameters;

    /**
     * 開始時間
     */
    @JsonProperty("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 結束時間
     */
    @JsonProperty("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /**
     * 執行時長（毫秒）
     */
    @JsonProperty("duration_ms")
    private Long durationMs;

    /**
     * 總筆數
     */
    @JsonProperty("total_items")
    private Integer totalItems;

    /**
     * 已處理筆數
     */
    @JsonProperty("processed_items")
    private Integer processedItems;

    /**
     * 成功筆數
     */
    @JsonProperty("success_items")
    private Integer successItems;

    /**
     * 失敗筆數
     */
    @JsonProperty("failed_items")
    private Integer failedItems;

    /**
     * 錯誤訊息（失敗時）
     */
    @JsonProperty("error_message")
    private String errorMessage;

    /**
     * 錯誤堆疊追蹤（失敗時，僅供除錯使用）
     * <p>
     * 注意: 此欄位較長，建議僅在開發/測試環境返回
     * </p>
     */
    @JsonProperty("error_stack_trace")
    private String errorStackTrace;

    /**
     * 重試次數
     */
    @JsonProperty("retry_count")
    private Integer retryCount;

    /**
     * 最大重試次數
     */
    @JsonProperty("max_retry")
    private Integer maxRetry;

    /**
     * 父 Job 執行 ID（用於子任務）
     */
    @JsonProperty("parent_execution_id")
    private Long parentExecutionId;

    /**
     * 觸發類型
     * <p>
     * 值: SCHEDULED, MANUAL, EVENT, RETRY
     * </p>
     */
    @JsonProperty("trigger_type")
    private String triggerType;

    /**
     * 觸發者
     * <p>
     * 例如: "system", "admin", "user123"
     * </p>
     */
    @JsonProperty("triggered_by")
    private String triggeredBy;

    /**
     * 建立時間
     */
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @JsonProperty("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    /**
     * 計算成功率（百分比）
     *
     * @return 成功率 (0-100)
     */
    public Double getSuccessRate() {
        if (totalItems == null || totalItems == 0) {
            return 0.0;
        }
        if (successItems == null) {
            return 0.0;
        }
        return (successItems * 100.0) / totalItems;
    }

    /**
     * 計算處理進度（百分比）
     *
     * @return 處理進度 (0-100)
     */
    public Double getProcessProgress() {
        if (totalItems == null || totalItems == 0) {
            return 0.0;
        }
        if (processedItems == null) {
            return 0.0;
        }
        return (processedItems * 100.0) / totalItems;
    }
}

