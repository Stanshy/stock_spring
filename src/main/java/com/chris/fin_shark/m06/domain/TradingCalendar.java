package com.chris.fin_shark.m06.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 交易日曆實體
 * <p>
 * 對應資料表: trading_calendar
 * 使用 JPA 管理，包含 PostgreSQL GENERATED COLUMN 映射
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Entity
@Table(name = "trading_calendar")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradingCalendar {

    /** 日曆 ID（自動遞增主鍵） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "calendar_id")
    private Integer calendarId;

    /** 日期（唯一） */
    @Column(name = "calendar_date", nullable = false, unique = true)
    private LocalDate calendarDate;

    /** 是否為交易日 */
    @Column(name = "is_trading_day", nullable = false)
    private Boolean isTradingDay;

    /** 日期類型（TRADING/WEEKEND/HOLIDAY/SPECIAL） */
    @Column(name = "day_type", length = 20)
    private String dayType;

    /** 假日名稱（若為假日） */
    @Column(name = "holiday_name", length = 100)
    private String holidayName;

    /** 年份（GENERATED COLUMN - 由資料庫自動計算） */
    @Column(name = "year", insertable = false, updatable = false)
    private Integer year;

    /** 月份（GENERATED COLUMN - 由資料庫自動計算） */
    @Column(name = "month", insertable = false, updatable = false)
    private Integer month;

    /** 星期幾（GENERATED COLUMN - 0=週日, 1=週一...6=週六） */
    @Column(name = "day_of_week", insertable = false, updatable = false)
    private Integer dayOfWeek;

    /** 建立時間 */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * 新增前自動設定建立時間
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
