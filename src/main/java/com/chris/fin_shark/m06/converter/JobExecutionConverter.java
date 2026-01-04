package com.chris.fin_shark.m06.converter;

import com.chris.fin_shark.common.domain.JobExecution;
import com.chris.fin_shark.m06.dto.JobExecutionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * Job 執行記錄 Converter
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JobExecutionConverter {

    JobExecutionDTO toDTO(JobExecution execution);

    List<JobExecutionDTO> toDTOList(List<JobExecution> executions);
}
