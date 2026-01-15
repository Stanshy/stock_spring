package com.chris.fin_shark.m09.converter;

import com.chris.fin_shark.m09.domain.ChipSignalEntity;
import com.chris.fin_shark.m09.dto.ChipSignalDTO;
import com.chris.fin_shark.m09.engine.model.ChipSignal;
import com.chris.fin_shark.m09.enums.SignalSeverity;
import org.mapstruct.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 籌碼訊號 Converter
 * <p>
 * 使用 MapStruct 進行 Entity、DTO、Engine Model 之間的轉換。
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChipSignalConverter {

    // ========== Entity ↔ DTO ==========

    /**
     * Entity → DTO
     */
    ChipSignalDTO toDTO(ChipSignalEntity entity);

    /**
     * Entity List → DTO List
     */
    List<ChipSignalDTO> toDTOList(List<ChipSignalEntity> entities);

    // ========== ChipSignal (Engine) → Entity ==========

    /**
     * 計算引擎訊號 → Entity
     *
     * @param signal     訊號模型
     * @param stockId    股票代碼
     * @param tradeDate  交易日期
     * @return Entity
     */
    @Mapping(target = "signalId", ignore = true)
    @Mapping(source = "signal.signalCode", target = "signalCode")
    @Mapping(source = "signal.signalName", target = "signalName")
    @Mapping(source = "signal.signalType", target = "signalType")
    @Mapping(source = "signal.severity", target = "severity", qualifiedByName = "severityToString")
    @Mapping(source = "signal.signalValue", target = "signalValue")
    @Mapping(source = "signal.thresholdValue", target = "thresholdValue")
    @Mapping(source = "signal.deviation", target = "deviation")
    @Mapping(source = "signal.description", target = "description")
    @Mapping(source = "signal.recommendation", target = "recommendation")
    @Mapping(source = "stockId", target = "stockId")
    @Mapping(source = "tradeDate", target = "tradeDate")
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "acknowledgedAt", ignore = true)
    @Mapping(target = "acknowledgedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ChipSignalEntity toEntity(ChipSignal signal, String stockId, LocalDate tradeDate);

    /**
     * 批次轉換訊號列表
     *
     * @param signals    訊號列表
     * @param stockId    股票代碼
     * @param tradeDate  交易日期
     * @return Entity 列表
     */
    default List<ChipSignalEntity> toEntityList(List<ChipSignal> signals, String stockId, LocalDate tradeDate) {
        if (signals == null) {
            return null;
        }
        return signals.stream()
                .map(signal -> toEntity(signal, stockId, tradeDate))
                .toList();
    }

    // ========== 自定義映射方法 ==========

    /**
     * SignalSeverity → String
     */
    @Named("severityToString")
    default String severityToString(SignalSeverity severity) {
        return severity != null ? severity.name() : null;
    }

    /**
     * String → SignalSeverity
     */
    @Named("stringToSeverity")
    default SignalSeverity stringToSeverity(String severity) {
        if (severity == null || severity.isBlank()) {
            return null;
        }
        try {
            return SignalSeverity.valueOf(severity);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
