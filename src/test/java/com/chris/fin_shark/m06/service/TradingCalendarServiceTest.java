package com.chris.fin_shark.m06.service;

import com.chris.fin_shark.m06.converter.TradingCalendarConverter;
import com.chris.fin_shark.m06.domain.TradingCalendar;
import com.chris.fin_shark.m06.dto.TradingCalendarDTO;
import com.chris.fin_shark.m06.repository.TradingCalendarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TradingCalendarService 單元測試
 * <p>
 * 測試交易日曆服務的核心業務邏輯
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("交易日曆服務測試")
class TradingCalendarServiceTest {

    @Mock
    private TradingCalendarRepository tradingCalendarRepository;

    @Mock
    private TradingCalendarConverter tradingCalendarConverter;

    @InjectMocks
    private TradingCalendarService tradingCalendarService;

    private LocalDate testDate;
    private TradingCalendar testCalendar;
    private TradingCalendarDTO testCalendarDTO;

    @BeforeEach
    void setUp() {
        testDate = LocalDate.of(2024, 12, 24);

        testCalendar = new TradingCalendar();
        testCalendar.setCalendarId(1);
        testCalendar.setCalendarDate(testDate);
        testCalendar.setIsTradingDay(true);
        testCalendar.setDayOfWeek(2); // Tuesday

        testCalendarDTO = TradingCalendarDTO.builder()
                .calendarId(1)
                .calendarDate(testDate)
                .isTradingDay(true)
                .dayOfWeek(2)
                .build();
    }

    @Test
    @DisplayName("getCalendarByDate - 應返回指定日期的交易日曆")
    void getCalendarByDate_shouldReturnCalendarByDate() {
        // Given
        when(tradingCalendarRepository.findByCalendarDate(testDate))
                .thenReturn(Optional.of(testCalendar));
        when(tradingCalendarConverter.toDTO(testCalendar)).thenReturn(testCalendarDTO);

        // When
        TradingCalendarDTO result = tradingCalendarService.getCalendarByDate(testDate);

        // Then
        assertThat(result.getCalendarDate()).isEqualTo(testDate);
        assertThat(result.getIsTradingDay()).isTrue();
        verify(tradingCalendarRepository).findByCalendarDate(testDate);
    }

    @Test
    @DisplayName("getCalendarByDate - 日期不存在時應拋出異常")
    void getCalendarByDate_shouldThrowWhenDateNotFound() {
        // Given
        LocalDate unknownDate = LocalDate.of(2099, 1, 1);
        when(tradingCalendarRepository.findByCalendarDate(unknownDate))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tradingCalendarService.getCalendarByDate(unknownDate))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Calendar date not found");
    }

    @Test
    @DisplayName("isTradingDay - 交易日應返回 true")
    void isTradingDay_shouldReturnTrueForTradingDay() {
        // Given
        when(tradingCalendarRepository.isTradingDay(testDate)).thenReturn(true);

        // When
        boolean result = tradingCalendarService.isTradingDay(testDate);

        // Then
        assertThat(result).isTrue();
        verify(tradingCalendarRepository).isTradingDay(testDate);
    }

    @Test
    @DisplayName("isTradingDay - 非交易日應返回 false")
    void isTradingDay_shouldReturnFalseForNonTradingDay() {
        // Given - 週末
        LocalDate saturday = LocalDate.of(2024, 12, 21);
        when(tradingCalendarRepository.isTradingDay(saturday)).thenReturn(false);

        // When
        boolean result = tradingCalendarService.isTradingDay(saturday);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isTradingDay - 國定假日應返回 false")
    void isTradingDay_shouldReturnFalseForHoliday() {
        // Given - 農曆新年
        LocalDate holiday = LocalDate.of(2025, 1, 29);
        when(tradingCalendarRepository.isTradingDay(holiday)).thenReturn(false);

        // When
        boolean result = tradingCalendarService.isTradingDay(holiday);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("getTradingDaysInRange - 應返回日期範圍內的所有交易日")
    void getTradingDaysInRange_shouldReturnTradingDaysInRange() {
        // Given
        LocalDate startDate = LocalDate.of(2024, 12, 23);
        LocalDate endDate = LocalDate.of(2024, 12, 27);

        TradingCalendar day1 = new TradingCalendar();
        day1.setCalendarDate(LocalDate.of(2024, 12, 23));
        day1.setIsTradingDay(true);

        TradingCalendar day2 = new TradingCalendar();
        day2.setCalendarDate(LocalDate.of(2024, 12, 24));
        day2.setIsTradingDay(true);

        TradingCalendar day3 = new TradingCalendar();
        day3.setCalendarDate(LocalDate.of(2024, 12, 25));
        day3.setIsTradingDay(true);

        List<TradingCalendar> tradingDays = List.of(day1, day2, day3);
        List<TradingCalendarDTO> dtoList = List.of(
                TradingCalendarDTO.builder().calendarDate(day1.getCalendarDate()).isTradingDay(true).build(),
                TradingCalendarDTO.builder().calendarDate(day2.getCalendarDate()).isTradingDay(true).build(),
                TradingCalendarDTO.builder().calendarDate(day3.getCalendarDate()).isTradingDay(true).build()
        );

        when(tradingCalendarRepository.findTradingDaysBetween(startDate, endDate))
                .thenReturn(tradingDays);
        when(tradingCalendarConverter.toDTOList(tradingDays)).thenReturn(dtoList);

        // When
        List<TradingCalendarDTO> result = tradingCalendarService.getTradingDaysInRange(
                startDate, endDate);

        // Then
        assertThat(result).hasSize(3);
        verify(tradingCalendarRepository).findTradingDaysBetween(startDate, endDate);
    }

    @Test
    @DisplayName("getTradingDaysInRange - 無交易日的日期範圍應返回空列表")
    void getTradingDaysInRange_shouldReturnEmptyForNoTradingDays() {
        // Given - 全部都是假日的範圍
        LocalDate startDate = LocalDate.of(2025, 1, 28);
        LocalDate endDate = LocalDate.of(2025, 2, 2);

        when(tradingCalendarRepository.findTradingDaysBetween(startDate, endDate))
                .thenReturn(Collections.emptyList());
        when(tradingCalendarConverter.toDTOList(any())).thenReturn(Collections.emptyList());

        // When
        List<TradingCalendarDTO> result = tradingCalendarService.getTradingDaysInRange(
                startDate, endDate);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getNextTradingDay - 應返回下一個交易日")
    void getNextTradingDay_shouldReturnNextTradingDay() {
        // Given
        LocalDate friday = LocalDate.of(2024, 12, 20);
        TradingCalendar monday = new TradingCalendar();
        monday.setCalendarDate(LocalDate.of(2024, 12, 23));
        monday.setIsTradingDay(true);

        TradingCalendarDTO mondayDTO = TradingCalendarDTO.builder()
                .calendarDate(LocalDate.of(2024, 12, 23))
                .isTradingDay(true)
                .build();

        when(tradingCalendarRepository.findNextTradingDayAfter(friday))
                .thenReturn(Optional.of(monday));
        when(tradingCalendarConverter.toDTO(monday)).thenReturn(mondayDTO);

        // When
        TradingCalendarDTO result = tradingCalendarService.getNextTradingDay(friday);

        // Then
        assertThat(result.getCalendarDate()).isEqualTo(LocalDate.of(2024, 12, 23));
    }

    @Test
    @DisplayName("getNextTradingDay - 無下一個交易日時應拋出異常")
    void getNextTradingDay_shouldThrowWhenNoNextTradingDay() {
        // Given
        LocalDate farFuture = LocalDate.of(2099, 12, 31);
        when(tradingCalendarRepository.findNextTradingDayAfter(farFuture))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tradingCalendarService.getNextTradingDay(farFuture))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No next trading day found");
    }

    @Test
    @DisplayName("getPreviousTradingDay - 應返回上一個交易日")
    void getPreviousTradingDay_shouldReturnPreviousTradingDay() {
        // Given
        LocalDate monday = LocalDate.of(2024, 12, 23);
        TradingCalendar friday = new TradingCalendar();
        friday.setCalendarDate(LocalDate.of(2024, 12, 20));
        friday.setIsTradingDay(true);

        TradingCalendarDTO fridayDTO = TradingCalendarDTO.builder()
                .calendarDate(LocalDate.of(2024, 12, 20))
                .isTradingDay(true)
                .build();

        when(tradingCalendarRepository.findLatestTradingDayBefore(monday))
                .thenReturn(Optional.of(friday));
        when(tradingCalendarConverter.toDTO(friday)).thenReturn(fridayDTO);

        // When
        TradingCalendarDTO result = tradingCalendarService.getPreviousTradingDay(monday);

        // Then
        assertThat(result.getCalendarDate()).isEqualTo(LocalDate.of(2024, 12, 20));
    }

    @Test
    @DisplayName("getPreviousTradingDay - 無上一個交易日時應拋出異常")
    void getPreviousTradingDay_shouldThrowWhenNoPreviousTradingDay() {
        // Given
        LocalDate ancientDate = LocalDate.of(1900, 1, 1);
        when(tradingCalendarRepository.findLatestTradingDayBefore(ancientDate))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tradingCalendarService.getPreviousTradingDay(ancientDate))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("No previous trading day found");
    }

    @Test
    @DisplayName("getTradingDaysByYear - 應返回指定年度的所有交易日")
    void getTradingDaysByYear_shouldReturnTradingDaysByYear() {
        // Given
        int year = 2024;
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        // 假設 2024 年有約 240 個交易日
        List<TradingCalendar> tradingDays = Collections.nCopies(240,
                createTradingCalendar(testDate, true));
        List<TradingCalendarDTO> dtoList = Collections.nCopies(240,
                TradingCalendarDTO.builder().isTradingDay(true).build());

        when(tradingCalendarRepository.findTradingDaysBetween(startDate, endDate))
                .thenReturn(tradingDays);
        when(tradingCalendarConverter.toDTOList(tradingDays)).thenReturn(dtoList);

        // When
        List<TradingCalendarDTO> result = tradingCalendarService.getTradingDaysByYear(year);

        // Then
        assertThat(result).hasSize(240);
        verify(tradingCalendarRepository).findTradingDaysBetween(startDate, endDate);
    }

    private TradingCalendar createTradingCalendar(LocalDate date, boolean isTradingDay) {
        TradingCalendar calendar = new TradingCalendar();
        calendar.setCalendarDate(date);
        calendar.setIsTradingDay(isTradingDay);
        return calendar;
    }
}
