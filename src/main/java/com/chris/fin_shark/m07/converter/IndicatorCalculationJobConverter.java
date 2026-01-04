package com.chris.fin_shark.m07.converter;

import com.chris.fin_shark.m07.domain.IndicatorCalculationJob;
import com.chris.fin_shark.m07.dto.IndicatorCalculationJobDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

/**
 * 指標計算 Job 轉換器
 * <p>
 * 使用 MapStruct 自動生成轉換邏輯
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface IndicatorCalculationJobConverter {

    /**
     * Entity → DTO
     *
     * @param entity IndicatorCalculationJob Entity
     * @return IndicatorCalculationJobDTO
     */
    IndicatorCalculationJobDTO toDTO(IndicatorCalculationJob entity);

    /**
     * DTO → Entity
     *
     * @param dto IndicatorCalculationJobDTO
     * @return IndicatorCalculationJob Entity
     */
    IndicatorCalculationJob toEntity(IndicatorCalculationJobDTO dto);

    /**
     * Entity List → DTO List
     *
     * @param entities Entity 列表
     * @return DTO 列表
     */
    List<IndicatorCalculationJobDTO> toDTOList(List<IndicatorCalculationJob> entities);

    /**
     * DTO List → Entity List
     *
     * @param dtos DTO 列表
     * @return Entity 列表
     */
    List<IndicatorCalculationJob> toEntityList(List<IndicatorCalculationJobDTO> dtos);
}