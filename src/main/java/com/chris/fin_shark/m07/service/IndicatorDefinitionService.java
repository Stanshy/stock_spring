package com.chris.fin_shark.m07.service;

import com.chris.fin_shark.m07.converter.IndicatorDefinitionConverter;
import com.chris.fin_shark.m07.domain.IndicatorDefinition;
import com.chris.fin_shark.m07.dto.IndicatorDefinitionDTO;
import com.chris.fin_shark.m07.exception.IndicatorNotFoundException;
import com.chris.fin_shark.m07.repository.IndicatorDefinitionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 指標定義服務
 * <p>
 * 提供指標定義的查詢功能
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IndicatorDefinitionService {

    private final IndicatorDefinitionRepository definitionRepository;
    private final IndicatorDefinitionConverter definitionConverter;

    /**
     * 查詢所有指標定義
     *
     * @return 指標定義列表
     */
    @Transactional(readOnly = true)
    public List<IndicatorDefinitionDTO> getAllDefinitions() {
        log.debug("查詢所有指標定義");

        List<IndicatorDefinition> definitions = definitionRepository.findAll();
        return definitionConverter.toDTOList(definitions);
    }

    /**
     * 查詢所有啟用的指標定義
     *
     * @return 指標定義列表
     */
    @Transactional(readOnly = true)
    public List<IndicatorDefinitionDTO> getActiveDefinitions() {
        log.debug("查詢啟用的指標定義");

        List<IndicatorDefinition> definitions = definitionRepository.findByIsActiveTrue();
        return definitionConverter.toDTOList(definitions);
    }

    /**
     * 根據類別查詢指標定義
     *
     * @param category 指標類別
     * @return 指標定義列表
     */
    @Transactional(readOnly = true)
    public List<IndicatorDefinitionDTO> getDefinitionsByCategory(String category) {
        log.debug("查詢指標定義: category={}", category);

        List<IndicatorDefinition> definitions =
                definitionRepository.findByIndicatorCategoryAndIsActiveTrue(category);
        return definitionConverter.toDTOList(definitions);
    }

    /**
     * 根據優先級查詢指標定義
     *
     * @param priority 優先級（P0/P1/P2）
     * @return 指標定義列表
     */
    @Transactional(readOnly = true)
    public List<IndicatorDefinitionDTO> getDefinitionsByPriority(String priority) {
        log.debug("查詢指標定義: priority={}", priority);

        List<IndicatorDefinition> definitions =
                definitionRepository.findByPriorityAndIsActiveTrue(priority);
        return definitionConverter.toDTOList(definitions);
    }

    /**
     * 根據指標名稱查詢定義
     * <p>
     * ✅ 修正：在 Service 層處理異常，不返回 Optional
     * </p>
     *
     * @param indicatorName 指標名稱
     * @return 指標定義
     * @throws IndicatorNotFoundException 當指標定義不存在時
     */
    @Transactional(readOnly = true)
    public IndicatorDefinitionDTO getDefinitionByName(String indicatorName) {
        log.debug("查詢指標定義: name={}", indicatorName);

        // ✅ 在 Service 層處理異常
        IndicatorDefinition definition = definitionRepository
                .findByIndicatorName(indicatorName)
                .orElseThrow(() -> IndicatorNotFoundException.of(
                        "指標定義不存在: " + indicatorName
                ));

        return definitionConverter.toDTO(definition);
    }

    /**
     * 根據指標名稱查詢定義（內部使用，返回 Entity）
     * <p>
     * 供 IndicatorCalculationService 內部使用
     * </p>
     *
     * @param indicatorName 指標名稱
     * @return 指標定義 Entity
     * @throws IndicatorNotFoundException 當指標定義不存在時
     */
    @Transactional(readOnly = true)
    public IndicatorDefinition getDefinitionEntityByName(String indicatorName) {
        log.debug("查詢指標定義 Entity: name={}", indicatorName);

        return definitionRepository
                .findByIndicatorName(indicatorName)
                .orElseThrow(() -> IndicatorNotFoundException.of(
                        "指標定義不存在: " + indicatorName
                ));
    }

    /**
     * 根據優先級查詢指標定義（內部使用，返回 Entity）
     * <p>
     * 供 IndicatorCalculationService 內部使用
     * </p>
     *
     * @param priority 優先級
     * @return 指標定義 Entity 列表
     */
    @Transactional(readOnly = true)
    public List<IndicatorDefinition> getDefinitionEntitiesByPriority(String priority) {
        log.debug("查詢指標定義 Entity: priority={}", priority);

        if (priority == null || priority.isEmpty()) {
            return definitionRepository.findByIsActiveTrue();
        }
        return definitionRepository.findByPriorityAndIsActiveTrue(priority);
    }

    /**
     * 檢查指標名稱是否存在
     *
     * @param indicatorName 指標名稱
     * @return 是否存在
     */
    @Transactional(readOnly = true)
    public boolean existsByName(String indicatorName) {
        log.debug("檢查指標是否存在: name={}", indicatorName);

        return definitionRepository.existsByIndicatorName(indicatorName);
    }

    /**
     * 查詢需要快取的指標定義
     *
     * @return 指標定義列表
     */
    @Transactional(readOnly = true)
    public List<IndicatorDefinitionDTO> getCachedDefinitions() {
        log.debug("查詢需要快取的指標定義");

        List<IndicatorDefinition> definitions =
                definitionRepository.findByIsCachedTrueAndIsActiveTrue();
        return definitionConverter.toDTOList(definitions);
    }
}
