package com.chris.fin_shark.m06.converter;

import com.chris.fin_shark.m06.domain.TradingCalendar;
import com.chris.fin_shark.m06.dto.TradingCalendarDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 交易日曆 Converter
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TradingCalendarConverter {

    TradingCalendarDTO toDTO(TradingCalendar tradingCalendar);

    List<TradingCalendarDTO> toDTOList(List<TradingCalendar> tradingCalendars);
}
