package com.chris.fin_shark.m06.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * TWSE 股價資料傳輸物件
 * <p>
 * 用於封裝從 TWSE API 解析後的股價資料
 * 此物件對應單一交易日的股價資訊
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwseStockPriceData {

    /**
     * 交易日期
     */
    private LocalDate tradeDate;

    /**
     * 成交股數
     */
    private Long volume;

    /**
     * 成交金額（元）
     */
    private BigDecimal turnover;

    /**
     * 開盤價（元）
     */
    private BigDecimal openPrice;

    /**
     * 最高價（元）
     */
    private BigDecimal highPrice;

    /**
     * 最低價（元）
     */
    private BigDecimal lowPrice;

    /**
     * 收盤價（元）
     */
    private BigDecimal closePrice;

    /**
     * 漲跌價差（元）
     */
    private BigDecimal changePrice;

    /**
     * 成交筆數
     */
    private Integer transactions;
}
