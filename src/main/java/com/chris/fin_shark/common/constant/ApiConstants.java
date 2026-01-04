package com.chris.fin_shark.common.constant;

/**
 * API 相關常量
 *
 * 定義 API 路徑、分頁參數等常量
 *
 * @author chris
 * @since 2025-12-24
 */
public final class ApiConstants {

    private ApiConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ========================================================================
    // API 路徑常量
    // ========================================================================

    /**
     * API 基礎路徑
     *
     * 所有 API 都以此路徑為前綴
     * 範例: /api/stocks, /api/stock-prices
     */
    public static final String API_BASE = "/api";

    // ========================================================================
    // 資源路徑常量
    // ========================================================================

    /**
     * 股票資源路徑
     *
     * 組合使用: API_BASE + STOCKS_PATH = "/api/stocks"
     */
    public static final String STOCKS_PATH = "/stocks";

    /**
     * 股價資源路徑
     *
     * 組合使用: API_BASE + STOCK_PRICES_PATH = "/api/stock-prices"
     */
    public static final String STOCK_PRICES_PATH = "/stock-prices";

    /**
     * 技術指標資源路徑
     *
     * 組合使用: API_BASE + INDICATORS_PATH = "/api/indicators"
     */
    public static final String INDICATORS_PATH = "/indicators";

    /**
     * 信號資源路徑
     *
     * 組合使用: API_BASE + SIGNALS_PATH = "/api/signals"
     */
    public static final String SIGNALS_PATH = "/signals";

    /**
     * Job 資源路徑
     *
     * 組合使用: API_BASE + JOBS_PATH = "/api/jobs"
     */
    public static final String JOBS_PATH = "/jobs";

    /**
     * 財務資料資源路徑
     *
     * 組合使用: API_BASE + FINANCIALS_PATH = "/api/financials"
     *
     * TODO: M08 開發時使用
     */
    public static final String FINANCIALS_PATH = "/financials";

    /**
     * 警報資源路徑
     *
     * 組合使用: API_BASE + ALERTS_PATH = "/api/alerts"
     *
     * TODO: M15 開發時使用
     */
    public static final String ALERTS_PATH = "/alerts";

    // ========================================================================
    // 分頁參數常量
    // ========================================================================

    /**
     * 預設頁碼（從 1 開始）
     *
     * 符合總綱 4.4.5 分頁規範
     */
    public static final int DEFAULT_PAGE = 1;

    /**
     * 預設每頁大小
     */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * 最大每頁大小
     *
     * 防止過大的分頁請求影響效能
     */
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * 頁碼參數名稱
     *
     * 用於 @RequestParam
     */
    public static final String PAGE_PARAM = "page";

    /**
     * 每頁大小參數名稱
     *
     * 用於 @RequestParam
     */
    public static final String SIZE_PARAM = "size";

    /**
     * 排序參數名稱
     *
     * 用於 @RequestParam
     * 範例: sort=name,asc
     */
    public static final String SORT_PARAM = "sort";

    // ========================================================================
    // 查詢參數常量
    // ========================================================================

    /**
     * 搜尋關鍵字參數名稱
     */
    public static final String SEARCH_PARAM = "search";

    /**
     * 開始日期參數名稱
     */
    public static final String START_DATE_PARAM = "start_date";

    /**
     * 結束日期參數名稱
     */
    public static final String END_DATE_PARAM = "end_date";

    /**
     * 狀態參數名稱
     */
    public static final String STATUS_PARAM = "status";

    /**
     * 類型參數名稱
     */
    public static final String TYPE_PARAM = "type";




    // ========================================================================
// HTTP Header 常量
// ========================================================================

    /**
     * 追蹤 ID Header 名稱
     *
     * 用於追蹤請求在系統中的完整路徑
     * TraceIdFilter 會自動產生或從請求中提取
     */
    public static final String HEADER_TRACE_ID = "X-Trace-Id";

    /**
     * 請求 ID Header 名稱
     *
     * 用於唯一識別單一 HTTP 請求
     */
    public static final String HEADER_REQUEST_ID = "X-Request-Id";

// ========================================================================
// 請求/回應相關常量
// ========================================================================

    /**
     * 預設請求超時時間（秒）
     *
     * 用於外部 API 調用
     */
    public static final int DEFAULT_REQUEST_TIMEOUT = 30;

    /**
     * 預設連線超時時間（秒）
     */
    public static final int DEFAULT_CONNECT_TIMEOUT = 10;

    /**
     * 預設讀取超時時間（秒）
     */
    public static final int DEFAULT_READ_TIMEOUT = 30;
}

