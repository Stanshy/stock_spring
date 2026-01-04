package com.chris.fin_shark.m07.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 特定指標回應
 * <p>
 * API-M07-002 專用回應格式
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecificIndicatorResponse {

    /** 股票代碼 */
    @JsonProperty("stock_id")
    private String stockId;

    /** 指標名稱 */
    @JsonProperty("indicator_name")
    private String indicatorName;

    /** 指標參數 */
    @JsonProperty("indicator_params")
    private Map<String, Object> indicatorParams;

    /** 數值列表 */
    @JsonProperty("values")
    private List<IndicatorValue> values;

    /** 總筆數 */
    @JsonProperty("total_count")
    private Integer totalCount;

    /** 統計資訊 */
    @JsonProperty("statistics")
    private IndicatorStatistics statistics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndicatorValue {
        @JsonProperty("date")
        private String date;

        @JsonProperty("value")
        private Object value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IndicatorStatistics {
        @JsonProperty("max")
        private Double max;

        @JsonProperty("min")
        private Double min;

        @JsonProperty("avg")
        private Double avg;

        @JsonProperty("current")
        private Double current;

        @JsonProperty("previous")
        private Double previous;

        @JsonProperty("change")
        private Double change;
    }
}

