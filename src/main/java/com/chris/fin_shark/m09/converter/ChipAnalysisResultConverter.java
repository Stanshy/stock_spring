package com.chris.fin_shark.m09.converter;

import com.chris.fin_shark.m09.domain.ChipAnalysisResult;
import com.chris.fin_shark.m09.dto.ChipAnalysisResultDTO;
import com.chris.fin_shark.m09.engine.ChipResult;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 籌碼分析結果 Converter
 * <p>
 * 使用 MapStruct 進行 Entity、DTO、Engine Model 之間的轉換。
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ChipAnalysisResultConverter {

    // ========== Entity ↔ DTO ==========

    /**
     * Entity → DTO
     */
    ChipAnalysisResultDTO toDTO(ChipAnalysisResult entity);

    /**
     * Entity List → DTO List
     */
    List<ChipAnalysisResultDTO> toDTOList(List<ChipAnalysisResult> entities);

    // ========== ChipResult (Engine) → Entity ==========

    /**
     * 計算結果 → Entity
     * <p>
     * 需要自定義映射方法從 Map 中提取核心指標欄位。
     * </p>
     *
     * @param result 計算結果
     * @return Entity
     */
    @Mapping(target = "resultId", ignore = true)
    @Mapping(source = "calculationDate", target = "tradeDate")
    @Mapping(source = "institutionalIndicators", target = "foreignNet", qualifiedByName = "extractForeignNet")
    @Mapping(source = "institutionalIndicators", target = "foreignNetMa5", qualifiedByName = "extractForeignNetMa5")
    @Mapping(source = "institutionalIndicators", target = "foreignNetMa20", qualifiedByName = "extractForeignNetMa20")
    @Mapping(source = "institutionalIndicators", target = "foreignContinuousDays", qualifiedByName = "extractForeignContinuousDays")
    @Mapping(source = "institutionalIndicators", target = "foreignAccumulated20d", qualifiedByName = "extractForeignAccumulated20d")
    @Mapping(source = "institutionalIndicators", target = "trustNet", qualifiedByName = "extractTrustNet")
    @Mapping(source = "institutionalIndicators", target = "trustNetMa5", qualifiedByName = "extractTrustNetMa5")
    @Mapping(source = "institutionalIndicators", target = "trustContinuousDays", qualifiedByName = "extractTrustContinuousDays")
    @Mapping(source = "institutionalIndicators", target = "dealerNet", qualifiedByName = "extractDealerNet")
    @Mapping(source = "institutionalIndicators", target = "totalNet", qualifiedByName = "extractTotalNet")
    @Mapping(source = "marginIndicators", target = "marginBalance", qualifiedByName = "extractMarginBalance")
    @Mapping(source = "marginIndicators", target = "marginChange", qualifiedByName = "extractMarginChange")
    @Mapping(source = "marginIndicators", target = "marginUsageRate", qualifiedByName = "extractMarginUsageRate")
    @Mapping(source = "marginIndicators", target = "marginContinuousDays", qualifiedByName = "extractMarginContinuousDays")
    @Mapping(source = "marginIndicators", target = "shortBalance", qualifiedByName = "extractShortBalance")
    @Mapping(source = "marginIndicators", target = "shortChange", qualifiedByName = "extractShortChange")
    @Mapping(source = "marginIndicators", target = "marginShortRatio", qualifiedByName = "extractMarginShortRatio")
    @Mapping(source = "concentrationIndicators", target = "institutionalRatio", qualifiedByName = "extractInstitutionalRatio")
    @Mapping(source = "concentrationIndicators", target = "concentrationTrend", qualifiedByName = "extractConcentrationTrend")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "calculationTimeMs", ignore = true)
    ChipAnalysisResult toEntity(ChipResult result);

    // ========== 三大法人指標提取方法 ==========

    @Named("extractForeignNet")
    default Long extractForeignNet(Map<String, Object> indicators) {
        return extractLong(indicators, "foreign_net");
    }

    @Named("extractForeignNetMa5")
    default BigDecimal extractForeignNetMa5(Map<String, Object> indicators) {
        return extractBigDecimal(indicators, "foreign_net_ma5");
    }

    @Named("extractForeignNetMa20")
    default BigDecimal extractForeignNetMa20(Map<String, Object> indicators) {
        return extractBigDecimal(indicators, "foreign_net_ma20");
    }

    @Named("extractForeignContinuousDays")
    default Integer extractForeignContinuousDays(Map<String, Object> indicators) {
        return extractInteger(indicators, "foreign_continuous_days");
    }

    @Named("extractForeignAccumulated20d")
    default Long extractForeignAccumulated20d(Map<String, Object> indicators) {
        return extractLong(indicators, "foreign_accumulated_20d");
    }

    @Named("extractTrustNet")
    default Long extractTrustNet(Map<String, Object> indicators) {
        return extractLong(indicators, "trust_net");
    }

    @Named("extractTrustNetMa5")
    default BigDecimal extractTrustNetMa5(Map<String, Object> indicators) {
        return extractBigDecimal(indicators, "trust_net_ma5");
    }

    @Named("extractTrustContinuousDays")
    default Integer extractTrustContinuousDays(Map<String, Object> indicators) {
        return extractInteger(indicators, "trust_continuous_days");
    }

    @Named("extractDealerNet")
    default Long extractDealerNet(Map<String, Object> indicators) {
        return extractLong(indicators, "dealer_net");
    }

    @Named("extractTotalNet")
    default Long extractTotalNet(Map<String, Object> indicators) {
        return extractLong(indicators, "total_net");
    }

    // ========== 融資融券指標提取方法 ==========

    @Named("extractMarginBalance")
    default Long extractMarginBalance(Map<String, Object> indicators) {
        return extractLong(indicators, "margin_balance");
    }

    @Named("extractMarginChange")
    default Long extractMarginChange(Map<String, Object> indicators) {
        return extractLong(indicators, "margin_change");
    }

    @Named("extractMarginUsageRate")
    default BigDecimal extractMarginUsageRate(Map<String, Object> indicators) {
        return extractBigDecimal(indicators, "margin_usage_rate");
    }

    @Named("extractMarginContinuousDays")
    default Integer extractMarginContinuousDays(Map<String, Object> indicators) {
        return extractInteger(indicators, "margin_continuous_days");
    }

    @Named("extractShortBalance")
    default Long extractShortBalance(Map<String, Object> indicators) {
        return extractLong(indicators, "short_balance");
    }

    @Named("extractShortChange")
    default Long extractShortChange(Map<String, Object> indicators) {
        return extractLong(indicators, "short_change");
    }

    @Named("extractMarginShortRatio")
    default BigDecimal extractMarginShortRatio(Map<String, Object> indicators) {
        return extractBigDecimal(indicators, "margin_short_ratio");
    }

    // ========== 籌碼集中度指標提取方法 ==========

    @Named("extractInstitutionalRatio")
    default BigDecimal extractInstitutionalRatio(Map<String, Object> indicators) {
        return extractBigDecimal(indicators, "institutional_ratio");
    }

    @Named("extractConcentrationTrend")
    default String extractConcentrationTrend(Map<String, Object> indicators) {
        if (indicators == null) {
            return null;
        }
        Object value = indicators.get("concentration_trend");
        return value != null ? value.toString() : null;
    }

    // ========== 通用提取方法 ==========

    /**
     * 從 Map 提取 Long 值
     */
    default Long extractLong(Map<String, Object> map, String key) {
        if (map == null) {
            return null;
        }
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 從 Map 提取 Integer 值
     */
    default Integer extractInteger(Map<String, Object> map, String key) {
        if (map == null) {
            return null;
        }
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 從 Map 提取 BigDecimal 值
     */
    default BigDecimal extractBigDecimal(Map<String, Object> map, String key) {
        if (map == null) {
            return null;
        }
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
