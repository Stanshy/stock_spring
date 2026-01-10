package com.chris.fin_shark.m08.converter;

import com.chris.fin_shark.m08.domain.FinancialAlert;
import com.chris.fin_shark.m08.dto.FinancialAlertDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 財務異常警示 Converter
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FinancialAlertConverter {

    /**
     * Entity → DTO
     */
    @Mapping(target = "stockName", ignore = true)
    FinancialAlertDTO toDTO(FinancialAlert entity);

    /**
     * Entity List → DTO List
     */
    List<FinancialAlertDTO> toDTOList(List<FinancialAlert> entities);
}
