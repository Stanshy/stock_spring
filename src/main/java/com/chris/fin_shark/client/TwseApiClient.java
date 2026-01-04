package com.chris.fin_shark.client;

import com.chris.fin_shark.m06.dto.external.TwseApiResponse;
import com.chris.fin_shark.m06.dto.external.TwseStockPriceData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 台灣證券交易所 API 客戶端
 * <p>
 * 功能編號: F-M06-002
 * 負責與台灣證券交易所的公開 API 進行對接，抓取股票交易資料
 * </p>
 * <p>
 * API 文件：https://www.twse.com.tw/zh/products/information/api/api_web.html
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TwseApiClient {

    private final RestTemplate restTemplate;

    /**
     * TWSE 個股日成交資訊 API 端點
     */
    private static final String TWSE_STOCK_DAY_URL =
            "https://www.twse.com.tw/rwd/zh/afterTrading/STOCK_DAY";

    /**
     * 西元日期格式（用於 API 參數）
     * 格式：yyyyMMdd（例如：20250102）
     */
    private static final DateTimeFormatter TWSE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 查詢個股當月日成交資訊
     * <p>
     * 呼叫 TWSE API 取得指定股票在指定月份的所有交易日資料
     * 注意：TWSE API 一次返回整個月的資料
     * </p>
     *
     * @param stockId 股票代碼（例如：2330）
     * @param date    查詢日期（會取得該日期所屬月份的所有資料）
     * @return 該月份所有交易日的股價資料列表
     */
    public List<TwseStockPriceData> getStockMonthlyPrices(String stockId, LocalDate date) {
        log.info("呼叫 TWSE API: stockId={}, date={}", stockId, date);

        try {
            // 1. 建立 API 請求 URL
            String dateStr = date.format(TWSE_DATE_FORMAT);
            String url = String.format("%s?date=%s&stockNo=%s&response=json",
                    TWSE_STOCK_DAY_URL, dateStr, stockId);

            log.debug("TWSE API URL: {}", url);

            // 2. 呼叫 API
            TwseApiResponse response = restTemplate.getForObject(url, TwseApiResponse.class);

            // 3. 檢查回應狀態
            if (response == null || !"OK".equals(response.getStatus())) {
                log.warn("TWSE API 回應異常: status={}",
                        response != null ? response.getStatus() : "null");
                return List.of();
            }

            // 4. 解析股價資料
            List<TwseStockPriceData> prices = parseStockPrices(response);
            log.info("TWSE API 回應成功: stockId={}, 資料筆數={}", stockId, prices.size());

            return prices;

        } catch (Exception e) {
            log.error("呼叫 TWSE API 失敗: stockId={}, date={}", stockId, date, e);
            return List.of();
        }
    }

    /**
     * 解析 TWSE API 回應資料
     * <p>
     * 將 API 返回的二維字串陣列轉換為結構化的股價資料物件
     * </p>
     *
     * @param response TWSE API 回應物件
     * @return 股價資料列表
     */
    private List<TwseStockPriceData> parseStockPrices(TwseApiResponse response) {
        List<TwseStockPriceData> results = new ArrayList<>();

        // 檢查資料是否為空
        if (response.getData() == null || response.getData().isEmpty()) {
            log.warn("TWSE API 回應無資料");
            return results;
        }

        // 逐筆解析每個交易日的資料
        for (List<String> row : response.getData()) {
            try {
                // 欄位索引對應：
                // 0: 日期, 1: 成交股數, 2: 成交金額, 3: 開盤價,
                // 4: 最高價, 5: 最低價, 6: 收盤價, 7: 漲跌價差, 8: 成交筆數
                TwseStockPriceData data = TwseStockPriceData.builder()
                        .tradeDate(parseRocDate(row.get(0)))      // 日期
                        .volume(parseLong(row.get(1)))            // 成交股數
                        .turnover(parseBigDecimal(row.get(2)))    // 成交金額
                        .openPrice(parseBigDecimal(row.get(3)))   // 開盤價
                        .highPrice(parseBigDecimal(row.get(4)))   // 最高價
                        .lowPrice(parseBigDecimal(row.get(5)))    // 最低價
                        .closePrice(parseBigDecimal(row.get(6)))  // 收盤價
                        .changePrice(parseBigDecimal(row.get(7))) // 漲跌價差
                        .transactions(parseInteger(row.get(8)))   // 成交筆數
                        .build();

                results.add(data);

            } catch (Exception e) {
                log.warn("解析股價資料失敗: row={}", row, e);
                // 繼續處理下一筆資料
            }
        }

        log.debug("成功解析 {} 筆股價資料", results.size());
        return results;
    }

    /**
     * 解析民國日期
     * <p>
     * 將民國年格式（例如：113/01/02）轉換為 LocalDate
     * 民國年 = 西元年 - 1911
     * </p>
     *
     * @param rocDateStr 民國日期字串（格式：yyy/MM/dd）
     * @return LocalDate 物件
     * @throws IllegalArgumentException 如果日期格式錯誤
     */
    private LocalDate parseRocDate(String rocDateStr) {
        try {
            String[] parts = rocDateStr.split("/");
            int year = Integer.parseInt(parts[0]) + 1911;  // 民國年轉西元年
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);
            return LocalDate.of(year, month, day);
        } catch (Exception e) {
            throw new IllegalArgumentException("無效的民國日期格式: " + rocDateStr, e);
        }
    }

    /**
     * 解析長整數（處理千分位逗號）
     * <p>
     * TWSE API 回應的數字包含千分位逗號，需要移除後才能解析
     * 特殊值處理：
     * - null 或空字串 -> 0
     * - "--" -> 0（表示無資料）
     * </p>
     *
     * @param value 數字字串
     * @return 解析後的長整數
     */
    private Long parseLong(String value) {
        if (value == null || value.trim().isEmpty() || "--".equals(value)) {
            return 0L;
        }
        try {
            return Long.parseLong(value.replace(",", ""));
        } catch (NumberFormatException e) {
            log.warn("解析 Long 失敗: value={}", value);
            return 0L;
        }
    }

    /**
     * 解析整數（處理千分位逗號）
     *
     * @param value 數字字串
     * @return 解析後的整數
     */
    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty() || "--".equals(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value.replace(",", ""));
        } catch (NumberFormatException e) {
            log.warn("解析 Integer 失敗: value={}", value);
            return 0;
        }
    }

    /**
     * 解析 BigDecimal（處理千分位逗號和正負號）
     * <p>
     * 特殊符號處理：
     * - 移除千分位逗號（,）
     * - 移除正號（+）
     * - 移除 "X" 符號（表示漲停或跌停）
     * </p>
     *
     * @param value 數字字串
     * @return 解析後的 BigDecimal
     */
    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty() || "--".equals(value)) {
            return BigDecimal.ZERO;
        }
        try {
            // 移除特殊符號
            String cleaned = value.replace(",", "")
                    .replace("+", "")
                    .replace("X", "")
                    .trim();
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            log.warn("解析 BigDecimal 失敗: value={}", value);
            return BigDecimal.ZERO;
        }
    }
}