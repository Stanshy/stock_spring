package com.chris.fin_shark.m08.exception;

import com.chris.fin_shark.common.exception.DataNotFoundException;
import com.chris.fin_shark.m08.enums.M08ErrorCode;

/**
 * 財務指標不存在異常
 *
 * @author chris
 * @since 1.0.0
 */
public class FundamentalIndicatorNotFoundException extends DataNotFoundException {

    private FundamentalIndicatorNotFoundException(String message, String field, String suggestion) {
        super(M08ErrorCode.M08_INDICATOR_NOT_FOUND, message, field, suggestion);
    }

    /**
     * 工廠方法 - 指定季度不存在
     */
    public static FundamentalIndicatorNotFoundException of(String stockId, Integer year, Integer quarter) {
        return new FundamentalIndicatorNotFoundException(
                String.format("Financial indicators not found: stockId=%s, year=%d, quarter=%d",
                        stockId, year, quarter),
                "stock_id, year, quarter",
                "Please check if the stock ID, year and quarter are correct, or wait for data calculation"
        );
    }

    /**
     * 工廠方法 - 最新季度不存在
     */
    public static FundamentalIndicatorNotFoundException ofLatest(String stockId) {
        return new FundamentalIndicatorNotFoundException(
                String.format("No financial indicators found for stock: %s", stockId),
                "stock_id",
                "Please wait for data calculation or manually trigger the calculation job"
        );
    }
}
