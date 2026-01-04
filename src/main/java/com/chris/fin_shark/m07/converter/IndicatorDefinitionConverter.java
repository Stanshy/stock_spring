package com.chris.fin_shark.m07.converter;

import com.chris.fin_shark.m07.domain.IndicatorDefinition;
import com.chris.fin_shark.m07.dto.IndicatorDefinitionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 指標定義資料轉換 Mapper
 * <p>
 * 使用 MapStruct 進行 Entity 和 DTO 之間的轉換
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface IndicatorDefinitionConverter {

    /**
     * Entity 轉 DTO
     *
     * @param definition Entity
     * @return DTO
     */
    IndicatorDefinitionDTO toDTO(IndicatorDefinition definition);

    /**
     * Entity 列表轉 DTO 列表
     *
     * @param definitions Entity 列表
     * @return DTO 列表
     */
    List<IndicatorDefinitionDTO> toDTOList(List<IndicatorDefinition> definitions);
}

