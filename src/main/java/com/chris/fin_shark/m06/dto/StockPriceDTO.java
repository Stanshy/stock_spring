package com.chris.fin_shark.m06.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 股價資料傳輸物件
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockPriceDTO {

    /** 股價 ID */
    @JsonProperty("price_id")
    private Long priceId;

    /** 股票代碼 */
    @JsonProperty("stock_id")
    private String stockId;

    /** 交易日期 */
    @JsonProperty("trade_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate tradeDate;

    /** 開盤價 */
    @JsonProperty("open_price")
    private BigDecimal openPrice;

    /** 最高價 */
    @JsonProperty("high_price")
    private BigDecimal highPrice;

    /** 最低價 */
    @JsonProperty("low_price")
    private BigDecimal lowPrice;

    /** 收盤價 */
    @JsonProperty("close_price")
    private BigDecimal closePrice;

    /** 成交量 */
    @JsonProperty("volume")
    private Long volume;

    /** 成交金額 */
    @JsonProperty("turnover")
    private BigDecimal turnover;

    /** 成交筆數 */
    @JsonProperty("transactions")
    private Integer transactions;

    /** 漲跌價差 */
    @JsonProperty("change_price")
    private BigDecimal changePrice;

    /** 漲跌幅（%） */
    @JsonProperty("change_percent")
    private BigDecimal changePercent;

    /** 5 日均價 */
    @JsonProperty("ma5")
    private BigDecimal ma5;

    /** 20 日均價 */
    @JsonProperty("ma20")
    private BigDecimal ma20;

    /** 5 日均量 */
    @JsonProperty("volume_ma5")
    private Long volumeMa5;

    /** 建立時間 */
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 更新時間 */
    @JsonProperty("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
