package com.chris.fin_shark.m06.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 資料品質檢核規則實體
 * <p>
 * 對應資料表: data_quality_checks
 * 定義資料品質檢核的規則與配置
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Entity
@Table(name = "data_quality_checks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataQualityCheck {

    /** 檢核 ID（自動遞增主鍵） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_id")
    private Long checkId;

    /** 檢核名稱 */
    @Column(name = "check_name", length = 100, nullable = false)
    private String checkName;

    /** 檢核類型（例如: FOUR_PRICE, BALANCE_SHEET, VOLUME） */
    @Column(name = "check_type", length = 50, nullable = false)
    private String checkType;

    /** 目標資料表 */
    @Column(name = "target_table", length = 50, nullable = false)
    private String targetTable;

    /** 檢核規則（SQL 或規則描述） */
    @Column(name = "check_rule", columnDefinition = "text", nullable = false)
    private String checkRule;

    /** 嚴重性（HIGH/MEDIUM/LOW） */
    @Column(name = "severity", length = 10)
    private String severity;

    /** 是否啟用 */
    @Column(name = "is_active")
    private Boolean isActive;

    /** 最後檢核時間 */
    @Column(name = "last_check_time")
    private LocalDateTime lastCheckTime;

    /** 最後檢核結果（PASS/FAIL/WARNING） */
    @Column(name = "last_result", length = 10)
    private String lastResult;

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
