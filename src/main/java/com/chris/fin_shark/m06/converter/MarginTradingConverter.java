package com.chris.fin_shark.m06.converter;

import com.chris.fin_shark.m06.domain.MarginTrading;
import com.chris.fin_shark.m06.dto.MarginTradingDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 融資融券 Converter
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MarginTradingConverter {

    /**
     * Entity 轉 DTO
     *
     * @param entity 實體
     * @return DTO
     */
    MarginTradingDTO toDTO(MarginTrading entity);

    /**
     * Entity List 轉 DTO List
     *
     * @param entities 實體列表
     * @return DTO 列表
     */
    List<MarginTradingDTO> toDTOList(List<MarginTrading> entities);

    /**
     * DTO 轉 Entity
     *
     * @param dto DTO
     * @return 實體
     */
    MarginTrading toEntity(MarginTradingDTO dto);

    /**
     * DTO List 轉 Entity List
     *
     * @param dtos DTO 列表
     * @return 實體列表
     */
    List<MarginTrading> toEntityList(List<MarginTradingDTO> dtos);
}
