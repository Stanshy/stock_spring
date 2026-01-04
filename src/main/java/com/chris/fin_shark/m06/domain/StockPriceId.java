package com.chris.fin_shark.m06.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * 股價複合主鍵
 * <p>
 * 用於 StockPrice 實體的複合主鍵
 * 必須實作 Serializable 介面並重寫 equals 和 hashCode
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockPriceId implements Serializable {

    /** 股票 ID */
    private String stockId;

    /** 交易日期（分區鍵） */
    private LocalDate tradeDate;

    /**
     * 重寫 equals 方法（複合主鍵必須）
     *
     * @param o 比較物件
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockPriceId that = (StockPriceId) o;
        return stockId.equals(that.stockId) && tradeDate.equals(that.tradeDate);
    }

    /**
     * 重寫 hashCode 方法（複合主鍵必須）
     *
     * @return hash 值
     */
    @Override
    public int hashCode() {
        return 31 * stockId.hashCode() + tradeDate.hashCode();
    }
}
