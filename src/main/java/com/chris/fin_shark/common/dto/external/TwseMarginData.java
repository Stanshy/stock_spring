package com.chris.fin_shark.common.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * TWSE 融資融券資料傳輸物件
 * <p>
 * 用於封裝從 TWSE MI_MARGN API 解析後的融資融券資料
 * 此物件對應單一股票單一交易日的融資融券資訊
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TwseMarginData {

    /**
     * 股票代碼
     */
    private String stockId;

    /**
     * 交易日期
     */
    private LocalDate tradeDate;

    /**
     * 融資買進（張）
     */
    private Long marginPurchase;

    /**
     * 融資賣出（張）
     */
    private Long marginSell;

    /**
     * 融資現金償還（張）
     */
    private Long marginCashRepayment;

    /**
     * 融資今日餘額（張）
     */
    private Long marginBalance;

    /**
     * 融資限額（張）
     */
    private Long marginQuota;

    /**
     * 融券買進（張）
     */
    private Long shortPurchase;

    /**
     * 融券賣出（張）
     */
    private Long shortSell;

    /**
     * 融券現券償還（張）
     */
    private Long shortCashRepayment;

    /**
     * 融券今日餘額（張）
     */
    private Long shortBalance;

    /**
     * 融券限額（張）
     */
    private Long shortQuota;
}
