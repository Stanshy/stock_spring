package com.chris.fin_shark.m07.engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 指標計算結果
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorResult {

    /** 股票代碼 */
    private String stockId;

    /** 計算日期 */
    private LocalDate calculationDate;

    /** 趨勢指標 */
    @Builder.Default
    private Map<String, Object> trendIndicators = new HashMap<>();

    /** 動能指標 */
    @Builder.Default
    private Map<String, Object> momentumIndicators = new HashMap<>();

    /** 波動性指標 */
    @Builder.Default
    private Map<String, Object> volatilityIndicators = new HashMap<>();

    /** 成交量指標 */
    @Builder.Default
    private Map<String, Object> volumeIndicators = new HashMap<>();

    /** 診斷訊息 */
    private Diagnostics diagnostics;

    /**
     * 取得指標值（自動從各類別中查找）
     */
    public Object getValue(String key) {
        Object value = trendIndicators.get(key);
        if (value != null) return value;

        value = momentumIndicators.get(key);
        if (value != null) return value;

        value = volatilityIndicators.get(key);
        if (value != null) return value;

        value = volumeIndicators.get(key);
        if (value != null) return value;

        return null;
    }

    /**
     * 是否有錯誤
     */
    public boolean hasErrors() {
        return diagnostics != null && diagnostics.hasErrors();
    }

    /**
     * 是否有警告
     */
    public boolean hasWarnings() {
        return diagnostics != null && diagnostics.hasWarnings();
    }

    /**
     * 加入趨勢指標
     */
    public void addTrendIndicator(String key, Object value) {
        if (trendIndicators == null) {
            trendIndicators = new HashMap<>();
        }
        trendIndicators.put(key, value);
    }

    /**
     * 加入動能指標
     */
    public void addMomentumIndicator(String key, Object value) {
        if (momentumIndicators == null) {
            momentumIndicators = new HashMap<>();
        }
        momentumIndicators.put(key, value);
    }

    /**
     * 加入波動性指標
     */
    public void addVolatilityIndicator(String key, Object value) {
        if (volatilityIndicators == null) {
            volatilityIndicators = new HashMap<>();
        }
        volatilityIndicators.put(key, value);
    }

    /**
     * 加入成交量指標
     */
    public void addVolumeIndicator(String key, Object value) {
        if (volumeIndicators == null) {
            volumeIndicators = new HashMap<>();
        }
        volumeIndicators.put(key, value);
    }
}
