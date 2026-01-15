package com.chris.fin_shark.m10.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 支撐壓力位實體
 * <p>
 * 對應資料表: support_resistance_levels
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Entity
@Table(name = "support_resistance_levels")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportResistanceLevel {

    /**
     * 價位 ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "level_id")
    private Long levelId;

    /**
     * 股票代碼
     */
    @Column(name = "stock_id", nullable = false, length = 10)
    private String stockId;

    /**
     * 分析日期
     */
    @Column(name = "analysis_date", nullable = false)
    private LocalDate analysisDate;

    // === 價位資訊 ===

    /**
     * 價位
     */
    @Column(name = "price_level", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceLevel;

    /**
     * 價位類型: SUPPORT, RESISTANCE
     */
    @Column(name = "level_type", nullable = false, length = 15)
    private String levelType;

    /**
     * 強度（0-100）
     */
    @Column(name = "strength", nullable = false)
    private Integer strength;

    // === 識別來源 ===

    /**
     * 來源類型: WAVE_PEAK, WAVE_TROUGH, MOVING_AVERAGE, VOLUME_PROFILE,
     *          PSYCHOLOGICAL, GAP, FIBONACCI, HISTORICAL, PIVOT
     */
    @Column(name = "source_type", nullable = false, length = 30)
    private String sourceType;

    /**
     * 來源描述
     */
    @Column(name = "source_description", length = 100)
    private String sourceDescription;

    // === 測試紀錄 ===

    /**
     * 測試次數
     */
    @Column(name = "test_count")
    @Builder.Default
    private Integer testCount = 0;

    /**
     * 最後測試日期
     */
    @Column(name = "last_test_date")
    private LocalDate lastTestDate;

    /**
     * 突破次數
     */
    @Column(name = "break_count")
    @Builder.Default
    private Integer breakCount = 0;

    // === 距離當前價格 ===

    /**
     * 當前價格
     */
    @Column(name = "current_price", precision = 10, scale = 2)
    private BigDecimal currentPrice;

    /**
     * 距離百分比
     */
    @Column(name = "distance_percent", precision = 8, scale = 2)
    private BigDecimal distancePercent;

    // === 狀態 ===

    /**
     * 是否有效
     */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 失效時間
     */
    @Column(name = "invalidated_at")
    private LocalDateTime invalidatedAt;

    /**
     * 失效原因
     */
    @Column(name = "invalidation_reason", length = 100)
    private String invalidationReason;

    // === 審計欄位 ===

    /**
     * 建立時間
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
