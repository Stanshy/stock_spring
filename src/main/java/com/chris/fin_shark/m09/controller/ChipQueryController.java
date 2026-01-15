package com.chris.fin_shark.m09.controller;

import com.chris.fin_shark.common.dto.ApiResponse;
import com.chris.fin_shark.m09.dto.ChipAnalysisResultDTO;
import com.chris.fin_shark.m09.dto.request.ChipRankingRequest;
import com.chris.fin_shark.m09.dto.response.ChipRankingResponse;
import com.chris.fin_shark.m09.dto.response.ChipSignalsResponse;
import com.chris.fin_shark.m09.dto.response.StockChipResponse;
import com.chris.fin_shark.m09.service.ChipQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * 籌碼查詢 Controller
 * <p>
 * 功能編號: F-M09-001, F-M09-002, F-M09-005
 * 提供籌碼分析資料、排行榜、異常訊號的查詢 API
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class ChipQueryController {

    private final ChipQueryService chipQueryService;

    /**
     * API-M09-001: 查詢個股籌碼分析
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期（可選，預設30天前）
     * @param endDate   結束日期（可選，預設今日）
     * @return 個股籌碼分析回應
     */
    @GetMapping("/stocks/{stockId}/chip")
    public ApiResponse<StockChipResponse> getStockChipAnalysis(
            @PathVariable String stockId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        log.info("GET /api/stocks/{}/chip", stockId);

        // 設定預設日期範圍
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        StockChipResponse response = chipQueryService
                .getStockChipAnalysis(stockId, startDate, endDate);

        return ApiResponse.success(response);
    }

    /**
     * API-M09-002: 批次查詢最新籌碼分析
     *
     * @param stockIds 股票代碼列表（逗號分隔）
     * @return 籌碼分析列表
     */
    @GetMapping("/chip/latest")
    public ApiResponse<List<ChipAnalysisResultDTO>> getLatestChipAnalysis(
            @RequestParam String stockIds) {

        log.info("GET /api/chip/latest?stockIds={}", stockIds);

        List<String> stockIdList = Arrays.asList(stockIds.split(","));
        List<ChipAnalysisResultDTO> response = chipQueryService.getLatestByStocks(stockIdList);

        return ApiResponse.success(response);
    }

    /**
     * API-M09-003: 查詢籌碼排行榜
     *
     * @param request 排行榜請求
     * @return 籌碼排行榜回應
     */
    @PostMapping("/chip/rankings")
    public ApiResponse<ChipRankingResponse> getChipRanking(
            @Valid @RequestBody ChipRankingRequest request) {

        log.info("POST /api/chip/rankings: type={}, date={}",
                request.getRankType(), request.getTradeDate());

        ChipRankingResponse response = chipQueryService.getChipRanking(request);

        return ApiResponse.success(response);
    }

    /**
     * API-M09-003 (GET): 查詢籌碼排行榜（簡化版）
     *
     * @param rankType   排行榜類型
     * @param tradeDate  交易日期
     * @param marketType 市場類型
     * @param limit      筆數限制
     * @return 籌碼排行榜回應
     */
    @GetMapping("/chip/rankings")
    public ApiResponse<ChipRankingResponse> getChipRankingByGet(
            @RequestParam String rankType,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate tradeDate,
            @RequestParam(required = false) String marketType,
            @RequestParam(required = false, defaultValue = "50") Integer limit) {

        log.info("GET /api/chip/rankings?rankType={}&date={}", rankType, tradeDate);

        ChipRankingRequest request = ChipRankingRequest.builder()
                .rankType(rankType)
                .tradeDate(tradeDate)
                .marketType(marketType)
                .limit(limit)
                .build();

        ChipRankingResponse response = chipQueryService.getChipRanking(request);

        return ApiResponse.success(response);
    }

    /**
     * API-M09-004: 查詢籌碼異常訊號
     *
     * @param tradeDate  交易日期
     * @param severity   嚴重度過濾（CRITICAL, HIGH, MEDIUM, LOW）
     * @param signalType 訊號類型過濾（INSTITUTIONAL, MARGIN）
     * @return 籌碼訊號回應
     */
    @GetMapping("/chip/signals")
    public ApiResponse<ChipSignalsResponse> getChipSignals(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate tradeDate,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String signalType) {

        log.info("GET /api/chip/signals?date={}&severity={}&type={}",
                tradeDate, severity, signalType);

        ChipSignalsResponse response = chipQueryService
                .getChipSignals(tradeDate, severity, signalType);

        return ApiResponse.success(response);
    }

    /**
     * API-M09-005: 查詢個股籌碼訊號
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 個股訊號列表
     */
    @GetMapping("/stocks/{stockId}/chip/signals")
    public ApiResponse<ChipSignalsResponse> getStockChipSignals(
            @PathVariable String stockId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        log.info("GET /api/stocks/{}/chip/signals", stockId);

        // 設定預設日期範圍
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        var signals = chipQueryService.getStockSignals(stockId, startDate, endDate);

        ChipSignalsResponse response = ChipSignalsResponse.builder()
                .tradeDate(endDate)
                .totalCount(signals.size())
                .signals(signals)
                .build();

        return ApiResponse.success(response);
    }
}
