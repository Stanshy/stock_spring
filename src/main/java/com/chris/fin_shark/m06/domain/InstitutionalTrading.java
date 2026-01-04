package com.chris.fin_shark.m06.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 三大法人買賣超實體
 * <p>
 * 對應資料表: institutional_trading
 * 包含 4 個 GENERATED COLUMN（foreign_net, trust_net, dealer_net, total_net）
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Entity
@Table(name = "institutional_trading")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionalTrading {

    /** 交易 ID（自動遞增主鍵） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "trading_id")
    private Long tradingId;

    /** 股票代碼 */
    @Column(name = "stock_id", length = 10, nullable = false)
    private String stockId;

    /** 交易日期 */
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    // ========== 外資 (Foreign Investors) ==========

    /** 外資買進（股） */
    @Column(name = "foreign_buy")
    private Long foreignBuy;

    /** 外資賣出（股） */
    @Column(name = "foreign_sell")
    private Long foreignSell;

    /** 外資買賣超（GENERATED COLUMN - 由資料庫自動計算） */
    @Column(name = "foreign_net", insertable = false, updatable = false)
    private Long foreignNet;

    // ========== 投信 (Investment Trust) ==========

    /** 投信買進（股） */
    @Column(name = "trust_buy")
    private Long trustBuy;

    /** 投信賣出（股） */
    @Column(name = "trust_sell")
    private Long trustSell;

    /** 投信買賣超（GENERATED COLUMN - 由資料庫自動計算） */
    @Column(name = "trust_net", insertable = false, updatable = false)
    private Long trustNet;

    // ========== 自營商 (Dealers) ==========

    /** 自營商買進（股） */
    @Column(name = "dealer_buy")
    private Long dealerBuy;

    /** 自營商賣出（股） */
    @Column(name = "dealer_sell")
    private Long dealerSell;

    /** 自營商買賣超（GENERATED COLUMN - 由資料庫自動計算） */
    @Column(name = "dealer_net", insertable = false, updatable = false)
    private Long dealerNet;

    // ========== 合計 ==========

    /** 三大法人合計買賣超（GENERATED COLUMN - 由資料庫自動計算） */
    @Column(name = "total_net", insertable = false, updatable = false)
    private Long totalNet;

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

