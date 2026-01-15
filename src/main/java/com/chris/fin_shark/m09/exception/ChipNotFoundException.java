package com.chris.fin_shark.m09.exception;

import com.chris.fin_shark.common.exception.DataNotFoundException;
import com.chris.fin_shark.m09.enums.M09ErrorCode;

import java.time.LocalDate;

/**
 * 籌碼資料不存在異常
 * <p>
 * 當查詢的籌碼分析資料不存在時拋出此異常
 * HTTP 狀態碼: 404
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
public class ChipNotFoundException extends DataNotFoundException {

    /**
     * 私有建構子
     *
     * @param message 錯誤訊息
     */
    private ChipNotFoundException(String message) {
        super(
                M09ErrorCode.M09_CHIP_DATA_NOT_FOUND,
                message,
                "chip_analysis",
                "Please check the stock ID and trade date"
        );
    }

    /**
     * 工廠方法 - 根據股票代碼和日期建立異常
     *
     * @param stockId   股票代碼
     * @param tradeDate 交易日期
     * @return 異常實例
     */
    public static ChipNotFoundException of(String stockId, LocalDate tradeDate) {
        return new ChipNotFoundException(
                String.format("Chip analysis data not found for stock '%s' on date '%s'",
                        stockId, tradeDate)
        );
    }

    /**
     * 工廠方法 - 根據股票代碼建立異常
     *
     * @param stockId 股票代碼
     * @return 異常實例
     */
    public static ChipNotFoundException ofStock(String stockId) {
        return new ChipNotFoundException(
                String.format("No chip analysis data found for stock '%s'", stockId)
        );
    }

    /**
     * 工廠方法 - 自訂訊息
     *
     * @param message 錯誤訊息
     * @return 異常實例
     */
    public static ChipNotFoundException of(String message) {
        return new ChipNotFoundException(message);
    }
}
