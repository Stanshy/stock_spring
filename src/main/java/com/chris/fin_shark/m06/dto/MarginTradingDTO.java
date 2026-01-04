package com.chris.fin_shark.m06.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 融資融券資料傳輸物件
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarginTradingDTO {

    /** 融資融券 ID */
    @JsonProperty("margin_id")
    private Long marginId;

    /** 股票代碼 */
    @JsonProperty("stock_id")
    private String stockId;

    /** 交易日期 */
    @JsonProperty("trade_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate tradeDate;

    /** 融資買進 */
    @JsonProperty("margin_purchase")
    private Long marginPurchase;

    /** 融資賣出 */
    @JsonProperty("margin_sell")
    private Long marginSell;

    /** 融資餘額 */
    @JsonProperty("margin_balance")
    private Long marginBalance;

    /** 融資限額 */
    @JsonProperty("margin_quota")
    private Long marginQuota;

    /** 融資使用率 */
    @JsonProperty("margin_usage_rate")
    private BigDecimal marginUsageRate;

    /** 融券買進 */
    @JsonProperty("short_purchase")
    private Long shortPurchase;

    /** 融券賣出 */
    @JsonProperty("short_sell")
    private Long shortSell;

    /** 融券餘額 */
    @JsonProperty("short_balance")
    private Long shortBalance;

    /** 融券限額 */
    @JsonProperty("short_quota")
    private Long shortQuota;

    /** 融券使用率 */
    @JsonProperty("short_usage_rate")
    private BigDecimal shortUsageRate;
}
