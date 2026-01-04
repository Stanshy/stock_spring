package com.chris.fin_shark.common.constant;

/**
 * 快取相關常量
 *
 * 定義 Redis 快取的 Key 前綴和 TTL
 *
 * @author chris
 * @since 2025-12-24
 */
public final class CacheConstants {

    private CacheConstants() {
        throw new UnsupportedOperationException("Constant class cannot be instantiated");
    }

    // ========================================================================
    // 快取 Key 前綴（已定義基礎結構，需模組開發時補充具體業務）
    // ========================================================================

    /**
     * 快取 Key 分隔符
     */
    public static final String KEY_SEPARATOR = ":";

    /**
     * 股票資訊快取 Key 前綴
     * 格式: stock:info:{stock_id}
     * 範例: stock:info:2330
     */
    public static final String STOCK_INFO_PREFIX = "stock:info";

    /**
     * 股價快取 Key 前綴
     * 格式: stock:prices:{stock_id}:{start_date}:{end_date}
     * 範例: stock:prices:2330:2024-01-01:2024-12-31
     */
    public static final String STOCK_PRICES_PREFIX = "stock:prices";

    /**
     * 單日股價快取 Key 前綴
     * 格式: stock:price:{stock_id}:{trade_date}
     * 範例: stock:price:2330:2024-12-24
     */
    public static final String STOCK_PRICE_SINGLE_PREFIX = "stock:price";

    /**
     * 財報資料快取 Key 前綴
     * 格式: stock:financial:{stock_id}:{year}:{quarter}
     * 範例: stock:financial:2330:2024:Q3
     */
    public static final String FINANCIAL_DATA_PREFIX = "stock:financial";

    /**
     * 活躍股票清單快取 Key
     * 格式: stocks:list:active
     */
    public static final String ACTIVE_STOCKS_LIST = "stocks:list:active";

    /**
     * 交易日曆快取 Key 前綴
     * 格式: trading:calendar:{year}
     * 範例: trading:calendar:2024
     */
    public static final String TRADING_CALENDAR_PREFIX = "trading:calendar";

    // TODO: M07 開發時補充技術指標快取 Key
    // 範例:
    // public static final String INDICATOR_PREFIX = "indicator";
    // 格式: indicator:{indicator_name}:{stock_id}:{date}

    // TODO: M13 開發時補充信號快取 Key
    // 範例:
    // public static final String SIGNAL_PREFIX = "signal";
    // 格式: signal:{signal_type}:{stock_id}:{date}


    // ========================================================================
    // TTL (Time To Live) - 快取過期時間（秒）
    // ========================================================================

    /**
     * 熱門股票資訊 TTL（1 小時）
     * 適用: 市值前 50 股票
     */
    public static final long HOT_STOCK_INFO_TTL = 60 * 60;  // 3600 秒

    /**
     * 一般股票資訊 TTL（6 小時）
     */
    public static final long STOCK_INFO_TTL = 6 * 60 * 60;  // 21600 秒

    /**
     * 股價資料 TTL（1 小時）
     * 盤中快取，收盤後可延長
     */
    public static final long STOCK_PRICE_TTL = 60 * 60;  // 3600 秒

    /**
     * 財報資料 TTL（24 小時）
     * 財報資料更新頻率低
     */
    public static final long FINANCIAL_DATA_TTL = 24 * 60 * 60;  // 86400 秒

    /**
     * 股票清單 TTL（24 小時）
     */
    public static final long STOCK_LIST_TTL = 24 * 60 * 60;  // 86400 秒

    /**
     * 交易日曆 TTL（7 天）
     */
    public static final long TRADING_CALENDAR_TTL = 7 * 24 * 60 * 60;  // 604800 秒

    /**
     * 短期快取 TTL（5 分鐘）
     * 適用: 頻繁變動的資料
     */
    public static final long SHORT_TERM_TTL = 5 * 60;  // 300 秒

    /**
     * 長期快取 TTL（7 天）
     * 適用: 很少變動的資料
     */
    public static final long LONG_TERM_TTL = 7 * 24 * 60 * 60;  // 604800 秒

    // TODO: 各模組開發時，可以在此補充特定快取的 TTL
    // 範例:
    // public static final long INDICATOR_TTL = 2 * 60 * 60;  // 技術指標 2 小時
    // public static final long SIGNAL_TTL = 60 * 60;  // 信號 1 小時


    // ========================================================================
    // 快取配置
    // ========================================================================

    /**
     * 預設快取最大容量（筆數）
     */
    public static final long DEFAULT_MAX_SIZE = 10000;

    /**
     * LRU 淘汰閾值（百分比）
     * 當使用率超過此值時，開始 LRU 淘汰
     */
    public static final int LRU_EVICTION_THRESHOLD = 80;


    // ========================================================================
    // 輔助方法
    // ========================================================================

    /**
     * 建立快取 Key
     *
     * @param parts Key 的各部分
     * @return 完整的快取 Key
     */
    public static String buildKey(String... parts) {
        return String.join(KEY_SEPARATOR, parts);
    }

    /**
     * 建立股票資訊快取 Key
     *
     * @param stockId 股票代碼
     * @return 快取 Key
     */
    public static String stockInfoKey(String stockId) {
        return buildKey(STOCK_INFO_PREFIX, stockId);
    }

    /**
     * 建立股價範圍查詢快取 Key
     *
     * @param stockId 股票代碼
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return 快取 Key
     */
    public static String stockPricesKey(String stockId, String startDate, String endDate) {
        return buildKey(STOCK_PRICES_PREFIX, stockId, startDate, endDate);
    }

    /**
     * 建立單日股價快取 Key
     *
     * @param stockId 股票代碼
     * @param tradeDate 交易日期
     * @return 快取 Key
     */
    public static String stockPriceKey(String stockId, String tradeDate) {
        return buildKey(STOCK_PRICE_SINGLE_PREFIX, stockId, tradeDate);
    }

    /**
     * 建立財報資料快取 Key
     *
     * @param stockId 股票代碼
     * @param year 年份
     * @param quarter 季度
     * @return 快取 Key
     */
    public static String financialDataKey(String stockId, String year, String quarter) {
        return buildKey(FINANCIAL_DATA_PREFIX, stockId, year, quarter);
    }

    /**
     * 建立交易日曆快取 Key
     *
     * @param year 年份
     * @return 快取 Key
     */
    public static String tradingCalendarKey(String year) {
        return buildKey(TRADING_CALENDAR_PREFIX, year);
    }

    // TODO: 各模組開發時，可以在此補充快取 Key 建構方法
}
