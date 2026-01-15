package com.chris.fin_shark.m11.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * 執行策略請求 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyExecuteRequest {

    @JsonProperty("execution_date")
    private LocalDate executionDate;

    @JsonProperty("stock_universe")
    private StockUniverseDTO stockUniverse;

    private ExecutionOptionsDTO options;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockUniverseDTO {

        /**
         * 範圍類型: MARKET, WATCHLIST, STOCKS, INDUSTRY
         */
        private String type;

        @JsonProperty("market_type")
        private String marketType;

        @JsonProperty("min_volume")
        private Integer minVolume;

        @JsonProperty("exclude_etf")
        private Boolean excludeEtf;

        @JsonProperty("stock_ids")
        private java.util.List<String> stockIds;

        private java.util.List<String> industries;

        @JsonProperty("watchlist_id")
        private String watchlistId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionOptionsDTO {

        @JsonProperty("include_factor_values")
        @Builder.Default
        private Boolean includeFactorValues = true;

        @JsonProperty("include_diagnostics")
        @Builder.Default
        private Boolean includeDiagnostics = true;

        @JsonProperty("save_results")
        @Builder.Default
        private Boolean saveResults = true;
    }
}
