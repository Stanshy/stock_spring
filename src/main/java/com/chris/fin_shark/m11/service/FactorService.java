package com.chris.fin_shark.m11.service;

import com.chris.fin_shark.m11.domain.FactorMetadata;
import com.chris.fin_shark.m11.dto.FactorMetadataDTO;
import com.chris.fin_shark.m11.dto.response.FactorListResponse;
import com.chris.fin_shark.m11.enums.FactorCategory;
import com.chris.fin_shark.m11.exception.FactorDataException;
import com.chris.fin_shark.m11.mapper.StrategyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 因子服務
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FactorService {

    private final StrategyMapper strategyMapper;

    /**
     * 查詢因子清單
     */
    public FactorListResponse getFactors(String category, String sourceModule, String keyword) {
        log.debug("查詢因子: category={}, sourceModule={}, keyword={}",
                category, sourceModule, keyword);

        List<FactorMetadata> factors;

        if (category != null && !category.isEmpty() && !"all".equalsIgnoreCase(category)) {
            factors = strategyMapper.selectFactorsByCategory(category);
        } else {
            factors = strategyMapper.selectActiveFactors();
        }

        // 篩選來源模組
        if (sourceModule != null && !sourceModule.isEmpty() && !"all".equalsIgnoreCase(sourceModule)) {
            factors = factors.stream()
                    .filter(f -> sourceModule.equals(f.getSourceModule()))
                    .collect(Collectors.toList());
        }

        // 關鍵字篩選
        if (keyword != null && !keyword.isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            factors = factors.stream()
                    .filter(f -> f.getFactorName().toLowerCase().contains(lowerKeyword)
                            || f.getDisplayName().toLowerCase().contains(lowerKeyword)
                            || (f.getDescription() != null &&
                                f.getDescription().toLowerCase().contains(lowerKeyword)))
                    .collect(Collectors.toList());
        }

        // 按類別分組
        Map<FactorCategory, List<FactorMetadata>> grouped = factors.stream()
                .collect(Collectors.groupingBy(FactorMetadata::getCategory));

        List<FactorListResponse.FactorCategoryDTO> categories = new ArrayList<>();

        for (FactorCategory cat : FactorCategory.values()) {
            List<FactorMetadata> categoryFactors = grouped.getOrDefault(cat, Collections.emptyList());
            if (!categoryFactors.isEmpty()) {
                categories.add(FactorListResponse.FactorCategoryDTO.builder()
                        .category(cat.name())
                        .categoryName(cat.getDisplayName())
                        .sourceModule(cat.getSourceModule())
                        .factorCount(categoryFactors.size())
                        .factors(categoryFactors.stream()
                                .map(this::toDTO)
                                .collect(Collectors.toList()))
                        .build());
            }
        }

        return FactorListResponse.builder()
                .totalFactors(factors.size())
                .categories(categories)
                .build();
    }

    /**
     * 查詢因子詳情
     */
    public FactorMetadataDTO getFactor(String factorId) {
        FactorMetadata factor = strategyMapper.selectFactorById(factorId);
        if (factor == null) {
            throw FactorDataException.notFound(factorId);
        }
        return toDetailDTO(factor);
    }

    // ==================== 私有方法 ====================

    private FactorMetadataDTO toDTO(FactorMetadata entity) {
        return FactorMetadataDTO.builder()
                .factorId(entity.getFactorId())
                .factorName(entity.getFactorName())
                .displayName(entity.getDisplayName())
                .category(entity.getCategory())
                .sourceModule(entity.getSourceModule())
                .dataType(entity.getDataType())
                .description(entity.getDescription())
                .build();
    }

    @SuppressWarnings("unchecked")
    private FactorMetadataDTO toDetailDTO(FactorMetadata entity) {
        FactorMetadataDTO dto = toDTO(entity);

        // 解析 value_range
        if (entity.getValueRange() != null) {
            dto.setValueRange(FactorMetadataDTO.ValueRangeDTO.builder()
                    .min(entity.getValueRange().get("min"))
                    .max(entity.getValueRange().get("max"))
                    .build());
        }

        // 解析 typical_thresholds
        if (entity.getTypicalThresholds() != null) {
            Object thresholds = entity.getTypicalThresholds().get("values");
            if (thresholds instanceof List) {
                dto.setTypicalThresholds((List<Object>) thresholds);
            }
        }

        // 解析 supported_operators
        if (entity.getSupportedOperators() != null) {
            Object operators = entity.getSupportedOperators().get("operators");
            if (operators instanceof List) {
                dto.setSupportedOperators(((List<?>) operators).stream()
                        .map(Object::toString)
                        .collect(Collectors.toList()));
            }
        }

        dto.setDefaultOperator(entity.getDefaultOperator());
        dto.setCalculationFormula(entity.getCalculationFormula());
        dto.setUpdateFrequency(entity.getUpdateFrequency());

        return dto;
    }
}
