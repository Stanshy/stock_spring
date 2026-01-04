package com.chris.fin_shark.m06.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 資料品質問題資料傳輸物件
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataQualityIssueDTO {

    /**
     * 問題 ID
     */
    @JsonProperty("issue_id")
    private Long issueId;

    /**
     * 檢核 ID
     */
    @JsonProperty("check_id")
    private Long checkId;

    /**
     * 檢核名稱（關聯查詢）
     */
    @JsonProperty("check_name")
    private String checkName;

    /**
     * 問題發生日期
     */
    @JsonProperty("issue_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate issueDate;

    /**
     * 受影響的資料筆數
     */
    @JsonProperty("affected_rows")
    private Integer affectedRows;

    /**
     * 問題詳細描述
     */
    @JsonProperty("issue_detail")
    private String issueDetail;

    /**
     * 嚴重性
     */
    @JsonProperty("severity")
    private String severity;

    /**
     * 狀態
     */
    @JsonProperty("status")
    private String status;

    /**
     * 解決時間
     */
    @JsonProperty("resolved_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime resolvedAt;

    /**
     * 解決者
     */
    @JsonProperty("resolved_by")
    private String resolvedBy;

    /**
     * 備註
     */
    @JsonProperty("notes")
    private String notes;

    /**
     * 建立時間
     */
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}