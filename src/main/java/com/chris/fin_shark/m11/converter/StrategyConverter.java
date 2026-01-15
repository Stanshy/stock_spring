package com.chris.fin_shark.m11.converter;

import com.chris.fin_shark.m11.domain.Strategy;
import com.chris.fin_shark.m11.domain.StrategyExecution;
import com.chris.fin_shark.m11.domain.StrategySignal;
import com.chris.fin_shark.m11.dto.StrategyDTO;
import com.chris.fin_shark.m11.dto.StrategyExecutionDTO;
import com.chris.fin_shark.m11.dto.StrategySignalDTO;
import com.chris.fin_shark.m11.dto.StrategyStatisticsDTO;
import com.chris.fin_shark.m11.dto.request.StrategyCreateRequest;
import com.chris.fin_shark.m11.enums.StrategyStatus;
import com.chris.fin_shark.m11.enums.StrategyType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 策略轉換器
 *
 * @author chris
 * @since 1.0.0
 */
@Component
public class StrategyConverter {

    /**
     * Entity → DTO（列表用）
     */
    public StrategyDTO toDTO(Strategy entity) {
        if (entity == null) return null;

        return StrategyDTO.builder()
                .strategyId(entity.getStrategyId())
                .strategyName(entity.getStrategyName())
                .strategyType(entity.getStrategyType())
                .description(entity.getDescription())
                .version(entity.getCurrentVersion())
                .status(entity.getStatus())
                .isPreset(entity.getIsPreset())
                .conditionCount(countConditions(entity.getConditions()))
                .lastExecution(entity.getLastExecutionAt())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Entity → DTO（詳情用，包含 conditions）
     */
    public StrategyDTO toDetailDTO(Strategy entity) {
        if (entity == null) return null;

        StrategyDTO dto = toDTO(entity);
        dto.setConditions(entity.getConditions());
        dto.setParameters(entity.getParameters());
        dto.setOutputConfig(entity.getOutputConfig());

        // 設置統計資訊
        if (entity.getTotalExecutions() != null && entity.getTotalExecutions() > 0) {
            BigDecimal avgSignals = BigDecimal.valueOf(entity.getTotalSignals())
                    .divide(BigDecimal.valueOf(entity.getTotalExecutions()), 2, RoundingMode.HALF_UP);

            dto.setStatistics(StrategyStatisticsDTO.builder()
                    .totalExecutions(entity.getTotalExecutions())
                    .totalSignals(entity.getTotalSignals())
                    .avgSignalsPerExecution(avgSignals)
                    .build());
        }

        return dto;
    }

    /**
     * 建立請求 → Entity
     */
    public Strategy toEntity(StrategyCreateRequest request) {
        if (request == null) return null;

        return Strategy.builder()
                .strategyName(request.getStrategyName())
                .strategyType(StrategyType.valueOf(request.getStrategyType()))
                .description(request.getDescription())
                .conditions(request.getConditions())
                .parameters(request.getParameters())
                .outputConfig(request.getOutput())
                .status(StrategyStatus.DRAFT)
                .currentVersion(1)
                .isPreset(false)
                .totalExecutions(0)
                .totalSignals(0)
                .build();
    }

    /**
     * 執行記錄 Entity → DTO
     */
    public StrategyExecutionDTO toExecutionDTO(StrategyExecution entity) {
        if (entity == null) return null;

        return StrategyExecutionDTO.builder()
                .executionId(entity.getExecutionId())
                .strategyId(entity.getStrategyId())
                .strategyVersion(entity.getStrategyVersion())
                .executionDate(entity.getExecutionDate())
                .executionType(entity.getExecutionType())
                .stocksEvaluated(entity.getStocksEvaluated())
                .signalsGenerated(entity.getSignalsGenerated())
                .buySignals(entity.getBuySignals())
                .sellSignals(entity.getSellSignals())
                .avgConfidence(entity.getAvgConfidence())
                .executionTimeMs(entity.getExecutionTimeMs())
                .status(entity.getStatus())
                .executedAt(entity.getStartedAt())
                .diagnostics(entity.getDiagnostics())
                .build();
    }

    /**
     * 信號 Entity → DTO
     */
    public StrategySignalDTO toSignalDTO(StrategySignal entity) {
        if (entity == null) return null;

        return StrategySignalDTO.builder()
                .signalId(entity.getSignalId())
                .executionId(entity.getExecutionId())
                .strategyId(entity.getStrategyId())
                .strategyVersion(entity.getStrategyVersion())
                .stockId(entity.getStockId())
                .tradeDate(entity.getTradeDate())
                .signalType(entity.getSignalType())
                .confidenceScore(entity.getConfidenceScore())
                .factorValues(entity.getFactorValues())
                .build();
    }

    /**
     * 批次轉換
     */
    public List<StrategyDTO> toDTOList(List<Strategy> entities) {
        if (entities == null) return null;
        return entities.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public List<StrategyExecutionDTO> toExecutionDTOList(List<StrategyExecution> entities) {
        if (entities == null) return null;
        return entities.stream().map(this::toExecutionDTO).collect(Collectors.toList());
    }

    public List<StrategySignalDTO> toSignalDTOList(List<StrategySignal> entities) {
        if (entities == null) return null;
        return entities.stream().map(this::toSignalDTO).collect(Collectors.toList());
    }

    /**
     * 計算條件數量
     */
    @SuppressWarnings("unchecked")
    private Integer countConditions(Map<String, Object> conditions) {
        if (conditions == null) return 0;

        Object conditionsList = conditions.get("conditions");
        if (conditionsList instanceof List) {
            return ((List<?>) conditionsList).size();
        }
        return 0;
    }
}
