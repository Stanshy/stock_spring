package com.chris.fin_shark.m11.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 策略統計 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyStatisticsDTO {

    @JsonProperty("total_executions")
    private Integer totalExecutions;

    @JsonProperty("total_signals")
    private Integer totalSignals;

    @JsonProperty("avg_signals_per_execution")
    private BigDecimal avgSignalsPerExecution;

    @JsonProperty("avg_confidence")
    private BigDecimal avgConfidence;
}
