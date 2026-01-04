package com.chris.fin_shark.m07.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 指標定義實體
 * <p>
 * 對應資料表: indicator_definitions
 * 功能編號: F-M07-005
 * </p>
 * <p>
 * 設計說明:
 * <ul>
 *   <li>集中管理所有技術指標的定義與參數</li>
 *   <li>使用 JSONB 儲存參數配置，提供彈性擴充</li>
 *   <li>支援優先級分類（P0/P1/P2），控制計算頻率</li>
 * </ul>
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Entity
@Table(name = "indicator_definitions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorDefinition {

    /**
     * 定義ID（自增主鍵）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "definition_id")
    private Long definitionId;

    /**
     * 指標名稱（英文）
     * <p>
     * 範例: MA, EMA, RSI, MACD, KD
     * </p>
     */
    @Column(name = "indicator_name", length = 50, nullable = false, unique = true)
    private String indicatorName;

    /**
     * 指標類別
     * <p>
     * 範例: TREND, MOMENTUM, VOLATILITY, VOLUME, SUPPORT_RESISTANCE,
     *       CYCLE, STATISTICAL, COMPOSITE
     * </p>
     */
    @Column(name = "indicator_category", length = 50, nullable = false)
    private String indicatorCategory;

    /**
     * 指標名稱（中文）
     */
    @Column(name = "indicator_name_zh", length = 100)
    private String indicatorNameZh;

    /**
     * 指標說明
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ========== 參數定義 (JSONB) ==========

    /**
     * 預設參數 (JSONB)
     * <p>
     * 範例: {"period": 14} 或 {"periods": [5, 10, 20]}
     * </p>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "default_params", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> defaultParams;

    /**
     * 參數範圍 (JSONB)
     * <p>
     * 範例: {"min_period": 5, "max_period": 30}
     * </p>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "param_ranges", columnDefinition = "jsonb")
    private Map<String, Object> paramRanges;

    // ========== 計算資訊 ==========

    /**
     * 計算公式說明
     */
    @Column(name = "calculation_formula", columnDefinition = "TEXT")
    private String calculationFormula;

    /**
     * pandas-ta 函數名稱
     */
    @Column(name = "pandas_ta_function", length = 50)
    private String pandasTaFunction;

    /**
     * 最少資料點數
     * <p>
     * 計算此指標所需的最少歷史資料筆數
     * </p>
     */
    @Column(name = "min_data_points")
    private Integer minDataPoints;

    // ========== 輸出資訊 (JSONB) ==========

    /**
     * 輸出欄位 (JSONB)
     * <p>
     * 範例: ["ma5", "ma10", "ma20"] 或 ["rsi_14"]
     * </p>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "output_fields", columnDefinition = "jsonb")
    private Map<String, Object> outputFields;

    /**
     * 數值範圍 (JSONB)
     * <p>
     * 範例: {"min": 0, "max": 100}
     * </p>
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "value_range", columnDefinition = "jsonb")
    private Map<String, Object> valueRange;

    // ========== 優先級與啟用狀態 ==========

    /**
     * 優先級
     * <p>
     * P0: 基礎組（每日必算）<br>
     * P1: 進階組（每日必算）<br>
     * P2: 專業組（按需計算）
     * </p>
     */
    @Column(name = "priority", length = 10)
    private String priority;

    /**
     * 是否啟用
     */
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 是否快取
     */
    @Column(name = "is_cached")
    @Builder.Default
    private Boolean isCached = false;

    // ========== 審計欄位 ==========

    /**
     * 建立時間
     */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
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
