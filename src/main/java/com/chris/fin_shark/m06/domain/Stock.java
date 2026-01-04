package com.chris.fin_shark.m06.domain;

import io.hypersistence.utils.hibernate.type.array.StringArrayType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 股票基本資料實體
 * <p>
 * 對應資料表: stocks
 * 使用 JPA 管理，包含 PostgreSQL TEXT[] 和 JSONB 欄位映射
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Entity
@Table(name = "stocks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    /** 股票代碼（主鍵，4-6 位數字） */
    @Id
    @Column(name = "stock_id", length = 10)
    private String stockId;

    /** 股票名稱（中文） */
    @Column(name = "stock_name", length = 50, nullable = false)
    private String stockName;

    /** 股票名稱（英文） */
    @Column(name = "stock_name_en", length = 100)
    private String stockNameEn;

    /** 市場類型（TWSE/OTC/EMERGING） */
    @Column(name = "market_type", length = 20, nullable = false)
    private String marketType;

    /** 產業別 */
    @Column(name = "industry", length = 50)
    private String industry;

    /** 產業子分類 */
    @Column(name = "sector", length = 50)
    private String sector;

    /** 上市日期 */
    @Column(name = "listing_date")
    private LocalDate listingDate;

    /** 下市日期 */
    @Column(name = "delisting_date")
    private LocalDate delistingDate;

    /** 是否為活躍股票 */
    @Column(name = "is_active")
    private Boolean isActive;

    /** 面額 */
    @Column(name = "par_value", precision = 10, scale = 2)
    private BigDecimal parValue;

    /** 已發行股數 */
    @Column(name = "issued_shares")
    private Long issuedShares;

    /** 市值 */
    @Column(name = "market_cap", precision = 20, scale = 2)
    private BigDecimal marketCap;

    /** 標籤陣列（使用 PostgreSQL TEXT[]） */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "tags")
    private List<String> tags;

    /** 額外資訊（使用 PostgreSQL JSONB） */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "extra_info")
    private Map<String, Object> extraInfo;

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
