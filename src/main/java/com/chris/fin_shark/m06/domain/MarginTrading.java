package com.chris.fin_shark.m06.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 融資融券實體
 * <p>
 * 對應資料表: margin_trading
 * 包含 2 個 GENERATED COLUMN（margin_usage_rate, short_usage_rate）
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Entity
@Table(name = "margin_trading")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarginTrading {

    /** 融資融券 ID（自動遞增主鍵） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "margin_id")
    private Long marginId;

    /** 股票代碼 */
    @Column(name = "stock_id", length = 10, nullable = false)
    private String stockId;

    /** 交易日期 */
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    // ========== 融資 (Margin) ==========

    /** 融資買進（股） */
    @Column(name = "margin_purchase")
    private Long marginPurchase;

    /** 融資賣出（股） */
    @Column(name = "margin_sell")
    private Long marginSell;

    /** 融資餘額（股） */
    @Column(name = "margin_balance")
    private Long marginBalance;

    /** 融資限額（股） */
    @Column(name = "margin_quota")
    private Long marginQuota;

    /** 融資使用率（%，GENERATED COLUMN - 由資料庫自動計算） */
    @Column(name = "margin_usage_rate", precision = 5, scale = 2, insertable = false, updatable = false)
    private BigDecimal marginUsageRate;

    // ========== 融券 (Short) ==========

    /** 融券買進（股） */
    @Column(name = "short_purchase")
    private Long shortPurchase;

    /** 融券賣出（股） */
    @Column(name = "short_sell")
    private Long shortSell;

    /** 融券餘額（股） */
    @Column(name = "short_balance")
    private Long shortBalance;

    /** 融券限額（股） */
    @Column(name = "short_quota")
    private Long shortQuota;

    /** 融券使用率（%，GENERATED COLUMN - 由資料庫自動計算） */
    @Column(name = "short_usage_rate", precision = 5, scale = 2, insertable = false, updatable = false)
    private BigDecimal shortUsageRate;

    // ========== 審計欄位 ==========

    /** 建立時間 */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 更新時間 */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 新增前自動設定時間戳
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * 更新前自動設定更新時間
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
