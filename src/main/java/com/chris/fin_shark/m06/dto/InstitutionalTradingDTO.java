package com.chris.fin_shark.m06.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 三大法人買賣超資料傳輸物件
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionalTradingDTO {

    /** 交易 ID */
    @JsonProperty("trading_id")
    private Long tradingId;

    /** 股票代碼 */
    @JsonProperty("stock_id")
    private String stockId;

    /** 交易日期 */
    @JsonProperty("trade_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate tradeDate;

    /** 外資買進 */
    @JsonProperty("foreign_buy")
    private Long foreignBuy;

    /** 外資賣出 */
    @JsonProperty("foreign_sell")
    private Long foreignSell;

    /** 外資買賣超 */
    @JsonProperty("foreign_net")
    private Long foreignNet;

    /** 投信買進 */
    @JsonProperty("trust_buy")
    private Long trustBuy;

    /** 投信賣出 */
    @JsonProperty("trust_sell")
    private Long trustSell;

    /** 投信買賣超 */
    @JsonProperty("trust_net")
    private Long trustNet;

    /** 自營商買進 */
    @JsonProperty("dealer_buy")
    private Long dealerBuy;

    /** 自營商賣出 */
    @JsonProperty("dealer_sell")
    private Long dealerSell;

    /** 自營商買賣超 */
    @JsonProperty("dealer_net")
    private Long dealerNet;

    /** 三大法人合計買賣超 */
    @JsonProperty("total_net")
    private Long totalNet;
}
