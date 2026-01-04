package com.chris.fin_shark.m07.repository;

import com.chris.fin_shark.m07.domain.IndicatorDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 指標定義 Repository
 * <p>
 * 提供指標定義的 CRUD 操作
 * 使用 Spring Data JPA
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Repository
public interface IndicatorDefinitionRepository extends JpaRepository<IndicatorDefinition, Long> {

    /**
     * 根據指標名稱查詢定義
     *
     * @param indicatorName 指標名稱
     * @return 指標定義（Optional）
     */
    Optional<IndicatorDefinition> findByIndicatorName(String indicatorName);

    /**
     * 查詢所有啟用的指標定義
     *
     * @return 指標定義列表
     */
    List<IndicatorDefinition> findByIsActiveTrue();

    /**
     * 根據類別查詢啟用的指標定義
     *
     * @param category 指標類別
     * @return 指標定義列表
     */
    List<IndicatorDefinition> findByIndicatorCategoryAndIsActiveTrue(String category);

    /**
     * 根據優先級查詢指標定義
     *
     * @param priority 優先級（P0/P1/P2）
     * @return 指標定義列表
     */
    List<IndicatorDefinition> findByPriority(String priority);

    /**
     * 根據優先級查詢啟用的指標定義
     *
     * @param priority 優先級
     * @return 指標定義列表
     */
    List<IndicatorDefinition> findByPriorityAndIsActiveTrue(String priority);

    /**
     * 檢查指標名稱是否存在
     *
     * @param indicatorName 指標名稱
     * @return 是否存在
     */
    boolean existsByIndicatorName(String indicatorName);

    /**
     * 查詢需要快取的指標定義
     *
     * @return 指標定義列表
     */
    List<IndicatorDefinition> findByIsCachedTrueAndIsActiveTrue();
}
