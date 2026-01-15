package com.chris.fin_shark.m09.engine;

import com.chris.fin_shark.m09.engine.model.ChipSignal;
import com.chris.fin_shark.m09.engine.model.Diagnostics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 籌碼計算結果
 * <p>
 * 籌碼計算引擎的輸出結果，包含各類指標與偵測到的訊號。
 * 與 M07 IndicatorResult 結構對齊。
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChipResult {

    /** 股票代碼 */
    private String stockId;

    /** 計算日期 */
    private LocalDate calculationDate;

    /** 三大法人指標 */
    @Builder.Default
    private Map<String, Object> institutionalIndicators = new HashMap<>();

    /** 融資融券指標 */
    @Builder.Default
    private Map<String, Object> marginIndicators = new HashMap<>();

    /** 籌碼集中度指標 */
    @Builder.Default
    private Map<String, Object> concentrationIndicators = new HashMap<>();

    /** 主力成本指標 */
    @Builder.Default
    private Map<String, Object> costIndicators = new HashMap<>();

    /** 偵測到的異常訊號 */
    @Builder.Default
    private List<ChipSignal> signals = new ArrayList<>();

    /** 籌碼評分（0-100） */
    private Integer chipScore;

    /** 籌碼評級（A/B/C/D/F） */
    private String chipGrade;

    /** 診斷訊息 */
    private Diagnostics diagnostics;

    // ========== 便利方法 ==========

    /**
     * 取得指標值（自動從各類別中查找）
     */
    public Object getValue(String key) {
        Object value = institutionalIndicators.get(key);
        if (value != null) return value;

        value = marginIndicators.get(key);
        if (value != null) return value;

        value = concentrationIndicators.get(key);
        if (value != null) return value;

        value = costIndicators.get(key);
        return value;
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
     * 是否有訊號
     */
    public boolean hasSignals() {
        return signals != null && !signals.isEmpty();
    }

    // ========== 指標新增方法 ==========

    /**
     * 新增三大法人指標
     */
    public void addInstitutionalIndicator(String key, Object value) {
        if (institutionalIndicators == null) {
            institutionalIndicators = new HashMap<>();
        }
        institutionalIndicators.put(key, value);
    }

    /**
     * 新增融資融券指標
     */
    public void addMarginIndicator(String key, Object value) {
        if (marginIndicators == null) {
            marginIndicators = new HashMap<>();
        }
        marginIndicators.put(key, value);
    }

    /**
     * 新增籌碼集中度指標
     */
    public void addConcentrationIndicator(String key, Object value) {
        if (concentrationIndicators == null) {
            concentrationIndicators = new HashMap<>();
        }
        concentrationIndicators.put(key, value);
    }

    /**
     * 新增主力成本指標
     */
    public void addCostIndicator(String key, Object value) {
        if (costIndicators == null) {
            costIndicators = new HashMap<>();
        }
        costIndicators.put(key, value);
    }

    /**
     * 新增訊號
     */
    public void addSignal(ChipSignal signal) {
        if (signals == null) {
            signals = new ArrayList<>();
        }
        signals.add(signal);
    }

    /**
     * 批次新增訊號
     */
    public void addSignals(List<ChipSignal> newSignals) {
        if (newSignals == null || newSignals.isEmpty()) {
            return;
        }
        if (signals == null) {
            signals = new ArrayList<>();
        }
        signals.addAll(newSignals);
    }

    /**
     * 合併另一個結果的指標（同一股票）
     */
    public void merge(ChipResult other) {
        if (other == null) {
            return;
        }

        if (other.institutionalIndicators != null) {
            if (this.institutionalIndicators == null) {
                this.institutionalIndicators = new HashMap<>();
            }
            this.institutionalIndicators.putAll(other.institutionalIndicators);
        }

        if (other.marginIndicators != null) {
            if (this.marginIndicators == null) {
                this.marginIndicators = new HashMap<>();
            }
            this.marginIndicators.putAll(other.marginIndicators);
        }

        if (other.concentrationIndicators != null) {
            if (this.concentrationIndicators == null) {
                this.concentrationIndicators = new HashMap<>();
            }
            this.concentrationIndicators.putAll(other.concentrationIndicators);
        }

        if (other.costIndicators != null) {
            if (this.costIndicators == null) {
                this.costIndicators = new HashMap<>();
            }
            this.costIndicators.putAll(other.costIndicators);
        }

        if (other.signals != null && !other.signals.isEmpty()) {
            addSignals(other.signals);
        }

        if (other.diagnostics != null && this.diagnostics != null) {
            this.diagnostics.merge(other.diagnostics);
        }
    }
}
