package com.chris.fin_shark.m06.controller;

import com.chris.fin_shark.common.dto.ApiResponse;
import com.chris.fin_shark.m06.dto.TradingCalendarDTO;
import com.chris.fin_shark.m06.service.TradingCalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 交易日曆 Controller
 * <p>
 * 功能編號: F-M06-005
 * 功能名稱: 交易日曆管理
 * 提供交易日查詢、驗證等功能
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/trading-calendar")
@Slf4j
@RequiredArgsConstructor
public class TradingCalendarController {

    private final TradingCalendarService tradingCalendarService;

    /**
     * 查詢指定日期的交易日曆
     *
     * @param date 日期
     * @return 交易日曆資訊
     */
    @GetMapping("/{date}")
    public ApiResponse<TradingCalendarDTO> getCalendarByDate(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        log.info("GET /api/trading-calendar/{}", date);

        TradingCalendarDTO calendar = tradingCalendarService.getCalendarByDate(date);
        return ApiResponse.success(calendar);
    }

    /**
     * 檢查指定日期是否為交易日
     *
     * @param date 日期
     * @return 是否為交易日
     */
    @GetMapping("/is-trading-day/{date}")
    public ApiResponse<Boolean> isTradingDay(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        log.info("GET /api/trading-calendar/is-trading-day/{}", date);

        boolean isTradingDay = tradingCalendarService.isTradingDay(date);
        return ApiResponse.success(isTradingDay);
    }

    /**
     * 查詢日期範圍內的交易日
     *
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 交易日列表
     */
    @GetMapping("/range")
    public ApiResponse<List<TradingCalendarDTO>> getTradingDaysInRange(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        log.info("GET /api/trading-calendar/range?startDate={}&endDate={}", startDate, endDate);

        List<TradingCalendarDTO> tradingDays = tradingCalendarService.getTradingDaysInRange(
                startDate, endDate);

        return ApiResponse.success(tradingDays);
    }

    /**
     * 查詢下一個交易日
     *
     * @param date 參考日期（可選，預設為今天）
     * @return 下一個交易日
     */
    @GetMapping("/next-trading-day")
    public ApiResponse<TradingCalendarDTO> getNextTradingDay(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        LocalDate referenceDate = date != null ? date : LocalDate.now();
        log.info("GET /api/trading-calendar/next-trading-day?date={}", referenceDate);

        TradingCalendarDTO nextTradingDay = tradingCalendarService.getNextTradingDay(referenceDate);
        return ApiResponse.success(nextTradingDay);
    }

    /**
     * 查詢上一個交易日
     *
     * @param date 參考日期（可選，預設為今天）
     * @return 上一個交易日
     */
    @GetMapping("/previous-trading-day")
    public ApiResponse<TradingCalendarDTO> getPreviousTradingDay(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        LocalDate referenceDate = date != null ? date : LocalDate.now();
        log.info("GET /api/trading-calendar/previous-trading-day?date={}", referenceDate);

        TradingCalendarDTO previousTradingDay = tradingCalendarService.getPreviousTradingDay(referenceDate);
        return ApiResponse.success(previousTradingDay);
    }

    /**
     * 查詢所有交易日（當年）
     *
     * @param year 年份（可選，預設當年）
     * @return 交易日列表
     */
    @GetMapping("/year")
    public ApiResponse<List<TradingCalendarDTO>> getTradingDaysByYear(
            @RequestParam(required = false) Integer year) {

        int targetYear = year != null ? year : LocalDate.now().getYear();
        log.info("GET /api/trading-calendar/year?year={}", targetYear);

        List<TradingCalendarDTO> tradingDays = tradingCalendarService.getTradingDaysByYear(targetYear);
        return ApiResponse.success(tradingDays);
    }
}

