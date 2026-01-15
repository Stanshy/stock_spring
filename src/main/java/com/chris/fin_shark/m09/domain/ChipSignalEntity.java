package com.chris.fin_shark.m09.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 籌碼異常訊號實體
 * <p>
 * 對應資料表: chip_signals
 * 功能編號: F-M09-005
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Entity
@Table(name = "chip_signals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChipSignalEntity {

    /**
     * 訊號ID（自增主鍵）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "signal_id")
    private Long signalId;

    /**
     * 股票代碼
     */
    @Column(name = "stock_id", length = 10, nullable = false)
    private String stockId;

    /**
     * 交易日期
     */
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    // ========== 訊號資訊 ==========

    /**
     * 訊號代碼（如 CHIP_SIG_001）
     */
    @Column(name = "signal_code", length = 20, nullable = false)
    private String signalCode;

    /**
     * 訊號名稱
     */
    @Column(name = "signal_name", length = 50, nullable = false)
    private String signalName;

    /**
     * 訊號類型（INSTITUTIONAL / MARGIN / CONCENTRATION / COMPOSITE）
     */
    @Column(name = "signal_type", length = 20, nullable = false)
    private String signalType;

    /**
     * 嚴重度（CRITICAL / HIGH / MEDIUM / LOW）
     */
    @Column(name = "severity", length = 10, nullable = false)
    private String severity;

    // ========== 訊號數值 ==========

    /**
     * 訊號觸發值
     */
    @Column(name = "signal_value", precision = 20, scale = 2)
    private BigDecimal signalValue;

    /**
     * 門檻值
     */
    @Column(name = "threshold_value", precision = 20, scale = 2)
    private BigDecimal thresholdValue;

    /**
     * 偏離程度（標準差倍數）
     */
    @Column(name = "deviation", precision = 10, scale = 2)
    private BigDecimal deviation;

    // ========== 描述 ==========

    /**
     * 訊號描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 建議操作
     */
    @Column(name = "recommendation", columnDefinition = "TEXT")
    private String recommendation;

    // ========== 狀態 ==========

    /**
     * 是否有效
     */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 確認時間
     */
    @Column(name = "acknowledged_at")
    private LocalDateTime acknowledgedAt;

    /**
     * 確認人
     */
    @Column(name = "acknowledged_by", length = 50)
    private String acknowledgedBy;

    // ========== 審計欄位 ==========

    /**
     * 建立時間
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 新增前自動設定時間戳
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
