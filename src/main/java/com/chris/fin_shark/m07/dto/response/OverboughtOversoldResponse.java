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
 * 超買超賣信號回應
 * <p>
 * API-M07-005 專用回應格式
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OverboughtOversoldResponse {

    /** 信號日期 */
    @JsonProperty("signal_date")
    private LocalDate signalDate;

    /** 信號列表 */
    @JsonProperty("signals")
    private List<OverboughtOversoldSignal> signals;

    /** 總筆數 */
    @JsonProperty("total_count")
    private Integer totalCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverboughtOversoldSignal {
        /** 股票代碼 */
        @JsonProperty("stock_id")
        private String stockId;

        /** 股票名稱 */
        @JsonProperty("stock_name")
        private String stockName;

        /** 信號類型 */
        @JsonProperty("signal_type")
        private String signalType;

        /** 指標 */
        @JsonProperty("indicator")
        private String indicator;

        /** 指標數值 */
        @JsonProperty("indicator_value")
        private BigDecimal indicatorValue;

        /** 閾值 */
        @JsonProperty("threshold")
        private BigDecimal threshold;

        /** 持續天數 */
        @JsonProperty("duration_days")
        private Integer durationDays;

        /** 信號強度 */
        @JsonProperty("signal_strength")
        private String signalStrength;

        /** 信心分數 */
        @JsonProperty("confidence_score")
        private Integer confidenceScore;
    }
}

