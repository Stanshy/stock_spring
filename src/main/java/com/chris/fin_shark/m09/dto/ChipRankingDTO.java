package com.chris.fin_shark.m09.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 籌碼排行榜 DTO
 * <p>
 * 用於排行榜查詢結果的資料傳輸物件。
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChipRankingDTO {

    /**
     * 排名
     */
    private Integer rank;

    /**
     * 股票代碼
     */
    private String stockId;

    /**
     * 股票名稱
     */
    private String stockName;

    /**
     * 市場類型
     */
    private String marketType;

    /**
     * 產業別
     */
    private String industry;

    /**
     * 排序值（買賣超量、連續天數等）
     */
    private Long value;

    /**
     * 收盤價
     */
    private BigDecimal closePrice;

    /**
     * 成交量
     */
    private Long volume;

    /**
     * 漲跌幅（%）
     */
    private BigDecimal changePercent;
}
