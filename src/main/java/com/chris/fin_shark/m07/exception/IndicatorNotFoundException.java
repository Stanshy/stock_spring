package com.chris.fin_shark.m07.exception;

import com.chris.fin_shark.common.exception.DataNotFoundException;
import com.chris.fin_shark.m07.enums.M07ErrorCode;

import java.time.LocalDate;

/**
 * 指標不存在異常
 * <p>
 * 當查詢的技術指標不存在時拋出此異常
 * HTTP 狀態碼: 404
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
public class IndicatorNotFoundException extends DataNotFoundException {

    /**
     * 私有建構子
     *
     * @param message 錯誤訊息
     */
    private IndicatorNotFoundException(String message) {
        super(
                M07ErrorCode.M07_INDICATOR_NOT_FOUND,
                message,
                "indicator",
                "Please check the stock ID and calculation date"
        );
    }

    /**
     * 工廠方法 - 根據股票代碼和日期建立異常
     *
     * @param stockId         股票代碼
     * @param calculationDate 計算日期
     * @return 異常實例
     */
    public static IndicatorNotFoundException of(String stockId, LocalDate calculationDate) {
        return new IndicatorNotFoundException(
                String.format("Indicator not found for stock '%s' on date '%s'",
                        stockId, calculationDate)
        );
    }

    /**
     * 工廠方法 - 根據股票代碼建立異常
     *
     * @param stockId 股票代碼
     * @return 異常實例
     */
    public static IndicatorNotFoundException ofStock(String stockId) {
        return new IndicatorNotFoundException(
                String.format("No indicators found for stock '%s'", stockId)
        );
    }

    /**
     * 工廠方法 - 自訂訊息
     *
     * @param message 錯誤訊息
     * @return 異常實例
     */
    public static IndicatorNotFoundException of(String message) {
        return new IndicatorNotFoundException(message);
    }
}
