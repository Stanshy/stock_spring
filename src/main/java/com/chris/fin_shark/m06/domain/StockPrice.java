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
 * 股價歷史資料實體
 * <p>
 * 對應資料表: stock_prices（分區表）
 * 使用複合主鍵（price_id + trade_date）
 * 支援 JPA 簡單操作和 MyBatis 批次操作
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Entity
@Table(name = "stock_prices")
@IdClass(StockPriceId.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockPrice {

    /** 股票 ID（複合主鍵之一） */
    @Id
    @Column(name = "stock_id", length = 10, nullable = false)
    private String stockId;

    /** 交易日期（複合主鍵之一，也是分區鍵） */
    @Id
    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    /** 股價流水號 */
    @Column(name = "price_id", insertable = false, updatable = false)
    private Long priceId;

    /** 開盤價 */
    @Column(name = "open_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal openPrice;

    /** 最高價 */
    @Column(name = "high_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal highPrice;

    /** 最低價 */
    @Column(name = "low_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal lowPrice;

    /** 收盤價 */
    @Column(name = "close_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal closePrice;

    /** 成交量（股） */
    @Column(name = "volume", nullable = false)
    private Long volume;

    /** 成交金額（元） */
    @Column(name = "turnover", precision = 20, scale = 2)
    private BigDecimal turnover;

    /** 成交筆數 */
    @Column(name = "transactions")
    private Integer transactions;

    /** 漲跌價差 */
    @Column(name = "change_price", precision = 10, scale = 2)
    private BigDecimal changePrice;

    /** 漲跌幅（%） */
    @Column(name = "change_percent", precision = 5, scale = 2)
    private BigDecimal changePercent;

    /** 5 日均價 */
    @Column(name = "ma5", precision = 10, scale = 2)
    private BigDecimal ma5;

    /** 20 日均價 */
    @Column(name = "ma20", precision = 10, scale = 2)
    private BigDecimal ma20;

    /** 5 日均量 */
    @Column(name = "volume_ma5")
    private Long volumeMa5;

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
