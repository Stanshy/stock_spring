package com.chris.fin_shark.m11.domain;

import com.chris.fin_shark.common.enums.SignalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 策略信號實體
 * <p>
 * 對應資料表: strategy_signals（按月分區）
 * 儲存策略執行產生的信號，供 M13 信號引擎消費
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategySignal {

    /**
     * 信號 ID（格式: STG_SIG_yyyyMMdd_xxx）
     */
    private String signalId;

    /**
     * 執行 ID
     */
    private String executionId;

    /**
     * 策略 ID
     */
    private String strategyId;

    /**
     * 策略版本
     */
    private Integer strategyVersion;

    /**
     * 股票代碼
     */
    private String stockId;

    /**
     * 交易日期
     */
    private LocalDate tradeDate;

    /**
     * 信號類型（BUY/SELL/HOLD）
     */
    private SignalType signalType;

    /**
     * 信心度分數（0-100）
     */
    private BigDecimal confidenceScore;

    /**
     * 匹配的條件詳情（JSONB）
     * <pre>
     * [
     *   {"factor_id": "M07_RSI_14", "factor_value": 25.3, "operator": "LESS_THAN", "threshold": 30, "matched": true},
     *   ...
     * ]
     * </pre>
     */
    private Map<String, Object> matchedConditions;

    /**
     * 因子數值快照（JSONB）
     */
    private Map<String, Object> factorValues;

    /**
     * 是否已被 M13 消費
     */
    @Builder.Default
    private Boolean isConsumed = false;

    /**
     * 消費者（M13 信號 ID）
     */
    private String consumedBy;

    /**
     * 消費時間
     */
    private LocalDateTime consumedAt;

    /**
     * 建立時間
     */
    private LocalDateTime createdAt;
}
