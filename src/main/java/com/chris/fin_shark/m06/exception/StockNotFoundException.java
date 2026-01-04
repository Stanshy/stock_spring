package com.chris.fin_shark.m06.exception;

import com.chris.fin_shark.common.exception.DataNotFoundException;
import com.chris.fin_shark.m06.enums.M06ErrorCode;

/**
 * 股票不存在異常
 * <p>
 * 當查詢的股票不存在時拋出此異常
 * HTTP 狀態碼: 404
 * </p>
 *
 * @author Chris
 * @since 1.0.0
 */
public class StockNotFoundException extends DataNotFoundException {

    /**
     * 私有建構子
     *
     * @param stockId 股票代碼
     */
    private StockNotFoundException(String stockId) {
        super(
                M06ErrorCode.M06_STOCK_NOT_FOUND,
                String.format("Stock with ID '%s' not found", stockId),
                "stock_id",
                "Please verify the stock ID and try again"
        );
    }

    /**
     * 工廠方法 - 建立股票不存在異常
     * <p>
     * 使用範例:
     * <pre>
     * Stock stock = stockRepository.findById(stockId)
     *     .orElseThrow(() -> StockNotFoundException.of(stockId));
     * </pre>
     * </p>
     *
     * @param stockId 股票代碼
     * @return 股票不存在異常實例
     */
    public static StockNotFoundException of(String stockId) {
        return new StockNotFoundException(stockId);
    }
}
