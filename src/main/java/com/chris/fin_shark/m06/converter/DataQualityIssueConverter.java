package com.chris.fin_shark.m06.converter;

import com.chris.fin_shark.m06.domain.DataQualityIssue;
import com.chris.fin_shark.m06.dto.DataQualityIssueDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 資料品質問題 Converter
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DataQualityIssueConverter {

    DataQualityIssueDTO toDTO(DataQualityIssue issue);

    List<DataQualityIssueDTO> toDTOList(List<DataQualityIssue> issues);
}
