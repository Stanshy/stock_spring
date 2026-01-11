package com.chris.fin_shark.common.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * TWSE 三大法人買賣超資料傳輸物件
 * <p>
 * 用於封裝從 TWSE T86 API 解析後的法人買賣資料
 * 此物件對應單一股票單一交易日的法人買賣資訊
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwseInstitutionalData {

    /**
     * 股票代碼
     */
    private String stockId;

    /**
     * 交易日期
     */
    private LocalDate tradeDate;

    /**
     * 外資買進股數
     */
    private Long foreignBuy;

    /**
     * 外資賣出股數
     */
    private Long foreignSell;

    /**
     * 投信買進股數
     */
    private Long trustBuy;

    /**
     * 投信賣出股數
     */
    private Long trustSell;

    /**
     * 自營商買進股數（自行買賣 + 避險）
     */
    private Long dealerBuy;

    /**
     * 自營商賣出股數（自行買賣 + 避險）
     */
    private Long dealerSell;
}
