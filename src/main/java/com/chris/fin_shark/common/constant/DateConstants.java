package com.chris.fin_shark.common.constant;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 日期時間格式常量
 *
 * 遵守總綱 4.4.6 日期時間格式規範
 * 統一使用 ISO 8601 格式
 *
 * @author chris
 * @since 2025-12-24
 */
public final class DateConstants {

    private DateConstants() {
        throw new UnsupportedOperationException("Constant class cannot be instantiated");
    }

    // ========================================================================
    // 時區
    // ========================================================================

    /**
     * 台北時區 (UTC+8)
     */
    public static final ZoneId TAIPEI_ZONE = ZoneId.of("Asia/Taipei");

    /**
     * UTC 時區
     */
    public static final ZoneId UTC_ZONE = ZoneId.of("UTC");


    // ========================================================================
    // 日期格式（ISO 8601）
    // ========================================================================

    /**
     * 日期格式: YYYY-MM-DD
     * 範例: 2024-12-24
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 日期時間格式: YYYY-MM-DDTHH:mm:ss
     * 範例: 2024-12-24T13:30:00
     */
    public static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * 日期時間格式（含時區）: YYYY-MM-DDTHH:mm:ss+08:00
     * 範例: 2024-12-24T13:30:00+08:00
     */
    public static final String DATETIME_WITH_ZONE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

    /**
     * 時間格式: HH:mm:ss
     * 範例: 13:30:00
     */
    public static final String TIME_FORMAT = "HH:mm:ss";

    /**
     * 年月格式: YYYY-MM
     * 範例: 2024-12
     */
    public static final String YEAR_MONTH_FORMAT = "yyyy-MM";

    /**
     * 年份格式: YYYY
     * 範例: 2024
     */
    public static final String YEAR_FORMAT = "yyyy";

    /**
     * 緊湊日期格式: YYYYMMDD
     * 範例: 20241224
     */
    public static final String COMPACT_DATE_FORMAT = "yyyyMMdd";

    /**
     * 緊湊日期時間格式: YYYYMMDDHHmmss
     * 範例: 20241224133000
     */
    public static final String COMPACT_DATETIME_FORMAT = "yyyyMMddHHmmss";


    // ========================================================================
    // DateTimeFormatter 實例（提升效能，避免重複創建）
    // ========================================================================

    /**
     * 日期 Formatter
     */
    public static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern(DATE_FORMAT);

    /**
     * 日期時間 Formatter
     */
    public static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern(DATETIME_FORMAT);

    /**
     * 日期時間（含時區）Formatter
     */
    public static final DateTimeFormatter DATETIME_WITH_ZONE_FORMATTER =
            DateTimeFormatter.ofPattern(DATETIME_WITH_ZONE_FORMAT);

    /**
     * 時間 Formatter
     */
    public static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern(TIME_FORMAT);

    /**
     * 年月 Formatter
     */
    public static final DateTimeFormatter YEAR_MONTH_FORMATTER =
            DateTimeFormatter.ofPattern(YEAR_MONTH_FORMAT);

    /**
     * 緊湊日期 Formatter
     */
    public static final DateTimeFormatter COMPACT_DATE_FORMATTER =
            DateTimeFormatter.ofPattern(COMPACT_DATE_FORMAT);


    // ========================================================================
    // 台股交易時間（已完整定義）
    // ========================================================================

    /**
     * 盤前開始時間: 08:30
     */
    public static final String PRE_MARKET_OPEN = "08:30:00";

    /**
     * 開盤時間: 09:00
     */
    public static final String MARKET_OPEN = "09:00:00";

    /**
     * 收盤時間: 13:30
     */
    public static final String MARKET_CLOSE = "13:30:00";

    /**
     * 盤後交易時間: 14:00 - 14:30
     */
    public static final String AFTER_MARKET_OPEN = "14:00:00";
    public static final String AFTER_MARKET_CLOSE = "14:30:00";


    // ========================================================================
    // 資料同步時間（遵守總綱 4.5.5 Job 排程規範）
    // ========================================================================

    /**
     * 股價同步時間: 15:00
     */
    public static final String STOCK_PRICE_SYNC_TIME = "15:00:00";

    /**
     * 籌碼資料同步時間: 15:30
     */
    public static final String CHIP_DATA_SYNC_TIME = "15:30:00";

    /**
     * 技術指標計算時間: 16:00
     */
    public static final String INDICATOR_CALC_TIME = "16:00:00";

    /**
     * 信號偵測時間: 17:00
     */
    public static final String SIGNAL_DETECT_TIME = "17:00:00";

    /**
     * 警報發送時間: 18:00
     */
    public static final String ALERT_SEND_TIME = "18:00:00";

    /**
     * 資料清理時間: 01:00
     */
    public static final String DATA_CLEANUP_TIME = "01:00:00";
}
