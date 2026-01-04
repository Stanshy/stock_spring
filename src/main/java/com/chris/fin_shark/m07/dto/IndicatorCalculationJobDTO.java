package com.chris.fin_shark.m07.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 指標計算 Job 執行記錄資料傳輸物件
 * <p>
 * 用於 API 請求和回應
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorCalculationJobDTO {

    /** Job ID */
    @JsonProperty("job_id")
    private Long jobId;

    /** Job 類型 */
    @JsonProperty("job_type")
    private String jobType;

    /** 計算日期 */
    @JsonProperty("calculation_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate calculationDate;

    /** 股票清單 */
    @JsonProperty("stock_list")
    private String[] stockList;

    /** 指標優先級 */
    @JsonProperty("indicator_priority")
    private String indicatorPriority;

    /** 執行狀態 */
    @JsonProperty("status")
    private String status;

    /** 開始時間 */
    @JsonProperty("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 結束時間 */
    @JsonProperty("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 執行時長（秒） */
    @JsonProperty("duration_seconds")
    private Integer durationSeconds;

    /** 統計資訊 */
    @JsonProperty("statistics")
    private Map<String, Object> statistics;

    /** 錯誤訊息 */
    @JsonProperty("error_message")
    private String errorMessage;

    /** 建立時間 */
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 建立者 */
    @JsonProperty("created_by")
    private String createdBy;
}
