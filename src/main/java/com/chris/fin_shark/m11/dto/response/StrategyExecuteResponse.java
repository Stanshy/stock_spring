package com.chris.fin_shark.m11.dto.response;

import com.chris.fin_shark.m11.dto.StrategySignalDTO;
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
 * 策略執行回應 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StrategyExecuteResponse {

    @JsonProperty("execution_id")
    private String executionId;

    @JsonProperty("strategy_id")
    private String strategyId;

    @JsonProperty("strategy_name")
    private String strategyName;

    @JsonProperty("execution_date")
    private LocalDate executionDate;

    @JsonProperty("execution_summary")
    private ExecutionSummaryDTO executionSummary;

    private List<StrategySignalDTO> signals;

    private DiagnosticsDTO diagnostics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionSummaryDTO {

        @JsonProperty("stocks_evaluated")
        private Integer stocksEvaluated;

        @JsonProperty("signals_generated")
        private Integer signalsGenerated;

        @JsonProperty("buy_signals")
        private Integer buySignals;

        @JsonProperty("sell_signals")
        private Integer sellSignals;

        @JsonProperty("avg_confidence")
        private BigDecimal avgConfidence;

        @JsonProperty("execution_time_ms")
        private Long executionTimeMs;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiagnosticsDTO {

        @JsonProperty("factors_loaded")
        private Integer factorsLoaded;

        @JsonProperty("factors_missing")
        private Integer factorsMissing;

        @JsonProperty("calculation_errors")
        private Integer calculationErrors;

        private List<String> warnings;
    }
}
