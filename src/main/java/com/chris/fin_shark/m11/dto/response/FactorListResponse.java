package com.chris.fin_shark.m11.dto.response;

import com.chris.fin_shark.m11.dto.FactorMetadataDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 因子清單回應 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FactorListResponse {

    @JsonProperty("total_factors")
    private Integer totalFactors;

    private List<FactorCategoryDTO> categories;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FactorCategoryDTO {

        private String category;

        @JsonProperty("category_name")
        private String categoryName;

        @JsonProperty("source_module")
        private String sourceModule;

        @JsonProperty("factor_count")
        private Integer factorCount;

        private List<FactorMetadataDTO> factors;
    }
}
