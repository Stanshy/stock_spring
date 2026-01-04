package com.chris.fin_shark.m06.repository;

import com.chris.fin_shark.m06.domain.TradingCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 交易日曆 Repository
 * <p>
 * 功能編號: F-M06-005
 * 功能名稱: 交易日曆管理
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Repository
public interface TradingCalendarRepository extends JpaRepository<TradingCalendar, Integer> {

    /**
     * 根據日期查詢
     *
     * @param date 日期
     * @return 交易日曆
     */
    Optional<TradingCalendar> findByCalendarDate(LocalDate date);

    /**
     * 查詢所有交易日
     *
     * @return 交易日列表
     */
    List<TradingCalendar> findByIsTradingDayTrue();

    /**
     * 查詢指定日期範圍的交易日
     *
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 交易日列表
     */
    @Query("SELECT tc FROM TradingCalendar tc WHERE tc.calendarDate BETWEEN :startDate AND :endDate AND tc.isTradingDay = true ORDER BY tc.calendarDate")
    List<TradingCalendar> findTradingDaysBetween(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    /**
     * 查詢最近的交易日
     *
     * @param date 參考日期
     * @return 最近的交易日
     */
    @Query("SELECT tc FROM TradingCalendar tc WHERE tc.calendarDate <= :date AND tc.isTradingDay = true ORDER BY tc.calendarDate DESC LIMIT 1")
    Optional<TradingCalendar> findLatestTradingDayBefore(@Param("date") LocalDate date);

    /**
     * 查詢下一個交易日
     *
     * @param date 參考日期
     * @return 下一個交易日
     */
    @Query("SELECT tc FROM TradingCalendar tc WHERE tc.calendarDate > :date AND tc.isTradingDay = true ORDER BY tc.calendarDate ASC LIMIT 1")
    Optional<TradingCalendar> findNextTradingDayAfter(@Param("date") LocalDate date);

    /**
     * 檢查日期是否為交易日
     *
     * @param date 日期
     * @return 是否為交易日
     */
    @Query("SELECT CASE WHEN COUNT(tc) > 0 THEN true ELSE false END FROM TradingCalendar tc WHERE tc.calendarDate = :date AND tc.isTradingDay = true")
    boolean isTradingDay(@Param("date") LocalDate date);
}
