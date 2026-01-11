package com.chris.fin_shark.m06.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 資料補齊結果資料傳輸物件
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataRepairResultDTO {

    /** 補齊策略 */
    private String strategy;

    /** 資料類型 */
    @JsonProperty("data_type")
    private String dataType;

    /** 開始日期 */
    @JsonProperty("start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /** 結束日期 */
    @JsonProperty("end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /** 股票代碼（單一股票補齊時使用） */
    @JsonProperty("stock_id")
    private String stockId;

    /** 總共需補齊筆數 */
    @JsonProperty("total_missing")
    private Integer totalMissing;

    /** 成功補齊筆數 */
    @JsonProperty("success_count")
    private Integer successCount;

    /** 失敗筆數 */
    @JsonProperty("failed_count")
    private Integer failedCount;

    /** 執行狀態（SUCCESS/PARTIAL/FAILED） */
    private String status;

    /** 執行開始時間 */
    @JsonProperty("start_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /** 執行結束時間 */
    @JsonProperty("end_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    /** 執行耗時（毫秒） */
    @JsonProperty("duration_ms")
    private Long durationMs;

    /** 錯誤訊息 */
    @JsonProperty("error_message")
    private String errorMessage;

    /** 補齊的日期列表 */
    @JsonProperty("repaired_dates")
    private List<LocalDate> repairedDates;

    /** 失敗的日期列表 */
    @JsonProperty("failed_dates")
    private List<LocalDate> failedDates;
}
