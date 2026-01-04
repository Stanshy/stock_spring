package com.chris.fin_shark.m06.controller;

import com.chris.fin_shark.common.dto.ApiResponse;
import com.chris.fin_shark.m06.dto.StockPriceDTO;
import com.chris.fin_shark.m06.dto.InstitutionalTradingDTO;
import com.chris.fin_shark.m06.dto.MarginTradingDTO;
import com.chris.fin_shark.m06.service.MarketDataQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 市場資料查詢 Controller
 * <p>
 * 功能編號: F-M06-007
 * 功能名稱: 資料查詢 API
 * 提供股價、法人買賣、融資融券等市場資料查詢
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/market-data")
@Slf4j
@RequiredArgsConstructor
public class MarketDataQueryController {

    private final MarketDataQueryService marketDataQueryService;

    /**
     * 查詢股票歷史股價
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期（可選）
     * @param endDate   結束日期（可選）
     * @param days      查詢天數（可選，預設 30）
     * @return 股價列表
     */
    @GetMapping("/prices/{stockId}")
    public ApiResponse<List<StockPriceDTO>> getStockPrices(
            @PathVariable String stockId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "30") Integer days) {

        log.info("GET /api/market-data/prices/{}?startDate={}&endDate={}&days={}",
                stockId, startDate, endDate, days);

        List<StockPriceDTO> prices = marketDataQueryService.queryStockPrices(
                stockId, startDate, endDate, days);

        return ApiResponse.success(prices);
    }

    /**
     * 查詢最新股價
     *
     * @param stockId 股票代碼
     * @return 最新股價
     */
    @GetMapping("/prices/{stockId}/latest")
    public ApiResponse<StockPriceDTO> getLatestPrice(@PathVariable String stockId) {
        log.info("GET /api/market-data/prices/{}/latest", stockId);

        StockPriceDTO price = marketDataQueryService.getLatestPrice(stockId);
        return ApiResponse.success(price);
    }

    /**
     * 查詢股價統計資訊（含技術指標）
     *
     * @param stockId 股票代碼
     * @param days    查詢天數（預設 60）
     * @return 股價統計資訊（MA5, MA20, 漲跌幅等）
     */
    @GetMapping("/prices/{stockId}/statistics")
    public ApiResponse<Object> getStockPriceStatistics(
            @PathVariable String stockId,
            @RequestParam(defaultValue = "60") Integer days) {

        log.info("GET /api/market-data/prices/{}/statistics?days={}", stockId, days);

        Object statistics = marketDataQueryService.getStockPriceStatistics(stockId, days);
        return ApiResponse.success(statistics);
    }

    /**
     * 查詢法人買賣超
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期（可選）
     * @param endDate   結束日期（可選）
     * @param days      查詢天數（預設 30）
     * @return 法人買賣超列表
     */
    @GetMapping("/institutional/{stockId}")
    public ApiResponse<List<InstitutionalTradingDTO>> getInstitutionalTrading(
            @PathVariable String stockId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "30") Integer days) {

        log.info("GET /api/market-data/institutional/{}?startDate={}&endDate={}&days={}",
                stockId, startDate, endDate, days);

        List<InstitutionalTradingDTO> data = marketDataQueryService.queryInstitutionalTrading(
                stockId, startDate, endDate, days);

        return ApiResponse.success(data);
    }

    /**
     * 查詢融資融券
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期（可選）
     * @param endDate   結束日期（可選）
     * @param days      查詢天數（預設 30）
     * @return 融資融券列表
     */
    @GetMapping("/margin/{stockId}")
    public ApiResponse<List<MarginTradingDTO>> getMarginTrading(
            @PathVariable String stockId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "30") Integer days) {

        log.info("GET /api/market-data/margin/{}?startDate={}&endDate={}&days={}",
                stockId, startDate, endDate, days);

        List<MarginTradingDTO> data = marketDataQueryService.queryMarginTrading(
                stockId, startDate, endDate, days);

        return ApiResponse.success(data);
    }
}

