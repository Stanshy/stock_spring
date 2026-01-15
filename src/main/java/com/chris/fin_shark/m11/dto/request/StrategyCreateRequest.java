package com.chris.fin_shark.m11.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 建立策略請求 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyCreateRequest {

    @NotBlank(message = "策略名稱不可為空")
    @Size(max = 100, message = "策略名稱不可超過 100 字元")
    @JsonProperty("strategy_name")
    private String strategyName;

    @NotBlank(message = "策略類型不可為空")
    @JsonProperty("strategy_type")
    private String strategyType;

    @Size(max = 500, message = "策略描述不可超過 500 字元")
    private String description;

    @NotNull(message = "策略條件不可為空")
    private Map<String, Object> conditions;

    private Map<String, Object> parameters;

    private Map<String, Object> output;
}
