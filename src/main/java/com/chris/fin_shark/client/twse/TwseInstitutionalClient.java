package com.chris.fin_shark.client.twse;

import com.chris.fin_shark.common.dto.external.TwseInstitutionalData;
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
 * TWSE 三大法人買賣超 API 客戶端
 * <p>
 * 功能編號: F-M06-002
 * 負責抓取三大法人（外資、投信、自營商）買賣超資料
 * </p>
 * <p>
 * API 端點：https://www.twse.com.tw/rwd/zh/fund/T86
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TwseInstitutionalClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * TWSE 三大法人買賣超日報 API 端點
     * 回傳指定日期的所有股票法人買賣資料
     */
    private static final String TWSE_T86_URL =
            "https://www.twse.com.tw/rwd/zh/fund/T86";

    /**
     * 日期格式（用於 API 參數）
     */
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 查詢指定日期的三大法人買賣超資料
     * <p>
     * 呼叫 TWSE T86 API 取得指定日期所有股票的法人買賣資料，
     * 然後篩選出指定的股票清單
     * </p>
     *
     * @param tradeDate 交易日期
     * @param stockIds  要篩選的股票代碼集合
     * @return 法人買賣超資料列表
     */
    public List<TwseInstitutionalData> getInstitutionalTrading(LocalDate tradeDate, Set<String> stockIds) {
        log.info("呼叫 TWSE T86 API: tradeDate={}, stockCount={}", tradeDate, stockIds.size());

        try {
            // 1. 建立 API 請求 URL
            String dateStr = tradeDate.format(DATE_FORMAT);
            String url = String.format("%s?date=%s&selectType=ALLBUT0999&response=json", TWSE_T86_URL, dateStr);

            log.debug("TWSE T86 API URL: {}", url);

            // 2. 呼叫 API
            String responseBody = restTemplate.getForObject(url, String.class);

            if (responseBody == null || responseBody.isEmpty()) {
                log.warn("TWSE T86 API 回應為空: tradeDate={}", tradeDate);
                return List.of();
            }

            // 3. 解析 JSON 回應
            JsonNode root = objectMapper.readTree(responseBody);

            // 檢查回應狀態
            String status = root.path("stat").asText();
            if (!"OK".equals(status)) {
                log.warn("TWSE T86 API 回應異常: status={}, tradeDate={}", status, tradeDate);
                return List.of();
            }

            // 4. 解析法人買賣資料
            List<TwseInstitutionalData> results = parseInstitutionalData(root, tradeDate, stockIds);
            log.info("TWSE T86 API 回應成功: tradeDate={}, 資料筆數={}", tradeDate, results.size());

            return results;

        } catch (Exception e) {
            log.error("呼叫 TWSE T86 API 失敗: tradeDate={}", tradeDate, e);
            return List.of();
        }
    }

    /**
     * 解析法人買賣超資料
     * <p>
     * T86 API 回應格式：
     * data[]: [證券代號, 證券名稱, 外資買進, 外資賣出, 外資買賣超,
     *          投信買進, 投信賣出, 投信買賣超,
     *          自營商買進, 自營商賣出, 自營商買賣超,
     *          自營商(自行)買進, 自營商(自行)賣出, 自營商(自行)買賣超,
     *          自營商(避險)買進, 自營商(避險)賣出, 自營商(避險)買賣超,
     *          三大法人買賣超合計]
     * </p>
     */
    private List<TwseInstitutionalData> parseInstitutionalData(
            JsonNode root, LocalDate tradeDate, Set<String> stockIds) {

        List<TwseInstitutionalData> results = new ArrayList<>();
        JsonNode dataArray = root.path("data");

        if (!dataArray.isArray()) {
            log.warn("TWSE T86 API 回應無資料陣列");
            return results;
        }

        for (JsonNode row : dataArray) {
            try {
                if (!row.isArray() || row.size() < 12) {
                    continue;
                }

                String stockId = row.get(0).asText().trim();

                // 篩選指定的股票
                if (!stockIds.contains(stockId)) {
                    continue;
                }

                TwseInstitutionalData data = TwseInstitutionalData.builder()
                        .stockId(stockId)
                        .tradeDate(tradeDate)
                        .foreignBuy(parseLong(row.get(2).asText()))      // 外資買進
                        .foreignSell(parseLong(row.get(3).asText()))     // 外資賣出
                        .trustBuy(parseLong(row.get(5).asText()))        // 投信買進
                        .trustSell(parseLong(row.get(6).asText()))       // 投信賣出
                        .dealerBuy(parseLong(row.get(8).asText()))       // 自營商買進(合計)
                        .dealerSell(parseLong(row.get(9).asText()))      // 自營商賣出(合計)
                        .build();

                results.add(data);

            } catch (Exception e) {
                log.warn("解析法人資料失敗: row={}", row, e);
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
