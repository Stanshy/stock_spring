package com.chris.fin_shark.m07.converter;

import com.chris.fin_shark.m07.domain.TechnicalIndicator;
import com.chris.fin_shark.m07.dto.TechnicalIndicatorDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 技術指標資料轉換 Mapper
 * <p>
 * 使用 MapStruct 進行 Entity 和 DTO 之間的轉換
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TechnicalIndicatorConverter {

    /**
     * Entity 轉 DTO
     *
     * @param indicator Entity
     * @return DTO
     */
    TechnicalIndicatorDTO toDTO(TechnicalIndicator indicator);

    /**
     * Entity 列表轉 DTO 列表
     *
     * @param indicators Entity 列表
     * @return DTO 列表
     */
    List<TechnicalIndicatorDTO> toDTOList(List<TechnicalIndicator> indicators);
}
