package com.chris.fin_shark.common.util;

import com.chris.fin_shark.common.constant.DateConstants;
import lombok.extern.slf4j.Slf4j;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 日期時間工具
 *
 * 遵守總綱 4.4.6 日期時間格式規範
 * 提供交易日判斷、日期轉換等功能
 *
 * @author chris
 * @since 2025-12-24
 */
@Slf4j
public final class DateUtil {

    private DateUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ========================================================================
    // 日期格式化
    // ========================================================================

    /**
     * 將 LocalDate 格式化為字串 (YYYY-MM-DD)
     *
     * @param date 日期
     * @return 格式化字串，例如: "2024-12-24"
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DateConstants.DATE_FORMATTER);
    }

    /**
     * 將 ZonedDateTime 格式化為字串 (含時區)
     *
     * @param dateTime 日期時間
     * @return 格式化字串，例如: "2024-12-24T13:30:00+08:00"
     */
    public static String formatDateTime(ZonedDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateConstants.DATETIME_WITH_ZONE_FORMATTER);
    }

    /**
     * 將字串解析為 LocalDate
     *
     * @param dateStr 日期字串 (YYYY-MM-DD)
     * @return LocalDate，解析失敗返回 null
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }

        try {
            return LocalDate.parse(dateStr, DateConstants.DATE_FORMATTER);
        } catch (Exception e) {
            log.error("Failed to parse date: {}", dateStr, e);
            return null;
        }
    }

    /**
     * 將字串解析為 ZonedDateTime
     *
     * @param dateTimeStr 日期時間字串
     * @return ZonedDateTime，解析失敗返回 null
     */
    public static ZonedDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }

        try {
            return ZonedDateTime.parse(dateTimeStr, DateConstants.DATETIME_WITH_ZONE_FORMATTER);
        } catch (Exception e) {
            log.error("Failed to parse datetime: {}", dateTimeStr, e);
            return null;
        }
    }

    // ========================================================================
    // 時區轉換
    // ========================================================================

    /**
     * 取得台北時區的當前日期時間
     *
     * @return ZonedDateTime (台北時區)
     */
    public static ZonedDateTime nowInTaipei() {
        return ZonedDateTime.now(DateConstants.TAIPEI_ZONE);
    }

    /**
     * 取得台北時區的當前日期
     *
     * @return LocalDate (台北時區)
     */
    public static LocalDate todayInTaipei() {
        return LocalDate.now(DateConstants.TAIPEI_ZONE);
    }

    /**
     * 將 UTC 時間轉換為台北時間
     *
     * @param utcDateTime UTC 時間
     * @return 台北時間
     */
    public static ZonedDateTime utcToTaipei(ZonedDateTime utcDateTime) {
        if (utcDateTime == null) {
            return null;
        }
        return utcDateTime.withZoneSameInstant(DateConstants.TAIPEI_ZONE);
    }

    // ========================================================================
    // 日期計算
    // ========================================================================

    /**
     * 計算兩個日期之間的天數差
     *
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return 天數差（endDate - startDate）
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * 取得日期範圍內的所有日期
     *
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return 日期列表
     */
    public static List<LocalDate> getDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null || startDate.isAfter(endDate)) {
            return Collections.emptyList();
        }

        return Stream.iterate(startDate, date -> date.plusDays(1))
                .limit(ChronoUnit.DAYS.between(startDate, endDate) + 1)
                .collect(Collectors.toList());
    }

    /**
     * 取得日期範圍內的所有日期（字串格式）
     *
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return 日期字串列表
     */
    public static List<String> getDateRangeAsString(LocalDate startDate, LocalDate endDate) {
        return getDateRange(startDate, endDate).stream()
                .map(DateUtil::formatDate)
                .collect(Collectors.toList());
    }

    // ========================================================================
    // 交易日判斷（基礎實作，需要與資料庫的交易日曆表配合）
    // ========================================================================

    /**
     * 判斷是否為週末
     *
     * @param date 日期
     * @return true 週末，false 平日
     */
    public static boolean isWeekend(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    /**
     * 判斷是否為工作日（週一到週五）
     *
     * 注意: 這只是簡單判斷，實際交易日需要考慮國定假日
     * 建議配合資料庫的 trading_calendar 表使用
     *
     * @param date 日期
     * @return true 工作日，false 週末
     *
     * TODO: M06 開發時，需要實作與 trading_calendar 表的整合
     * 真正的交易日判斷應該從資料庫查詢
     */
    public static boolean isWeekday(LocalDate date) {
        return !isWeekend(date);
    }

    /**
     * 取得下一個工作日
     *
     * 注意: 這只是簡單實作（跳過週末），未考慮國定假日
     *
     * @param date 日期
     * @return 下一個工作日
     *
     * TODO: M06 開發時，需要實作與 trading_calendar 表的整合
     */
    public static LocalDate getNextWeekday(LocalDate date) {
        if (date == null) {
            return null;
        }

        LocalDate nextDay = date.plusDays(1);
        while (isWeekend(nextDay)) {
            nextDay = nextDay.plusDays(1);
        }
        return nextDay;
    }

    /**
     * 取得上一個工作日
     *
     * 注意: 這只是簡單實作（跳過週末），未考慮國定假日
     *
     * @param date 日期
     * @return 上一個工作日
     *
     * TODO: M06 開發時，需要實作與 trading_calendar 表的整合
     */
    public static LocalDate getPreviousWeekday(LocalDate date) {
        if (date == null) {
            return null;
        }

        LocalDate prevDay = date.minusDays(1);
        while (isWeekend(prevDay)) {
            prevDay = prevDay.minusDays(1);
        }
        return prevDay;
    }

    /**
     * 取得最近 N 個交易日
     *
     * 注意: 這只是簡單實作（跳過週末），未考慮國定假日
     *
     * @param date 基準日期
     * @param count 天數
     * @return 交易日列表（降序，最新的在前）
     *
     * TODO: M06 開發時，需要實作與 trading_calendar 表的整合
     */
    public static List<LocalDate> getRecentTradingDays(LocalDate date, int count) {
        if (date == null || count <= 0) {
            return Collections.emptyList();
        }

        List<LocalDate> tradingDays = new ArrayList<>();
        LocalDate current = date;

        while (tradingDays.size() < count) {
            if (isWeekday(current)) {
                tradingDays.add(current);
            }
            current = current.minusDays(1);
        }

        return tradingDays;
    }

    // ========================================================================
    // 盤中時間判斷（台股）
    // ========================================================================

    /**
     * 判斷當前是否為交易時間
     *
     * 台股交易時間: 09:00 - 13:30
     *
     * @return true 交易時間內，false 交易時間外
     */
    public static boolean isMarketOpen() {
        return isMarketOpen(nowInTaipei());
    }

    /**
     * 判斷指定時間是否為交易時間
     *
     * @param dateTime 日期時間
     * @return true 交易時間內，false 交易時間外
     */
    public static boolean isMarketOpen(ZonedDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }

        LocalTime time = dateTime.toLocalTime();
        LocalTime marketOpen = LocalTime.parse(DateConstants.MARKET_OPEN);
        LocalTime marketClose = LocalTime.parse(DateConstants.MARKET_CLOSE);

        return !time.isBefore(marketOpen) && !time.isAfter(marketClose);
    }

    /**
     * 判斷當前是否為盤後時間
     *
     * 盤後時間: 14:00 - 14:30
     *
     * @return true 盤後時間，false 非盤後時間
     */
    public static boolean isAfterMarket() {
        return isAfterMarket(nowInTaipei());
    }

    /**
     * 判斷指定時間是否為盤後時間
     *
     * @param dateTime 日期時間
     * @return true 盤後時間，false 非盤後時間
     */
    public static boolean isAfterMarket(ZonedDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }

        LocalTime time = dateTime.toLocalTime();
        LocalTime afterOpen = LocalTime.parse(DateConstants.AFTER_MARKET_OPEN);
        LocalTime afterClose = LocalTime.parse(DateConstants.AFTER_MARKET_CLOSE);

        return !time.isBefore(afterOpen) && !time.isAfter(afterClose);
    }

    // ========================================================================
    // 季度計算（用於財報資料）
    // ========================================================================

    /**
     * 取得指定日期所屬的季度
     *
     * @param date 日期
     * @return 季度 (1-4)
     */
    public static int getQuarter(LocalDate date) {
        if (date == null) {
            return 0;
        }
        return (date.getMonthValue() - 1) / 3 + 1;
    }

    /**
     * 取得季度字串表示
     *
     * @param date 日期
     * @return 季度字串，例如: "Q1", "Q2", "Q3", "Q4"
     */
    public static String getQuarterString(LocalDate date) {
        if (date == null) {
            return null;
        }
        return "Q" + getQuarter(date);
    }

    /**
     * 取得上一季度
     *
     * @param year 年份
     * @param quarter 季度 (1-4)
     * @return [年份, 季度]
     */
    public static int[] getPreviousQuarter(int year, int quarter) {
        if (quarter < 1 || quarter > 4) {
            throw new IllegalArgumentException("Quarter must be between 1 and 4");
        }

        if (quarter == 1) {
            return new int[]{year - 1, 4};
        } else {
            return new int[]{year, quarter - 1};
        }
    }
}
