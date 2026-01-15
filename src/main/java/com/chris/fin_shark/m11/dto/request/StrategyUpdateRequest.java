package com.chris.fin_shark.m11.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 更新策略請求 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyUpdateRequest {

    @Size(max = 100, message = "策略名稱不可超過 100 字元")
    @JsonProperty("strategy_name")
    private String strategyName;

    @Size(max = 500, message = "策略描述不可超過 500 字元")
    private String description;

    private Map<String, Object> conditions;

    private Map<String, Object> parameters;

    private Map<String, Object> output;
}
