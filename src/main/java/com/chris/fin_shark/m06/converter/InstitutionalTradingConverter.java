package com.chris.fin_shark.m06.converter;

import com.chris.fin_shark.m06.domain.InstitutionalTrading;
import com.chris.fin_shark.m06.dto.InstitutionalTradingDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 三大法人買賣超 Converter
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InstitutionalTradingConverter {

    /**
     * Entity 轉 DTO
     *
     * @param entity 實體
     * @return DTO
     */
    InstitutionalTradingDTO toDTO(InstitutionalTrading entity);

    /**
     * Entity List 轉 DTO List
     *
     * @param entities 實體列表
     * @return DTO 列表
     */
    List<InstitutionalTradingDTO> toDTOList(List<InstitutionalTrading> entities);

    /**
     * DTO 轉 Entity
     *
     * @param dto DTO
     * @return 實體
     */
    InstitutionalTrading toEntity(InstitutionalTradingDTO dto);

    /**
     * DTO List 轉 Entity List
     *
     * @param dtos DTO 列表
     * @return 實體列表
     */
    List<InstitutionalTrading> toEntityList(List<InstitutionalTradingDTO> dtos);
}
