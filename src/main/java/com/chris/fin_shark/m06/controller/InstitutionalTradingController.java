package com.chris.fin_shark.m06.controller;

import com.chris.fin_shark.common.dto.ApiResponse;
import com.chris.fin_shark.m06.converter.InstitutionalTradingConverter;
import com.chris.fin_shark.m06.domain.InstitutionalTrading;
import com.chris.fin_shark.m06.dto.InstitutionalTradingDTO;
import com.chris.fin_shark.m06.repository.InstitutionalTradingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 三大法人買賣超 Controller
 * <p>
 * 功能編號: F-M06-004, F-M06-007
 * 功能名稱: 籌碼資料同步、資料查詢 API
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/institutional")
@Slf4j
@RequiredArgsConstructor
public class InstitutionalTradingController {

    private final InstitutionalTradingRepository institutionalTradingRepository;
    private final InstitutionalTradingConverter institutionalTradingConverter;

    /**
     * 查詢指定股票的法人買賣超
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期（可選）
     * @param endDate   結束日期（可選）
     * @param days      查詢天數（預設 30）
     * @return 法人買賣超列表
     */
    @GetMapping("/{stockId}")
    public ApiResponse<List<InstitutionalTradingDTO>> getInstitutionalTrading(
            @PathVariable String stockId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(defaultValue = "30") Integer days) {

        log.info("GET /api/institutional/{}?startDate={}&endDate={}&days={}",
                stockId, startDate, endDate, days);

        List<InstitutionalTrading> data;

        if (startDate != null && endDate != null) {
            data = institutionalTradingRepository.findByStockIdAndDateRange(stockId, startDate, endDate);
        } else {
            data = institutionalTradingRepository
                    .findByStockIdOrderByTradeDateDesc(stockId, PageRequest.of(0, days))
                    .getContent();
        }

        return ApiResponse.success(institutionalTradingConverter.toDTOList(data));
    }

    /**
     * 查詢最新法人買賣超
     *
     * @param stockId 股票代碼
     * @return 最新法人買賣超
     */
    @GetMapping("/{stockId}/latest")
    public ApiResponse<InstitutionalTradingDTO> getLatestInstitutionalTrading(
            @PathVariable String stockId) {

        log.info("GET /api/institutional/{}/latest", stockId);

        Optional<InstitutionalTrading> latest = institutionalTradingRepository
                .findTopByStockIdOrderByTradeDateDesc(stockId);

        return latest.map(data -> ApiResponse.success(institutionalTradingConverter.toDTO(data)))
                .orElse(ApiResponse.success(null));
    }

    /**
     * 查詢指定日期的全市場法人買賣超
     *
     * @param date 交易日期
     * @return 全市場法人買賣超列表
     */
    @GetMapping("/market/{date}")
    public ApiResponse<List<InstitutionalTradingDTO>> getMarketInstitutionalTrading(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        log.info("GET /api/institutional/market/{}", date);

        List<InstitutionalTrading> data = institutionalTradingRepository.findByTradeDate(date);
        return ApiResponse.success(institutionalTradingConverter.toDTOList(data));
    }
}
