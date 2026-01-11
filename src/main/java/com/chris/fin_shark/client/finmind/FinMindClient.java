package com.chris.fin_shark.client.finmind;

import com.chris.fin_shark.common.dto.external.FinMindFinancialData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * FinMind API 客戶端
 * <p>
 * 功能編號: F-M06-002
 * 負責從 FinMind 第三方 API 抓取台股財務報表資料
 * </p>
 * <p>
 * API 文件：https://finmindtrade.com/analysis/#/data/api
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FinMindClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * FinMind API 基底 URL
     */
    private static final String FINMIND_BASE_URL = "https://api.finmindtrade.com/api/v4/data";

    /**
     * 日期格式
     */
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * FinMind API Token（可選，用於提高請求限額）
     */
    @Value("${finmind.api.token:}")
    private String apiToken;

    /**
     * 查詢指定股票的財務報表資料
     * <p>
     * Dataset: TaiwanStockFinancialStatement
     * </p>
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 財務報表資料列表
     */
    public List<FinMindFinancialData> getFinancialStatements(
            String stockId, LocalDate startDate, LocalDate endDate) {

        log.info("呼叫 FinMind API (財報): stockId={}, startDate={}, endDate={}", stockId, startDate, endDate);

        try {
            // 1. 建立 API 請求 URL
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(FINMIND_BASE_URL)
                    .queryParam("dataset", "TaiwanStockFinancialStatements")
                    .queryParam("data_id", stockId)
                    .queryParam("start_date", startDate.format(DATE_FORMAT))
                    .queryParam("end_date", endDate.format(DATE_FORMAT));

            // 如果有 API Token，加入請求
            if (apiToken != null && !apiToken.isEmpty()) {
                builder.queryParam("token", apiToken);
            }

            String url = builder.toUriString();
            log.debug("FinMind API URL: {}", url);

            // 2. 呼叫 API
            String responseBody = restTemplate.getForObject(url, String.class);

            if (responseBody == null || responseBody.isEmpty()) {
                log.warn("FinMind API 回應為空: stockId={}", stockId);
                return List.of();
            }

            // 3. 解析 JSON 回應
            JsonNode root = objectMapper.readTree(responseBody);

            // 檢查回應狀態
            int status = root.path("status").asInt();
            if (status != 200) {
                String msg = root.path("msg").asText();
                log.warn("FinMind API 回應異常: status={}, msg={}", status, msg);
                return List.of();
            }

            // 4. 解析財報資料
            List<FinMindFinancialData> results = parseFinancialData(root, stockId);
            log.info("FinMind API 回應成功: stockId={}, 資料筆數={}", stockId, results.size());

            return results;

        } catch (Exception e) {
            log.error("呼叫 FinMind API 失敗: stockId={}", stockId, e);
            return List.of();
        }
    }

    /**
     * 批量查詢多支股票的財務報表
     *
     * @param stockIds  股票代碼列表
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 財務報表資料列表
     */
    public List<FinMindFinancialData> getFinancialStatementsBatch(
            List<String> stockIds, LocalDate startDate, LocalDate endDate) {

        List<FinMindFinancialData> allResults = new ArrayList<>();

        for (String stockId : stockIds) {
            List<FinMindFinancialData> data = getFinancialStatements(stockId, startDate, endDate);
            allResults.addAll(data);

            // 避免請求過快，加入延遲
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return allResults;
    }

    /**
     * 查詢指定年度季度的財務報表
     *
     * @param stockId 股票代碼
     * @param year    年度
     * @param quarter 季度 (1-4)
     * @return 財務報表資料（可能為 null）
     */
    public FinMindFinancialData getFinancialStatementByPeriod(
            String stockId, int year, int quarter) {

        // 計算季度的日期範圍
        LocalDate startDate = LocalDate.of(year, (quarter - 1) * 3 + 1, 1);
        LocalDate endDate = startDate.plusMonths(3).minusDays(1);

        List<FinMindFinancialData> results = getFinancialStatements(stockId, startDate, endDate);

        // 找出匹配的季度資料
        return results.stream()
                .filter(d -> d.getYear() == year && d.getQuarter() == quarter)
                .findFirst()
                .orElse(null);
    }

    /**
     * 解析財務報表資料
     * <p>
     * FinMind 財報 API 回應格式：
     * {
     *   "msg": "success",
     *   "status": 200,
     *   "data": [
     *     {
     *       "date": "2024-03-31",
     *       "stock_id": "2330",
     *       "type": "IncomeStatement",  // or "BalanceSheet", "CashFlowStatement"
     *       "origin_name": "營業收入",
     *       "value": 123456789
     *     },
     *     ...
     *   ]
     * }
     * </p>
     */
    private List<FinMindFinancialData> parseFinancialData(JsonNode root, String stockId) {
        List<FinMindFinancialData> results = new ArrayList<>();
        JsonNode dataArray = root.path("data");

        if (!dataArray.isArray() || dataArray.isEmpty()) {
            log.warn("FinMind API 回應無資料");
            return results;
        }

        // 按日期分組財報資料
        java.util.Map<LocalDate, FinMindFinancialData.FinMindFinancialDataBuilder> builderMap = new java.util.HashMap<>();

        for (JsonNode row : dataArray) {
            try {
                String dateStr = row.path("date").asText();
                LocalDate date = LocalDate.parse(dateStr);
                String originName = row.path("origin_name").asText();
                BigDecimal value = parseBigDecimal(row.path("value").asText());

                // 取得或建立 Builder
                FinMindFinancialData.FinMindFinancialDataBuilder builder = builderMap.computeIfAbsent(
                        date,
                        d -> FinMindFinancialData.builder()
                                .stockId(stockId)
                                .date(d)
                                .year(d.getYear())
                                .quarter(getQuarter(d))
                                .reportType("Q")
                );

                // 根據欄位名稱填入對應值
                mapFieldValue(builder, originName, value);

            } catch (Exception e) {
                log.warn("解析財報資料列失敗: row={}", row, e);
            }
        }

        // 轉換 Builder 為 FinancialData
        for (FinMindFinancialData.FinMindFinancialDataBuilder builder : builderMap.values()) {
            results.add(builder.build());
        }

        return results;
    }

    /**
     * 根據欄位名稱映射值到 Builder
     */
    private void mapFieldValue(
            FinMindFinancialData.FinMindFinancialDataBuilder builder,
            String fieldName,
            BigDecimal value) {

        switch (fieldName) {
            // 損益表
            case "營業收入":
            case "營業收入合計":
                builder.revenue(value);
                break;
            case "營業成本":
            case "營業成本合計":
                builder.operatingCost(value);
                break;
            case "營業毛利":
            case "營業毛利（毛損）":
                builder.grossProfit(value);
                break;
            case "營業費用":
            case "營業費用合計":
                builder.operatingExpense(value);
                break;
            case "營業利益":
            case "營業利益（損失）":
                builder.operatingIncome(value);
                break;
            case "稅前淨利":
            case "繼續營業單位稅前淨利（淨損）":
                builder.preTaxIncome(value);
                break;
            case "稅後淨利":
            case "本期淨利（淨損）":
                builder.netIncome(value);
                break;
            case "基本每股盈餘":
                builder.eps(value);
                break;

            // 資產負債表
            case "資產總計":
            case "資產總額":
                builder.totalAssets(value);
                break;
            case "負債總計":
            case "負債總額":
                builder.totalLiabilities(value);
                break;
            case "權益總計":
            case "權益總額":
            case "股東權益總計":
                builder.equity(value);
                break;
            case "流動資產":
            case "流動資產合計":
                builder.currentAssets(value);
                break;
            case "流動負債":
            case "流動負債合計":
                builder.currentLiabilities(value);
                break;
            case "每股淨值":
                builder.bps(value);
                break;

            // 現金流量表
            case "營業活動之淨現金流入（流出）":
            case "營業活動現金流量":
                builder.operatingCashFlow(value);
                break;
            case "投資活動之淨現金流入（流出）":
            case "投資活動現金流量":
                builder.investingCashFlow(value);
                break;
            case "籌資活動之淨現金流入（流出）":
            case "融資活動現金流量":
                builder.financingCashFlow(value);
                break;

            default:
                // 其他欄位忽略
                break;
        }
    }

    /**
     * 根據日期計算季度
     */
    private Short getQuarter(LocalDate date) {
        int month = date.getMonthValue();
        return (short) ((month - 1) / 3 + 1);
    }

    /**
     * 解析 BigDecimal
     */
    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty() || "null".equals(value)) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(value.replace(",", "").trim());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}
