package com.chris.fin_shark.m11.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 因子數據 DTO
 * <p>
 * 用於策略執行時載入各模組的因子數據
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactorDataDTO {

    /**
     * 股票代碼
     */
    private String stockId;

    /**
     * 股票名稱
     */
    private String stockName;

    /**
     * 市場類型
     */
    private String marketType;

    /**
     * 產業類別
     */
    private String industry;

    /**
     * 交易日期
     */
    private LocalDate tradeDate;

    // ========== M06 價量因子 ==========

    private BigDecimal closePrice;
    private Long volume;
    private BigDecimal volumeRatio;
    private BigDecimal priceChangePct;

    // ========== M07 技術指標 ==========

    private BigDecimal rsi14;
    private BigDecimal macdHistogram;
    private BigDecimal kdK;
    private BigDecimal kdD;
    private BigDecimal ma5;
    private BigDecimal ma20;
    private BigDecimal ma60;
    private BigDecimal bollingerUpper;
    private BigDecimal bollingerLower;
    private BigDecimal bollingerMiddle;

    // ========== M08 財務指標 ==========

    private BigDecimal peRatio;
    private BigDecimal pbRatio;
    private BigDecimal roe;
    private BigDecimal eps;
    private BigDecimal dividendYield;
    private BigDecimal revenueGrowthYoy;
    private BigDecimal profitMargin;

    // ========== M09 籌碼指標 ==========

    private Long foreignNet;
    private Integer foreignContinuousDays;
    private Long foreignAccumulated20d;
    private Long trustNet;
    private Integer trustContinuousDays;
    private Long dealerNet;
    private Long totalNet;
    private Long marginBalance;
    private Long marginChange;
    private BigDecimal marginShortRatio;
    private BigDecimal chipScore;

    /**
     * 轉換為因子值 Map（供條件評估使用）
     */
    public Map<String, Object> toFactorMap() {
        Map<String, Object> map = new HashMap<>();

        // M06
        putIfNotNull(map, "M06_CLOSE_PRICE", closePrice);
        putIfNotNull(map, "M06_VOLUME", volume);
        putIfNotNull(map, "M06_VOLUME_RATIO", volumeRatio);
        putIfNotNull(map, "M06_PRICE_CHANGE_PCT", priceChangePct);

        // M07
        putIfNotNull(map, "M07_RSI_14", rsi14);
        putIfNotNull(map, "M07_MACD_HISTOGRAM", macdHistogram);
        putIfNotNull(map, "M07_KD_K", kdK);
        putIfNotNull(map, "M07_KD_D", kdD);
        putIfNotNull(map, "M07_MA5", ma5);
        putIfNotNull(map, "M07_MA20", ma20);
        putIfNotNull(map, "M07_MA60", ma60);
        putIfNotNull(map, "M07_BOLLINGER_UPPER", bollingerUpper);
        putIfNotNull(map, "M07_BOLLINGER_LOWER", bollingerLower);
        putIfNotNull(map, "M07_BOLLINGER_MIDDLE", bollingerMiddle);

        // M08
        putIfNotNull(map, "M08_PE_RATIO", peRatio);
        putIfNotNull(map, "M08_PB_RATIO", pbRatio);
        putIfNotNull(map, "M08_ROE", roe);
        putIfNotNull(map, "M08_EPS", eps);
        putIfNotNull(map, "M08_DIVIDEND_YIELD", dividendYield);
        putIfNotNull(map, "M08_REVENUE_GROWTH_YOY", revenueGrowthYoy);
        putIfNotNull(map, "M08_PROFIT_MARGIN", profitMargin);

        // M09
        putIfNotNull(map, "M09_FOREIGN_NET", foreignNet);
        putIfNotNull(map, "M09_FOREIGN_CONTINUOUS_DAYS", foreignContinuousDays);
        putIfNotNull(map, "M09_FOREIGN_ACCUMULATED_20D", foreignAccumulated20d);
        putIfNotNull(map, "M09_TRUST_NET", trustNet);
        putIfNotNull(map, "M09_TRUST_CONTINUOUS_DAYS", trustContinuousDays);
        putIfNotNull(map, "M09_DEALER_NET", dealerNet);
        putIfNotNull(map, "M09_TOTAL_NET", totalNet);
        putIfNotNull(map, "M09_MARGIN_BALANCE", marginBalance);
        putIfNotNull(map, "M09_MARGIN_CHANGE", marginChange);
        putIfNotNull(map, "M09_MARGIN_SHORT_RATIO", marginShortRatio);
        putIfNotNull(map, "M09_CHIP_SCORE", chipScore);

        return map;
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }
}
