package com.chris.fin_shark.m10.engine.model;

import com.chris.fin_shark.m10.enums.PatternCategory;
import com.chris.fin_shark.m10.enums.SignalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 型態元資料
 * <p>
 * 定義型態的基本資訊與偵測參數
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatternMetadata {

    /**
     * 型態 ID（如 KLINE001, CHART003）
     */
    private String patternId;

    /**
     * 型態名稱（中文）
     */
    private String nameZh;

    /**
     * 型態名稱（英文）
     */
    private String nameEn;

    /**
     * 型態類別
     */
    private PatternCategory category;

    /**
     * 預設訊號類型
     */
    private SignalType signalType;

    /**
     * 優先級（P0, P1, P2）
     */
    @Builder.Default
    private String priority = "P0";

    /**
     * 最少需要的 K 線數量
     */
    @Builder.Default
    private int minDataPoints = 1;

    /**
     * 建議的資料天數
     */
    @Builder.Default
    private int recommendedDataPoints = 30;

    /**
     * 型態描述
     */
    private String description;

    /**
     * 識別條件說明
     */
    private String identificationCriteria;

    /**
     * 預設參數
     */
    private Map<String, Object> defaultParams;

    /**
     * 可靠度等級（HIGH, MEDIUM, LOW）
     */
    @Builder.Default
    private String reliability = "MEDIUM";

    /**
     * 是否啟用
     */
    @Builder.Default
    private boolean enabled = true;
}
