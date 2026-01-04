package com.chris.fin_shark.m07.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 交叉信號回應
 * <p>
 * API-M07-004 專用回應格式
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrossSignalsResponse {

    /** 交叉日期 */
    @JsonProperty("cross_date")
    private LocalDate crossDate;

    /** 信號列表 */
    @JsonProperty("signals")
    private List<CrossSignal> signals;

    /** 總筆數 */
    @JsonProperty("total_count")
    private Integer totalCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CrossSignal {
        /** 股票代碼 */
        @JsonProperty("stock_id")
        private String stockId;

        /** 股票名稱 */
        @JsonProperty("stock_name")
        private String stockName;

        /** 交叉類型 */
        @JsonProperty("cross_type")
        private String crossType;

        /** 指標 */
        @JsonProperty("indicator")
        private String indicator;

        /** 短週期 */
        @JsonProperty("short_period")
        private Integer shortPeriod;

        /** 長週期 */
        @JsonProperty("long_period")
        private Integer longPeriod;

        /** 短週期數值 */
        @JsonProperty("short_value")
        private BigDecimal shortValue;

        /** 長週期數值 */
        @JsonProperty("long_value")
        private BigDecimal longValue;

        /** 前一日短週期數值 */
        @JsonProperty("previous_short")
        private BigDecimal previousShort;

        /** 前一日長週期數值 */
        @JsonProperty("previous_long")
        private BigDecimal previousLong;

        /** 信號強度 */
        @JsonProperty("signal_strength")
        private String signalStrength;

        /** 信心分數 */
        @JsonProperty("confidence_score")
        private Integer confidenceScore;
    }
}

