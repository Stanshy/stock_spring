package com.chris.fin_shark.common.constant;

/**
 * 驗證規則常量
 *
 * 遵守總綱 4.7 資料品質規範
 *
 * @author chris
 * @since 2025-12-24
 */
public final class ValidationConstants {

    private ValidationConstants() {
        throw new UnsupportedOperationException("Constant class cannot be instantiated");
    }

    // ========================================================================
    // 股票代碼驗證
    // ========================================================================

    /**
     * 股票代碼正則表達式
     * 台股代碼: 4-6 位數字
     */
    public static final String STOCK_ID_PATTERN = "^[0-9]{4,6}$";

    /**
     * 股票代碼最小長度
     */
    public static final int STOCK_ID_MIN_LENGTH = 4;

    /**
     * 股票代碼最大長度
     */
    public static final int STOCK_ID_MAX_LENGTH = 6;


    // ========================================================================
    // 價格驗證（遵守總綱 4.7 資料品質規範）
    // ========================================================================

    /**
     * 最小價格（不可為負數）
     */
    public static final double MIN_PRICE = 0.0;

    /**
     * 最大價格（理論上限，防止異常資料）
     */
    public static final double MAX_PRICE = 999999.99;

    /**
     * 價格變動上限（台股漲跌停幅度 10%）
     */
    public static final double PRICE_CHANGE_LIMIT = 0.10;  // 10%

    /**
     * 成交金額容許誤差（1%）
     * 用於驗證: 成交金額 = close_price × volume
     */
    public static final double AMOUNT_TOLERANCE = 0.01;  // 1%


    // ========================================================================
    // 成交量驗證
    // ========================================================================

    /**
     * 最小成交量（不可為負數）
     */
    public static final long MIN_VOLUME = 0L;

    /**
     * 最大成交量（理論上限，防止異常資料）
     */
    public static final long MAX_VOLUME = 999999999999L;  // 9999 億張


    // ========================================================================
    // 財務指標驗證
    // ========================================================================

    /**
     * 資產負債平衡容許誤差（1%）
     * 用於驗證: total_assets = total_liabilities + equity
     */
    public static final double BALANCE_SHEET_TOLERANCE = 0.01;  // 1%

    /**
     * ROE 合理範圍上限（%）
     */
    public static final double MAX_ROE = 200.0;  // 200%

    /**
     * ROE 合理範圍下限（%）
     */
    public static final double MIN_ROE = -100.0;  // -100%

    // TODO: M08 開發時補充更多財務指標驗證規則


    // ========================================================================
    // 日期驗證
    // ========================================================================

    /**
     * 歷史資料最早日期
     * 台股開始電子化交易的時間
     */
    public static final String MIN_TRADE_DATE = "1990-01-01";

    /**
     * 資料保留最大天數
     * 超過此天數的資料可能被歸檔
     */
    public static final int MAX_DATA_RETENTION_DAYS = 7300;  // 20 年


    // ========================================================================
    // 字串長度限制
    // ========================================================================

    /**
     * 股票名稱最大長度
     */
    public static final int STOCK_NAME_MAX_LENGTH = 50;

    /**
     * 產業名稱最大長度
     */
    public static final int INDUSTRY_MAX_LENGTH = 50;

    /**
     * 錯誤訊息最大長度
     */
    public static final int ERROR_MESSAGE_MAX_LENGTH = 500;

    /**
     * 註解最大長度
     */
    public static final int COMMENT_MAX_LENGTH = 1000;


    // ========================================================================
    // 驗證訊息模板
    // ========================================================================

    /**
     * 必填欄位錯誤訊息模板
     */
    public static final String MSG_REQUIRED = "%s is required";

    /**
     * 格式錯誤訊息模板
     */
    public static final String MSG_INVALID_FORMAT = "%s format is invalid";

    /**
     * 範圍錯誤訊息模板
     */
    public static final String MSG_OUT_OF_RANGE = "%s is out of range [%s, %s]";

    /**
     * 長度錯誤訊息模板
     */
    public static final String MSG_INVALID_LENGTH = "%s length must be between %d and %d";

    // TODO: 各模組開發時，可以在此補充驗證規則常量
}
