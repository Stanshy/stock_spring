package com.chris.fin_shark.m11.dto;

import com.chris.fin_shark.m11.enums.FactorCategory;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 因子元數據 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FactorMetadataDTO {

    @JsonProperty("factor_id")
    private String factorId;

    @JsonProperty("factor_name")
    private String factorName;

    @JsonProperty("display_name")
    private String displayName;

    private FactorCategory category;

    @JsonProperty("source_module")
    private String sourceModule;

    @JsonProperty("data_type")
    private String dataType;

    @JsonProperty("value_range")
    private ValueRangeDTO valueRange;

    @JsonProperty("typical_thresholds")
    private List<Object> typicalThresholds;

    @JsonProperty("default_operator")
    private String defaultOperator;

    @JsonProperty("supported_operators")
    private List<String> supportedOperators;

    private String description;

    @JsonProperty("calculation_formula")
    private String calculationFormula;

    @JsonProperty("update_frequency")
    private String updateFrequency;

    @JsonProperty("example_conditions")
    private List<ExampleConditionDTO> exampleConditions;

    @JsonProperty("related_factors")
    private List<String> relatedFactors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValueRangeDTO {
        private Object min;
        private Object max;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExampleConditionDTO {
        private String description;
        private Map<String, Object> condition;
    }
}
