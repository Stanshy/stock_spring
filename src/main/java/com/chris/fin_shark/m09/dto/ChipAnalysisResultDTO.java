package com.chris.fin_shark.m09.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * 籌碼分析結果 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChipAnalysisResultDTO {

    @JsonProperty("result_id")
    private Long resultId;

    @JsonProperty("stock_id")
    private String stockId;

    @JsonProperty("trade_date")
    private LocalDate tradeDate;

    // ========== 三大法人指標 ==========

    @JsonProperty("foreign_net")
    private Long foreignNet;

    @JsonProperty("foreign_net_ma5")
    private BigDecimal foreignNetMa5;

    @JsonProperty("foreign_net_ma20")
    private BigDecimal foreignNetMa20;

    @JsonProperty("foreign_continuous_days")
    private Integer foreignContinuousDays;

    @JsonProperty("foreign_accumulated_20d")
    private Long foreignAccumulated20d;

    @JsonProperty("trust_net")
    private Long trustNet;

    @JsonProperty("trust_net_ma5")
    private BigDecimal trustNetMa5;

    @JsonProperty("trust_continuous_days")
    private Integer trustContinuousDays;

    @JsonProperty("dealer_net")
    private Long dealerNet;

    @JsonProperty("total_net")
    private Long totalNet;

    // ========== 融資融券指標 ==========

    @JsonProperty("margin_balance")
    private Long marginBalance;

    @JsonProperty("margin_change")
    private Long marginChange;

    @JsonProperty("margin_usage_rate")
    private BigDecimal marginUsageRate;

    @JsonProperty("margin_continuous_days")
    private Integer marginContinuousDays;

    @JsonProperty("short_balance")
    private Long shortBalance;

    @JsonProperty("short_change")
    private Long shortChange;

    @JsonProperty("margin_short_ratio")
    private BigDecimal marginShortRatio;

    // ========== 籌碼集中度 ==========

    @JsonProperty("institutional_ratio")
    private BigDecimal institutionalRatio;

    @JsonProperty("concentration_trend")
    private String concentrationTrend;

    // ========== JSONB 詳細指標 ==========

    @JsonProperty("institutional_indicators")
    private Map<String, Object> institutionalIndicators;

    @JsonProperty("margin_indicators")
    private Map<String, Object> marginIndicators;

    @JsonProperty("concentration_indicators")
    private Map<String, Object> concentrationIndicators;

    // ========== 籌碼評分 ==========

    @JsonProperty("chip_score")
    private Integer chipScore;

    @JsonProperty("chip_grade")
    private String chipGrade;
}
