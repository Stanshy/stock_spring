package com.chris.fin_shark.client.twse;

import com.chris.fin_shark.common.dto.external.TwseMarginData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * TWSE 融資融券 API 客戶端
 * <p>
 * 功能編號: F-M06-002
 * 負責抓取融資融券交易資料
 * </p>
 * <p>
 * API 端點：https://www.twse.com.tw/rwd/zh/marginTrading/MI_MARGN
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TwseMarginClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * TWSE 融資融券彙總 API 端點
     */
    private static final String TWSE_MARGIN_URL =
            "https://www.twse.com.tw/rwd/zh/marginTrading/MI_MARGN";

    /**
     * 日期格式（用於 API 參數）
     */
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 查詢指定日期的融資融券資料
     * <p>
     * 呼叫 TWSE MI_MARGN API 取得指定日期所有股票的融資融券資料，
     * 然後篩選出指定的股票清單
     * </p>
     *
     * @param tradeDate 交易日期
     * @param stockIds  要篩選的股票代碼集合
     * @return 融資融券資料列表
     */
    public List<TwseMarginData> getMarginTrading(LocalDate tradeDate, Set<String> stockIds) {
        log.info("呼叫 TWSE MI_MARGN API: tradeDate={}, stockCount={}", tradeDate, stockIds.size());

        try {
            // 1. 建立 API 請求 URL
            String dateStr = tradeDate.format(DATE_FORMAT);
            String url = String.format("%s?date=%s&selectType=ALL&response=json", TWSE_MARGIN_URL, dateStr);

            log.debug("TWSE MI_MARGN API URL: {}", url);

            // 2. 呼叫 API
            String responseBody = restTemplate.getForObject(url, String.class);

            if (responseBody == null || responseBody.isEmpty()) {
                log.warn("TWSE MI_MARGN API 回應為空: tradeDate={}", tradeDate);
                return List.of();
            }

            // 3. 解析 JSON 回應
            JsonNode root = objectMapper.readTree(responseBody);

            // 檢查回應狀態
            String status = root.path("stat").asText();
            if (!"OK".equals(status)) {
                log.warn("TWSE MI_MARGN API 回應異常: status={}, tradeDate={}", status, tradeDate);
                return List.of();
            }

            // 4. 解析融資融券資料（tables[1] 是個股資料）
            List<TwseMarginData> results = parseMarginData(root, tradeDate, stockIds);
            log.info("TWSE MI_MARGN API 回應成功: tradeDate={}, 資料筆數={}", tradeDate, results.size());

            return results;

        } catch (Exception e) {
            log.error("呼叫 TWSE MI_MARGN API 失敗: tradeDate={}", tradeDate, e);
            return List.of();
        }
    }

    /**
     * 解析融資融券資料
     * <p>
     * MI_MARGN API 回應格式 (tables[1].data)：
     * [股票代號, 股票名稱,
     *  融資買進, 融資賣出, 融資現金償還, 融資前日餘額, 融資今日餘額, 融資限額,
     *  融券買進, 融券賣出, 融券現券償還, 融券前日餘額, 融券今日餘額, 融券限額,
     *  資券互抵, 備註]
     * </p>
     */
    private List<TwseMarginData> parseMarginData(
            JsonNode root, LocalDate tradeDate, Set<String> stockIds) {

        List<TwseMarginData> results = new ArrayList<>();

        // MI_MARGN API 使用 tables 結構，個股資料在 tables[1]
        JsonNode tables = root.path("tables");
        if (!tables.isArray() || tables.size() < 2) {
            log.warn("TWSE MI_MARGN API 回應無 tables 結構");
            return results;
        }

        JsonNode dataArray = tables.get(1).path("data");
        if (!dataArray.isArray()) {
            log.warn("TWSE MI_MARGN API 回應無個股資料");
            return results;
        }

        for (JsonNode row : dataArray) {
            try {
                if (!row.isArray() || row.size() < 14) {
                    continue;
                }

                String stockId = row.get(0).asText().trim();

                // 篩選指定的股票
                if (!stockIds.contains(stockId)) {
                    continue;
                }

                TwseMarginData data = TwseMarginData.builder()
                        .stockId(stockId)
                        .tradeDate(tradeDate)
                        // 融資欄位
                        .marginPurchase(parseLong(row.get(2).asText()))      // 融資買進
                        .marginSell(parseLong(row.get(3).asText()))          // 融資賣出
                        .marginCashRepayment(parseLong(row.get(4).asText())) // 融資現金償還
                        .marginBalance(parseLong(row.get(6).asText()))       // 融資今日餘額
                        .marginQuota(parseLong(row.get(7).asText()))         // 融資限額
                        // 融券欄位
                        .shortPurchase(parseLong(row.get(8).asText()))       // 融券買進
                        .shortSell(parseLong(row.get(9).asText()))           // 融券賣出
                        .shortCashRepayment(parseLong(row.get(10).asText())) // 融券現券償還
                        .shortBalance(parseLong(row.get(12).asText()))       // 融券今日餘額
                        .shortQuota(parseLong(row.get(13).asText()))         // 融券限額
                        .build();

                results.add(data);

            } catch (Exception e) {
                log.warn("解析融資融券資料失敗: row={}", row, e);
            }
        }

        return results;
    }

    /**
     * 解析長整數（處理千分位逗號）
     */
    private Long parseLong(String value) {
        if (value == null || value.trim().isEmpty() || "--".equals(value.trim())) {
            return 0L;
        }
        try {
            return Long.parseLong(value.replace(",", "").trim());
        } catch (NumberFormatException e) {
            log.warn("解析 Long 失敗: value={}", value);
            return 0L;
        }
    }
}
