package com.chris.fin_shark.m06.converter;

import com.chris.fin_shark.m06.domain.StockPrice;
import com.chris.fin_shark.m06.dto.StockPriceDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 股價 Converter
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StockPriceConverter {

    StockPriceDTO toDTO(StockPrice stockPrice);

    List<StockPriceDTO> toDTOList(List<StockPrice> stockPrices);
}
