package com.chris.fin_shark.m06.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 交易日曆資料傳輸物件
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradingCalendarDTO {

    /** 日曆 ID */
    @JsonProperty("calendar_id")
    private Integer calendarId;

    /** 日期 */
    @JsonProperty("calendar_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate calendarDate;

    /** 是否為交易日 */
    @JsonProperty("is_trading_day")
    private Boolean isTradingDay;

    /** 日期類型 */
    @JsonProperty("day_type")
    private String dayType;

    /** 假日名稱 */
    @JsonProperty("holiday_name")
    private String holidayName;

    /** 年份 */
    @JsonProperty("year")
    private Integer year;

    /** 月份 */
    @JsonProperty("month")
    private Integer month;

    /** 星期幾（0=週日, 1=週一...6=週六） */
    @JsonProperty("day_of_week")
    private Integer dayOfWeek;
}
