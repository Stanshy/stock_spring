package com.chris.fin_shark.m06.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 資料品質統計摘要資料傳輸物件
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataQualitySummaryDTO {

    /** 啟用的檢核規則數量 */
    @JsonProperty("total_active_checks")
    private Long totalActiveChecks;

    /** 未解決問題總數 */
    @JsonProperty("total_open_issues")
    private Long totalOpenIssues;

    /** 高嚴重性問題數 */
    @JsonProperty("high_severity_issues")
    private Long highSeverityIssues;

    /** 中嚴重性問題數 */
    @JsonProperty("medium_severity_issues")
    private Long mediumSeverityIssues;

    /** 低嚴重性問題數 */
    @JsonProperty("low_severity_issues")
    private Long lowSeverityIssues;

    /** 今日新增問題數 */
    @JsonProperty("today_new_issues")
    private Long todayNewIssues;

    /** 今日解決問題數 */
    @JsonProperty("today_resolved_issues")
    private Long todayResolvedIssues;

    /** 資料品質分數（0-100） */
    @JsonProperty("quality_score")
    private Integer qualityScore;
}
