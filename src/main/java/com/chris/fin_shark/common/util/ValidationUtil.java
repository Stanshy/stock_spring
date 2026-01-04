package com.chris.fin_shark.common.util;

import com.chris.fin_shark.common.constant.ValidationConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * 驗證工具
 *
 * 遵守總綱 4.7 資料品質規範
 *
 * @author chris
 * @since 2025-12-24
 */
@Slf4j
public final class ValidationUtil {

    private ValidationUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 股票代碼正則表達式（編譯快取）
     */
    private static final Pattern STOCK_ID_PATTERN =
            Pattern.compile(ValidationConstants.STOCK_ID_PATTERN);

    // ========================================================================
    // 股票代碼驗證
    // ========================================================================

    /**
     * 驗證股票代碼格式
     *
     * 台股代碼: 4-6 位數字
     *
     * @param stockId 股票代碼
     * @return true 合法，false 不合法
     */
    public static boolean isValidStockId(String stockId) {
        if (stockId == null) {
            return false;
        }
        return STOCK_ID_PATTERN.matcher(stockId).matches();
    }

    // ========================================================================
    // 價格驗證（遵守總綱 4.7 資料品質規範）
    // ========================================================================

    /**
     * 驗證價格是否在合法範圍內
     *
     * @param price 價格
     * @return true 合法，false 不合法
     */
    public static boolean isValidPrice(Double price) {
        if (price == null) {
            return false;
        }
        return price >= ValidationConstants.MIN_PRICE &&
                price <= ValidationConstants.MAX_PRICE;
    }

    /**
     * 驗證股價四價關係
     *
     * 規則: low ≤ open, close ≤ high
     *
     * @param openPrice 開盤價
     * @param highPrice 最高價
     * @param lowPrice 最低價
     * @param closePrice 收盤價
     * @return true 合法，false 不合法
     */
    public static boolean isValidFourPriceRelation(
            Double openPrice, Double highPrice, Double lowPrice, Double closePrice) {

        if (openPrice == null || highPrice == null ||
                lowPrice == null || closePrice == null) {
            return false;
        }

        // low ≤ open ≤ high
        if (lowPrice > openPrice || openPrice > highPrice) {
            return false;
        }

        // low ≤ close ≤ high
        if (lowPrice > closePrice || closePrice > highPrice) {
            return false;
        }

        return true;
    }

    /**
     * 驗證成交金額一致性
     *
     * 規則: amount ≈ close_price × volume
     * 容許誤差: 1%
     *
     * @param closePrice 收盤價
     * @param volume 成交量（張）
     * @param amount 成交金額
     * @return true 合法，false 不合法
     */
    public static boolean isValidAmount(Double closePrice, Long volume, Double amount) {
        if (closePrice == null || volume == null || amount == null) {
            return false;
        }

        // 計算預期成交金額（1 張 = 1000 股）
        double expectedAmount = closePrice * volume * 1000;

        // 計算誤差百分比
        double errorRate = Math.abs(amount - expectedAmount) / expectedAmount;

        return errorRate <= ValidationConstants.AMOUNT_TOLERANCE;
    }

    // ========================================================================
    // 成交量驗證
    // ========================================================================

    /**
     * 驗證成交量是否在合法範圍內
     *
     * @param volume 成交量
     * @return true 合法，false 不合法
     */
    public static boolean isValidVolume(Long volume) {
        if (volume == null) {
            return false;
        }
        return volume >= ValidationConstants.MIN_VOLUME &&
                volume <= ValidationConstants.MAX_VOLUME;
    }

    // ========================================================================
    // 日期驗證
    // ========================================================================

    /**
     * 驗證日期字串格式 (YYYY-MM-DD)
     *
     * @param dateStr 日期字串
     * @return true 合法，false 不合法
     */
    public static boolean isValidDateFormat(String dateStr) {
        if (dateStr == null) {
            return false;
        }
        return DateUtil.parseDate(dateStr) != null;
    }

    // TODO: M08 開發時補充財務指標驗證方法
    // 範例:
    // public static boolean isValidBalanceSheet(...) { ... }
    // public static boolean isValidROE(Double roe) { ... }

    // TODO: 各模組開發時，可以在此補充驗證方法
}
