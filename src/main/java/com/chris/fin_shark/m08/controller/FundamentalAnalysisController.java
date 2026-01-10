package com.chris.fin_shark.m08.controller;

import com.chris.fin_shark.common.dto.ApiResponse;
import com.chris.fin_shark.m08.dto.FundamentalIndicatorDTO;
import com.chris.fin_shark.m08.dto.request.BatchQueryRequest;
import com.chris.fin_shark.m08.dto.request.TrendQueryRequest;
import com.chris.fin_shark.m08.service.FundamentalIndicatorService;
import com.chris.fin_shark.m08.vo.IndicatorTrendVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 基本面分析 Controller
 * <p>
 * 功能編號: F-M08-001 ~ F-M08-005
 * 提供財務指標查詢 API
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api")
@Slf4j
@RequiredArgsConstructor
public class FundamentalAnalysisController {

    private final FundamentalIndicatorService indicatorService;

    /**
     * API-M08-001: 查詢單一股票財務指標（P0 核心功能）
     * <p>
     * GET /api/stocks/{stockId}/fundamentals?year=2024&quarter=3
     * </p>
     *
     * @param stockId 股票代碼
     * @param year    年度（可選）
     * @param quarter 季度（可選）
     * @return 財務指標
     */
    @GetMapping("/stocks/{stockId}/fundamentals")
    public ApiResponse<FundamentalIndicatorDTO> getIndicators(
            @PathVariable String stockId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter) {

        log.info("GET /api/stocks/{}/fundamentals?year={}&quarter={}",
                stockId, year, quarter);

        FundamentalIndicatorDTO indicators = indicatorService.getIndicators(stockId, year, quarter);
        return ApiResponse.success(indicators);
    }

    /**
     * API-M08-004: 批次查詢財務指標（P0 核心功能）
     * <p>
     * POST /api/fundamentals/batch
     * </p>
     *
     * @param request 批次查詢請求
     * @return 財務指標列表
     */
    @PostMapping("/fundamentals/batch")
    public ApiResponse<List<FundamentalIndicatorDTO>> batchQuery(
            @Valid @RequestBody BatchQueryRequest request) {

        log.info("POST /api/fundamentals/batch: stockIds={}", request.getStockIds());

        List<FundamentalIndicatorDTO> indicators = indicatorService.batchQuery(request);
        return ApiResponse.success(indicators);
    }

    /**
     * API-M08-005: 查詢指標歷史趨勢（P0 核心功能）
     * <p>
     * POST /api/fundamentals/trends
     * </p>
     *
     * @param request 趨勢查詢請求
     * @return 趨勢資料
     */
    @PostMapping("/fundamentals/trends")
    public ApiResponse<List<IndicatorTrendVO>> queryTrend(
            @Valid @RequestBody TrendQueryRequest request) {

        log.info("POST /api/fundamentals/trends: stockId={}, indicator={}",
                request.getStockId(), request.getIndicator());

        List<IndicatorTrendVO> trends = indicatorService.queryTrend(request);
        return ApiResponse.success(trends);
    }

    // ========== P1 進階 API（TODO） ==========

    /**
     * TODO: P1 - API-M08-002: 查詢綜合評分
     * GET /api/stocks/{stockId}/scores
     */
    // @GetMapping("/stocks/{stockId}/scores")
    // public ApiResponse<FinancialScoreDTO> getScore(...) {
    //     throw new UnsupportedOperationException("P1 功能尚未實作");
    // }

    /**
     * TODO: P1 - API-M08-003: 查詢財務警示
     * GET /api/stocks/{stockId}/alerts
     */
    // @GetMapping("/stocks/{stockId}/alerts")
    // public ApiResponse<List<FinancialAlertDTO>> getAlerts(...) {
    //     throw new UnsupportedOperationException("P1 功能尚未實作");
    // }
}
