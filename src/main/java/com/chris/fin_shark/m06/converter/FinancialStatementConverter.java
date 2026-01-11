package com.chris.fin_shark.m06.converter;

import com.chris.fin_shark.m06.domain.FinancialStatement;
import com.chris.fin_shark.m06.dto.FinancialStatementDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 財務報表 Converter
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FinancialStatementConverter {

    /**
     * Entity 轉 DTO
     *
     * @param entity 實體
     * @return DTO
     */
    FinancialStatementDTO toDTO(FinancialStatement entity);

    /**
     * Entity List 轉 DTO List
     *
     * @param entities 實體列表
     * @return DTO 列表
     */
    List<FinancialStatementDTO> toDTOList(List<FinancialStatement> entities);

    /**
     * DTO 轉 Entity
     *
     * @param dto DTO
     * @return 實體
     */
    FinancialStatement toEntity(FinancialStatementDTO dto);

    /**
     * DTO List 轉 Entity List
     *
     * @param dtos DTO 列表
     * @return 實體列表
     */
    List<FinancialStatement> toEntityList(List<FinancialStatementDTO> dtos);
}
