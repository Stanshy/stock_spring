package com.chris.fin_shark.m11.dto.response;

import com.chris.fin_shark.m11.enums.StrategyType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 預設策略庫回應 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PresetStrategyResponse {

    @JsonProperty("preset_strategies")
    private List<PresetStrategyDTO> presetStrategies;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PresetStrategyDTO {

        @JsonProperty("strategy_id")
        private String strategyId;

        @JsonProperty("strategy_name")
        private String strategyName;

        @JsonProperty("strategy_type")
        private StrategyType strategyType;

        private String description;

        private String difficulty;

        @JsonProperty("avg_signals_per_day")
        private Integer avgSignalsPerDay;

        @JsonProperty("backtest_performance")
        private BacktestPerformanceDTO backtestPerformance;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BacktestPerformanceDTO {

        @JsonProperty("annual_return")
        private BigDecimal annualReturn;

        @JsonProperty("sharpe_ratio")
        private BigDecimal sharpeRatio;

        @JsonProperty("max_drawdown")
        private BigDecimal maxDrawdown;

        @JsonProperty("win_rate")
        private BigDecimal winRate;
    }
}
