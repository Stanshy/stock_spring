package com.chris.fin_shark.m11.dto;

import com.chris.fin_shark.common.enums.SignalType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 策略信號 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StrategySignalDTO {

    @JsonProperty("signal_id")
    private String signalId;

    @JsonProperty("execution_id")
    private String executionId;

    @JsonProperty("strategy_id")
    private String strategyId;

    @JsonProperty("strategy_name")
    private String strategyName;

    @JsonProperty("strategy_version")
    private Integer strategyVersion;

    @JsonProperty("stock_id")
    private String stockId;

    @JsonProperty("stock_name")
    private String stockName;

    @JsonProperty("trade_date")
    private LocalDate tradeDate;

    @JsonProperty("signal_type")
    private SignalType signalType;

    @JsonProperty("confidence_score")
    private BigDecimal confidenceScore;

    @JsonProperty("close_price")
    private BigDecimal closePrice;

    @JsonProperty("matched_conditions")
    private List<MatchedConditionDTO> matchedConditions;

    @JsonProperty("factor_values")
    private Map<String, Object> factorValues;

    @JsonProperty("factor_summary")
    private Map<String, Object> factorSummary;

    /**
     * 匹配條件詳情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MatchedConditionDTO {

        @JsonProperty("factor_id")
        private String factorId;

        @JsonProperty("factor_value")
        private Object factorValue;

        private String condition;

        private String operator;

        private Object threshold;

        private Boolean matched;
    }
}
