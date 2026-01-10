package com.chris.fin_shark.m06.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Job 執行記錄資料傳輸物件
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobExecutionDTO {

    /** 執行 ID */
    @JsonProperty("execution_id")
    private Long executionId;

    /** Job 名稱 */
    @JsonProperty("job_name")
    private String jobName;

    /** Job 類型 */
    @JsonProperty("job_type")
    private String jobType;

    /** Job 狀態 */
    @JsonProperty("job_status")
    private String jobStatus;

    /** 執行參數 */
    @JsonProperty("parameters")
    private Map<String, Object> parameters;

    /** 開始時間 */
    @JsonProperty("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 結束時間 */
    @JsonProperty("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 執行時長（毫秒） */
    @JsonProperty("duration_ms")
    private Long durationMs;

    /** 總筆數 */
    @JsonProperty("total_items")
    private Integer totalItems;

    /** 已處理筆數 */
    @JsonProperty("processed_items")
    private Integer processedItems;

    /** 成功筆數 */
    @JsonProperty("success_items")
    private Integer successItems;

    /** 失敗筆數 */
    @JsonProperty("failed_items")
    private Integer failedItems;

    /** 錯誤訊息 */
    @JsonProperty("error_message")
    private String errorMessage;

    /** 重試次數 */
    @JsonProperty("retry_count")
    private Integer retryCount;

    /** 觸發類型 */
    @JsonProperty("trigger_type")
    private String triggerType;

    /** 觸發者 */
    @JsonProperty("triggered_by")
    private String triggeredBy;

    /** 建立時間 */
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
