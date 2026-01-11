package com.chris.fin_shark.m06.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 品質檢核結果資料傳輸物件
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityCheckResultDTO {

    /** 檢核 ID */
    @JsonProperty("check_id")
    private Long checkId;

    /** 檢核名稱 */
    @JsonProperty("check_name")
    private String checkName;

    /** 檢核類型 */
    @JsonProperty("check_type")
    private String checkType;

    /** 目標資料表 */
    @JsonProperty("target_table")
    private String targetTable;

    /** 執行狀態（SUCCESS/FAILED） */
    private String status;

    /** 檢核結果（PASS/FAIL） */
    private String result;

    /** 發現的問題數量 */
    @JsonProperty("issue_count")
    private Integer issueCount;

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

    /** 檢核詳細結果 */
    private List<Map<String, Object>> details;

    /** 品質分數（0-100） */
    @JsonProperty("quality_score")
    private Integer qualityScore;

    /** 統計摘要 */
    private Map<String, Object> summary;
}
