package com.chris.fin_shark.m11.domain;

import com.chris.fin_shark.m11.enums.FactorCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 因子元數據實體
 * <p>
 * 對應資料表: factor_metadata
 * 儲存可用於策略組合的因子定義
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactorMetadata {

    /**
     * 因子 ID（格式: M{模組}_{指標名}）
     */
    private String factorId;

    /**
     * 因子名稱
     */
    private String factorName;

    /**
     * 顯示名稱
     */
    private String displayName;

    /**
     * 因子類別
     */
    private FactorCategory category;

    /**
     * 來源模組（M06/M07/M08/M09/M11）
     */
    private String sourceModule;

    /**
     * 數據類型（NUMERIC/INTEGER/BOOLEAN/STRING/ENUM）
     */
    private String dataType;

    /**
     * 數值範圍（JSONB）
     * <pre>
     * {"min": 0, "max": 100}
     * </pre>
     */
    private Map<String, Object> valueRange;

    /**
     * 典型閾值（JSONB）
     * <pre>
     * [30, 70]
     * </pre>
     */
    private Map<String, Object> typicalThresholds;

    /**
     * 支援的運算子（JSONB）
     */
    private Map<String, Object> supportedOperators;

    /**
     * 預設運算子
     */
    private String defaultOperator;

    /**
     * 因子說明
     */
    private String description;

    /**
     * 計算公式說明
     */
    private String calculationFormula;

    /**
     * 更新頻率（DAILY/WEEKLY/MONTHLY/QUARTERLY）
     */
    @Builder.Default
    private String updateFrequency = "DAILY";

    /**
     * 是否啟用
     */
    @Builder.Default
    private Boolean isActive = true;

    /**
     * 建立時間
     */
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    private LocalDateTime updatedAt;
}
