package com.chris.fin_shark.m08.converter;

import com.chris.fin_shark.m08.domain.FinancialScore;
import com.chris.fin_shark.m08.dto.FinancialScoreDTO;
import org.mapstruct.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 財務綜合評分 Converter
 * 🔴 修正：提供 Map<String, Object> → Map<String, Integer> 自定義轉換
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FinancialScoreConverter {

    /**
     * Entity → DTO
     * 🔴 修正：piotroskiDetails 使用自定義映射方法
     */
    @Mapping(target = "stockName", ignore = true)
    @Mapping(target = "piotroskiInterpretation", ignore = true)
    @Mapping(target = "altmanInterpretation", ignore = true)
    @Mapping(target = "beneishInterpretation", ignore = true)
    @Mapping(source = "piotroskiDetails", target = "piotroskiDetails", qualifiedByName = "convertObjectMapToIntegerMap")
    FinancialScoreDTO toDTO(FinancialScore entity);

    /**
     * Entity List → DTO List
     */
    List<FinancialScoreDTO> toDTOList(List<FinancialScore> entities);

    /**
     * 🔴 自定義映射方法：Map<String, Object> → Map<String, Integer>
     *
     * @param source 來源 Map（Object 類型）
     * @return 目標 Map（Integer 類型）
     */
    @Named("convertObjectMapToIntegerMap")
    default Map<String, Integer> convertObjectMapToIntegerMap(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }

        Map<String, Integer> result = new HashMap<>();
        source.forEach((key, value) -> {
            if (value != null) {
                if (value instanceof Number) {
                    // 如果是數字類型，直接轉換
                    result.put(key, ((Number) value).intValue());
                } else if (value instanceof String) {
                    // 如果是字串，嘗試解析
                    try {
                        result.put(key, Integer.parseInt((String) value));
                    } catch (NumberFormatException e) {
                        // 無法轉換的值忽略
                        // 或者可以記錄 log
                    }
                }
            }
        });

        return result.isEmpty() ? null : result;
    }
}