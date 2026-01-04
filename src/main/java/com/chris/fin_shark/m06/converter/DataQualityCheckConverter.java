package com.chris.fin_shark.m06.converter;

import com.chris.fin_shark.m06.domain.DataQualityCheck;
import com.chris.fin_shark.m06.dto.DataQualityCheckDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 資料品質檢核 Converter
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DataQualityCheckConverter {

    DataQualityCheckDTO toDTO(DataQualityCheck check);

    List<DataQualityCheckDTO> toDTOList(List<DataQualityCheck> checks);
}
