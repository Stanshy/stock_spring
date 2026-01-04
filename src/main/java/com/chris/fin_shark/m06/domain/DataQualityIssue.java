package com.chris.fin_shark.m06.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 資料品質問題記錄實體
 * <p>
 * 對應資料表: data_quality_issues
 * 記錄資料品質檢核發現的問題
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Entity
@Table(name = "data_quality_issues")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataQualityIssue {

    /** 問題 ID（自動遞增主鍵） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "issue_id")
    private Long issueId;

    /** 檢核 ID（外鍵，關聯 data_quality_checks） */
    @Column(name = "check_id", nullable = false)
    private Long checkId;

    /** 問題發生日期 */
    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    /** 受影響的資料筆數 */
    @Column(name = "affected_rows")
    private Integer affectedRows;

    /** 問題詳細描述 */
    @Column(name = "issue_detail", columnDefinition = "text")
    private String issueDetail;

    /** 嚴重性（HIGH/MEDIUM/LOW） */
    @Column(name = "severity", length = 10)
    private String severity;

    /** 狀態（OPEN/IN_PROGRESS/RESOLVED/IGNORED） */
    @Column(name = "status", length = 20)
    private String status;

    /** 解決時間 */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    /** 解決者 */
    @Column(name = "resolved_by", length = 50)
    private String resolvedBy;

    /** 備註 */
    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    /** 建立時間 */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 新增前自動設定建立時間
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
