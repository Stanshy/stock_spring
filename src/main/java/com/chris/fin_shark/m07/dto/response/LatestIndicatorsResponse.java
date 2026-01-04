package com.chris.fin_shark.m07.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * 最新指標回應項目
 * <p>
 * API-M07-003 專用回應格式
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LatestIndicatorsResponse {

    /** 股票代碼 */
    @JsonProperty("stock_id")
    private String stockId;

    /** 股票名稱 */
    @JsonProperty("stock_name")
    private String stockName;

    /** 計算日期 */
    @JsonProperty("calculation_date")
    private LocalDate calculationDate;

    /** 5日均線 */
    @JsonProperty("ma5")
    private BigDecimal ma5;

    /** 20日均線 */
    @JsonProperty("ma20")
    private BigDecimal ma20;

    /** RSI 14 */
    @JsonProperty("rsi_14")
    private BigDecimal rsi14;

    /** MACD */
    @JsonProperty("macd")
    private Map<String, Object> macd;
}

