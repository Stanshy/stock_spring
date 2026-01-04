package com.chris.fin_shark.m06.repository;

import com.chris.fin_shark.m06.domain.DataQualityCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 資料品質檢核規則 Repository
 * <p>
 * 功能編號: F-M06-006
 * 功能名稱: 資料品質檢核
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Repository
public interface DataQualityCheckRepository extends JpaRepository<DataQualityCheck, Long> {

    /**
     * 查詢所有啟用的檢核規則
     *
     * @return 啟用的檢核規則列表
     */
    List<DataQualityCheck> findByIsActiveTrue();

    /**
     * 根據檢核類型查詢
     *
     * @param checkType 檢核類型
     * @return 檢核規則列表
     */
    List<DataQualityCheck> findByCheckType(String checkType);

    /**
     * 根據目標表查詢啟用的檢核規則
     *
     * @param targetTable 目標表
     * @return 檢核規則列表
     */
    @Query("SELECT dqc FROM DataQualityCheck dqc WHERE dqc.targetTable = :targetTable AND dqc.isActive = true")
    List<DataQualityCheck> findActiveChecksByTargetTable(String targetTable);
}
