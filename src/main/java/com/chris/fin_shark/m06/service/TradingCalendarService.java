package com.chris.fin_shark.m06.service;

import com.chris.fin_shark.m06.converter.TradingCalendarConverter;
import com.chris.fin_shark.m06.domain.TradingCalendar;
import com.chris.fin_shark.m06.dto.TradingCalendarDTO;
import com.chris.fin_shark.m06.repository.TradingCalendarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 交易日曆服務
 * <p>
 * 功能編號: F-M06-005
 * 功能名稱: 交易日曆管理
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TradingCalendarService {

    private final TradingCalendarRepository tradingCalendarRepository;
    private final TradingCalendarConverter tradingCalendarConverter;

    /**
     * 根據日期查詢交易日曆
     *
     * @param date 日期
     * @return 交易日曆
     */
    @Transactional(readOnly = true)
    public TradingCalendarDTO getCalendarByDate(LocalDate date) {
        log.debug("查詢交易日曆: date={}", date);

        TradingCalendar calendar = tradingCalendarRepository.findByCalendarDate(date)
                .orElseThrow(() -> new RuntimeException("Calendar date not found: " + date));

        return tradingCalendarConverter.toDTO(calendar);
    }

    /**
     * 檢查是否為交易日
     *
     * @param date 日期
     * @return 是否為交易日
     */
    @Transactional(readOnly = true)
    public boolean isTradingDay(LocalDate date) {
        log.debug("檢查是否為交易日: date={}", date);

        return tradingCalendarRepository.isTradingDay(date);
    }

    /**
     * 查詢日期範圍內的交易日
     *
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 交易日列表
     */
    @Transactional(readOnly = true)
    public List<TradingCalendarDTO> getTradingDaysInRange(LocalDate startDate, LocalDate endDate) {
        log.debug("查詢交易日範圍: startDate={}, endDate={}", startDate, endDate);

        List<TradingCalendar> tradingDays = tradingCalendarRepository.findTradingDaysBetween(
                startDate, endDate);

        return tradingCalendarConverter.toDTOList(tradingDays);
    }

    /**
     * 查詢下一個交易日
     *
     * @param date 參考日期
     * @return 下一個交易日
     */
    @Transactional(readOnly = true)
    public TradingCalendarDTO getNextTradingDay(LocalDate date) {
        log.debug("查詢下一交易日: date={}", date);

        TradingCalendar nextDay = tradingCalendarRepository.findNextTradingDayAfter(date)
                .orElseThrow(() -> new RuntimeException("No next trading day found after: " + date));

        return tradingCalendarConverter.toDTO(nextDay);
    }

    /**
     * 查詢上一個交易日
     *
     * @param date 參考日期
     * @return 上一個交易日
     */
    @Transactional(readOnly = true)
    public TradingCalendarDTO getPreviousTradingDay(LocalDate date) {
        log.debug("查詢上一交易日: date={}", date);

        TradingCalendar previousDay = tradingCalendarRepository.findLatestTradingDayBefore(date)
                .orElseThrow(() -> new RuntimeException("No previous trading day found before: " + date));

        return tradingCalendarConverter.toDTO(previousDay);
    }

    /**
     * 查詢指定年份的所有交易日
     *
     * @param year 年份
     * @return 交易日列表
     */
    @Transactional(readOnly = true)
    public List<TradingCalendarDTO> getTradingDaysByYear(int year) {
        log.debug("查詢年度交易日: year={}", year);

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<TradingCalendar> tradingDays = tradingCalendarRepository.findTradingDaysBetween(
                startDate, endDate);

        return tradingCalendarConverter.toDTOList(tradingDays);
    }
}

