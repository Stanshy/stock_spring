package com.chris.fin_shark.common.constant;

/**
 * Job 相關常量
 *
 * 遵守總綱 4.5 Job/排程模型規範
 *
 * @author chris
 * @since 2025-12-24
 */
public final class JobConstants {

    private JobConstants() {
        throw new UnsupportedOperationException("Constant class cannot be instantiated");
    }

    // ========================================================================
    // Job 名稱（已定義基礎 Job，需模組開發時補充具體 Job）
    // ========================================================================

    /**
     * 同步股價資料 Job
     * 執行頻率: 每日 15:00
     */
    public static final String SYNC_STOCK_PRICES = "SYNC_STOCK_PRICES";

    /**
     * 同步籌碼資料 Job
     * 執行頻率: 每日 15:30
     */
    public static final String SYNC_CHIP_DATA = "SYNC_CHIP_DATA";

    /**
     * 同步財報資料 Job
     * 執行頻率: 每季財報公告後
     */
    public static final String SYNC_FINANCIAL_DATA = "SYNC_FINANCIAL_DATA";

    /**
     * 同步股票清單 Job
     * 執行頻率: 每月 1 日 08:00
     */
    public static final String SYNC_STOCK_LIST = "SYNC_STOCK_LIST";

    /**
     * 同步交易日曆 Job
     * 執行頻率: 每年 1 月 1 日 02:00
     */
    public static final String SYNC_TRADING_CALENDAR = "SYNC_TRADING_CALENDAR";

    // TODO: M07 開發時補充技術指標計算 Job
    // 範例:
    // public static final String CALC_TECHNICAL_INDICATORS = "CALC_TECHNICAL_INDICATORS";
    // 執行頻率: 每日 16:00

    // TODO: M13 開發時補充信號偵測 Job
    // 範例:
    // public static final String DETECT_TECHNICAL_SIGNALS = "DETECT_TECHNICAL_SIGNALS";
    // 執行頻率: 每日 17:00

    // TODO: M15 開發時補充警報發送 Job
    // 範例:
    // public static final String SEND_DAILY_ALERTS = "SEND_DAILY_ALERTS";
    // 執行頻率: 每日 18:00


    // ========================================================================
    // Cron 表達式（遵守總綱 4.5.5 規範）
    // ========================================================================

    /**
     * 每日 15:00 執行（週一到週五）
     * 用於: 股價同步
     */
    public static final String CRON_DAILY_1500_WEEKDAY = "0 0 15 * * MON-FRI";

    /**
     * 每日 15:30 執行（週一到週五）
     * 用於: 籌碼資料同步
     */
    public static final String CRON_DAILY_1530_WEEKDAY = "0 30 15 * * MON-FRI";

    /**
     * 每日 16:00 執行（週一到週五）
     * 用於: 技術指標計算
     */
    public static final String CRON_DAILY_1600_WEEKDAY = "0 0 16 * * MON-FRI";

    /**
     * 每日 17:00 執行（週一到週五）
     * 用於: 信號偵測
     */
    public static final String CRON_DAILY_1700_WEEKDAY = "0 0 17 * * MON-FRI";

    /**
     * 每日 18:00 執行
     * 用於: 警報發送
     */
    public static final String CRON_DAILY_1800 = "0 0 18 * * *";

    /**
     * 每日 01:00 執行
     * 用於: 資料清理
     */
    public static final String CRON_DAILY_0100 = "0 0 1 * * *";

    /**
     * 每週日 02:00 執行
     * 用於: 週期性維護
     */
    public static final String CRON_WEEKLY_SUNDAY_0200 = "0 0 2 * * SUN";

    /**
     * 每月 1 日 08:00 執行
     * 用於: 股票清單更新
     */
    public static final String CRON_MONTHLY_1ST_0800 = "0 0 8 1 * *";

    /**
     * 每年 1 月 1 日 02:00 執行
     * 用於: 交易日曆更新
     */
    public static final String CRON_YEARLY_JAN1_0200 = "0 0 2 1 1 *";


    // ========================================================================
    // Job 重試配置（遵守總綱 4.5.4 規範）
    // ========================================================================

    /**
     * 預設最大重試次數
     */
    public static final int DEFAULT_MAX_RETRY = 3;

    /**
     * 重試延遲時間（秒）
     */
    public static final int RETRY_DELAY_SECONDS = 300;  // 5 分鐘

    /**
     * Job 執行超時時間（秒）
     */
    public static final int JOB_TIMEOUT_SECONDS = 3600;  // 1 小時


    // ========================================================================
    // Job 參數 Key（用於 JSONB parameters 欄位）
    // ========================================================================

    /**
     * 交易日期參數 Key
     */
    public static final String PARAM_TRADE_DATE = "trade_date";

    /**
     * 股票代碼清單參數 Key
     */
    public static final String PARAM_STOCK_IDS = "stock_ids";

    /**
     * 開始日期參數 Key
     */
    public static final String PARAM_START_DATE = "start_date";

    /**
     * 結束日期參數 Key
     */
    public static final String PARAM_END_DATE = "end_date";

    /**
     * 年份參數 Key
     */
    public static final String PARAM_YEAR = "year";

    /**
     * 季度參數 Key
     */
    public static final String PARAM_QUARTER = "quarter";

    /**
     * 強制重新執行參數 Key
     */
    public static final String PARAM_FORCE_RERUN = "force_rerun";

    // TODO: 各模組開發時，可以在此補充 Job 參數 Key
}
