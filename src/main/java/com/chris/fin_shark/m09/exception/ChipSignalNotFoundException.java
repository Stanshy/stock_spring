package com.chris.fin_shark.m09.exception;

import com.chris.fin_shark.common.exception.DataNotFoundException;
import com.chris.fin_shark.m09.enums.M09ErrorCode;

import java.time.LocalDate;

/**
 * 籌碼訊號不存在異常
 * <p>
 * 當查詢的籌碼異常訊號不存在時拋出此異常
 * HTTP 狀態碼: 404
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
public class ChipSignalNotFoundException extends DataNotFoundException {

    /**
     * 私有建構子
     *
     * @param message 錯誤訊息
     */
    private ChipSignalNotFoundException(String message) {
        super(
                M09ErrorCode.M09_SIGNAL_NOT_FOUND,
                message,
                "chip_signal",
                "Please check the signal ID or query parameters"
        );
    }

    /**
     * 工廠方法 - 根據訊號 ID 建立異常
     *
     * @param signalId 訊號 ID
     * @return 異常實例
     */
    public static ChipSignalNotFoundException ofId(Long signalId) {
        return new ChipSignalNotFoundException(
                String.format("Chip signal with ID '%d' not found", signalId)
        );
    }

    /**
     * 工廠方法 - 根據股票代碼和日期建立異常
     *
     * @param stockId   股票代碼
     * @param tradeDate 交易日期
     * @return 異常實例
     */
    public static ChipSignalNotFoundException of(String stockId, LocalDate tradeDate) {
        return new ChipSignalNotFoundException(
                String.format("No chip signals found for stock '%s' on date '%s'",
                        stockId, tradeDate)
        );
    }

    /**
     * 工廠方法 - 自訂訊息
     *
     * @param message 錯誤訊息
     * @return 異常實例
     */
    public static ChipSignalNotFoundException of(String message) {
        return new ChipSignalNotFoundException(message);
    }
}
