package com.chris.fin_shark.m07.controller;

import com.chris.fin_shark.common.dto.ApiResponse;
import com.chris.fin_shark.m07.dto.response.*;
import com.chris.fin_shark.m07.service.IndicatorQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 指標查詢 Controller
 * <p>
 * 功能編號: F-M07-009
 * 功能名稱: 指標查詢 API
 * 提供技術指標的查詢操作
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class IndicatorQueryController {

    private final IndicatorQueryService indicatorQueryService;

    /**
     * API-M07-001: 查詢單一股票技術指標
     *
     * @param stockId    股票代碼
     * @param startDate  開始日期（可選，預設30天前）
     * @param endDate    結束日期（可選，預設今日）
     * @param indicators 指標名稱清單（可選，逗號分隔）
     * @param categories 指標類別（可選，逗號分隔）
     * @return 技術指標回應
     */
    @GetMapping("/stocks/{stockId}/indicators")
    public ApiResponse<StockIndicatorsResponse> getStockIndicators(
            @PathVariable String stockId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String indicators,
            @RequestParam(required = false) String categories) {

        log.info("GET /api/stocks/{}/indicators", stockId);

        // 設定預設日期範圍
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        StockIndicatorsResponse response = indicatorQueryService
                .getStockIndicators(stockId, startDate, endDate, indicators, categories);

        return ApiResponse.success(response);
    }

    /**
     * API-M07-002: 查詢單一股票特定指標
     *
     * @param stockId       股票代碼
     * @param indicatorName 指標名稱（如 MA, RSI, MACD）
     * @param startDate     開始日期（可選，預設30天前）
     * @param endDate       結束日期（可選，預設今日）
     * @return 特定指標資料
     */
    @GetMapping("/stocks/{stockId}/indicators/{indicatorName}")
    public ApiResponse<SpecificIndicatorResponse> getSpecificIndicator(
            @PathVariable String stockId,
            @PathVariable String indicatorName,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        log.info("GET /api/stocks/{}/indicators/{}", stockId, indicatorName);

        // 設定預設日期範圍
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        SpecificIndicatorResponse response = indicatorQueryService
                .getSpecificIndicator(stockId, indicatorName, startDate, endDate);

        return ApiResponse.success(response);
    }

    /**
     * API-M07-003: 批次查詢最新指標
     *
     * @param stockIds   股票代碼清單（逗號分隔）
     * @param indicators 指標名稱清單（可選，逗號分隔）
     * @return 最新指標列表
     */
    @GetMapping("/indicators/latest")
    public ApiResponse<List<LatestIndicatorsResponse>> getLatestIndicators(
            @RequestParam String stockIds,
            @RequestParam(required = false) String indicators) {

        log.info("GET /api/indicators/latest?stockIds={}", stockIds);

        List<LatestIndicatorsResponse> response = indicatorQueryService
                .getLatestIndicators(stockIds, indicators);

        return ApiResponse.success(response);
    }

    /**
     * API-M07-004: 查詢交叉信號
     *
     * @param crossType  交叉類型（GOLDEN, DEATH, KD）
     * @param date       查詢日期（可選，預設最新交易日）
     * @param marketType 市場類型（TWSE, OTC）
     * @return 交叉信號回應
     */
    @GetMapping("/indicators/signals/crosses")
    public ApiResponse<CrossSignalsResponse> getCrossCandidates(
            @RequestParam(required = false) String crossType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false) String marketType) {

        log.info("GET /api/indicators/signals/crosses?date={}&type={}", date, crossType);

        if (date == null) {
            date = LocalDate.now().minusDays(1); // 預設查詢前一交易日
        }

        CrossSignalsResponse response = indicatorQueryService
                .getCrossSignals(crossType, date, marketType);

        return ApiResponse.success(response);
    }

    /**
     * API-M07-005: 查詢超買超賣信號
     *
     * @param signalType 信號類型（OVERBOUGHT, OVERSOLD）
     * @param indicator  指標（RSI, KD, WILLIAMS_R）
     * @param date       查詢日期（可選，預設最新交易日）
     * @return 超買超賣信號回應
     */
    @GetMapping("/indicators/signals/overbought")
    public ApiResponse<OverboughtOversoldResponse> getOverboughtOversoldStocks(
            @RequestParam(required = false) String signalType,
            @RequestParam(required = false) String indicator,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        log.info("GET /api/indicators/signals/overbought?date={}&type={}", date, signalType);

        if (date == null) {
            date = LocalDate.now().minusDays(1); // 預設查詢前一交易日
        }

        OverboughtOversoldResponse response = indicatorQueryService
                .getOverboughtOversoldSignals(signalType, indicator, date);

        return ApiResponse.success(response);
    }
}

