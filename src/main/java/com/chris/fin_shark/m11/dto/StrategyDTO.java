package com.chris.fin_shark.m11.dto;

import com.chris.fin_shark.m11.enums.StrategyStatus;
import com.chris.fin_shark.m11.enums.StrategyType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 策略 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StrategyDTO {

    @JsonProperty("strategy_id")
    private String strategyId;

    @JsonProperty("strategy_name")
    private String strategyName;

    @JsonProperty("strategy_type")
    private StrategyType strategyType;

    private String description;

    private Integer version;

    private StrategyStatus status;

    @JsonProperty("is_preset")
    private Boolean isPreset;

    @JsonProperty("condition_count")
    private Integer conditionCount;

    private Map<String, Object> conditions;

    private Map<String, Object> parameters;

    @JsonProperty("output")
    private Map<String, Object> outputConfig;

    private StrategyStatisticsDTO statistics;

    @JsonProperty("last_execution")
    private LocalDateTime lastExecution;

    @JsonProperty("total_signals_today")
    private Integer totalSignalsToday;

    @JsonProperty("created_by")
    private String createdBy;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
