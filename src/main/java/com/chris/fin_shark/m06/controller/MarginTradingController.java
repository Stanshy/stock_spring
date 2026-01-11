package com.chris.fin_shark.m06.controller;

import com.chris.fin_shark.common.dto.ApiResponse;
import com.chris.fin_shark.m06.converter.MarginTradingConverter;
import com.chris.fin_shark.m06.domain.MarginTrading;
import com.chris.fin_shark.m06.dto.MarginTradingDTO;
import com.chris.fin_shark.m06.repository.MarginTradingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 融資融券 Controller
 * <p>
 * 功能編號: F-M06-004, F-M06-007
 * 功能名稱: 籌碼資料同步、資料查詢 API
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/margin")
@Slf4j
@RequiredArgsConstructor
public class MarginTradingController {

    private final MarginTradingRepository marginTradingRepository;
    private final MarginTradingConverter marginTradingConverter;

    /**
     * 查詢指定股票的融資融券
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期（可選）
     * @param endDate   結束日期（可選）
     * @param days      查詢天數（預設 30）
     * @return 融資融券列表
     */
    @GetMapping("/{stockId}")
    public ApiResponse<List<MarginTradingDTO>> getMarginTrading(
            @PathVariable String stockId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "30") Integer days) {

        log.info("GET /api/margin/{}?startDate={}&endDate={}&days={}",
                stockId, startDate, endDate, days);

        List<MarginTrading> data;

        if (startDate != null && endDate != null) {
            data = marginTradingRepository.findByStockIdAndDateRange(stockId, startDate, endDate);
        } else {
            data = marginTradingRepository
                    .findByStockIdOrderByTradeDateDesc(stockId, PageRequest.of(0, days))
                    .getContent();
        }

        return ApiResponse.success(marginTradingConverter.toDTOList(data));
    }

    /**
     * 查詢最新融資融券
     *
     * @param stockId 股票代碼
     * @return 最新融資融券
     */
    @GetMapping("/{stockId}/latest")
    public ApiResponse<MarginTradingDTO> getLatestMarginTrading(
            @PathVariable String stockId) {

        log.info("GET /api/margin/{}/latest", stockId);

        Optional<MarginTrading> latest = marginTradingRepository
                .findTopByStockIdOrderByTradeDateDesc(stockId);

        return latest.map(data -> ApiResponse.success(marginTradingConverter.toDTO(data)))
                .orElse(ApiResponse.success(null));
    }

    /**
     * 查詢指定日期的全市場融資融券
     *
     * @param date 交易日期
     * @return 全市場融資融券列表
     */
    @GetMapping("/market/{date}")
    public ApiResponse<List<MarginTradingDTO>> getMarketMarginTrading(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        log.info("GET /api/margin/market/{}", date);

        List<MarginTrading> data = marginTradingRepository.findByTradeDate(date);
        return ApiResponse.success(marginTradingConverter.toDTOList(data));
    }
}
