package com.chris.fin_shark.m06.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 股價統計 VO
 * <p>
 * 用於 MyBatis 複雜查詢結果映射
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockPriceStatisticsVO {

    /** 股票代碼 */
    private String stockId;

    /** 交易日期 */
    private LocalDate tradeDate;

    /** 收盤價 */
    private BigDecimal closePrice;

    /** 成交量 */
    private Long volume;

    /** 漲跌幅 */
    private BigDecimal changePercent;

    /** 5 日均價 */
    private BigDecimal ma5;

    /** 20 日均價 */
    private BigDecimal ma20;

    /** 5 日均量 */
    private Long volumeMa5;
}
