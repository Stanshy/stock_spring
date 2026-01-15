package com.chris.fin_shark.m11.dto;

import com.chris.fin_shark.m11.enums.ExecutionStatus;
import com.chris.fin_shark.m11.enums.ExecutionType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 策略執行記錄 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StrategyExecutionDTO {

    @JsonProperty("execution_id")
    private String executionId;

    @JsonProperty("strategy_id")
    private String strategyId;

    @JsonProperty("strategy_version")
    private Integer strategyVersion;

    @JsonProperty("execution_date")
    private LocalDate executionDate;

    @JsonProperty("execution_type")
    private ExecutionType executionType;

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
    private Integer executionTimeMs;

    private ExecutionStatus status;

    @JsonProperty("executed_at")
    private LocalDateTime executedAt;

    private Map<String, Object> diagnostics;
}
