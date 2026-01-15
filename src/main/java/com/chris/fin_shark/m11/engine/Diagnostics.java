package com.chris.fin_shark.m11.engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 策略執行診斷資訊
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Diagnostics {

    /**
     * 載入的因子數
     */
    @Builder.Default
    private int factorsLoaded = 0;

    /**
     * 缺失的因子數
     */
    @Builder.Default
    private int factorsMissing = 0;

    /**
     * 跳過的股票數
     */
    @Builder.Default
    private int stocksSkipped = 0;

    /**
     * 跳過原因統計
     */
    @Builder.Default
    private Map<String, Integer> skipReasons = new HashMap<>();

    /**
     * 計算錯誤數
     */
    @Builder.Default
    private int calculationErrors = 0;

    /**
     * 警告訊息
     */
    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    /**
     * 錯誤訊息
     */
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    /**
     * 新增警告
     */
    public void addWarning(String warning) {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        warnings.add(warning);
    }

    /**
     * 新增錯誤
     */
    public void addError(String error) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(error);
        calculationErrors++;
    }

    /**
     * 記錄跳過的股票
     */
    public void recordSkipped(String reason) {
        stocksSkipped++;
        if (skipReasons == null) {
            skipReasons = new HashMap<>();
        }
        skipReasons.merge(reason, 1, Integer::sum);
    }

    /**
     * 記錄載入的因子
     */
    public void recordFactorLoaded() {
        factorsLoaded++;
    }

    /**
     * 記錄缺失的因子
     */
    public void recordFactorMissing(String factorId) {
        factorsMissing++;
        addWarning(String.format("因子缺失: %s", factorId));
    }

    /**
     * 轉換為 Map（用於 JSONB 儲存）
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("factors_loaded", factorsLoaded);
        map.put("factors_missing", factorsMissing);
        map.put("stocks_skipped", stocksSkipped);
        map.put("skip_reasons", skipReasons);
        map.put("calculation_errors", calculationErrors);
        map.put("warnings", warnings);
        return map;
    }
}
