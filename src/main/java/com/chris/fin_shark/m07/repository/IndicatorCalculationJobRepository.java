package com.chris.fin_shark.m07.repository;

import com.chris.fin_shark.m07.domain.IndicatorCalculationJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 指標計算 Job 執行記錄 Repository
 * <p>
 * 提供 Job 執行記錄的 CRUD 操作
 * 使用 Spring Data JPA
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Repository
public interface IndicatorCalculationJobRepository extends JpaRepository<IndicatorCalculationJob, Long> {

    /**
     * 根據計算日期查詢 Job
     *
     * @param calculationDate 計算日期
     * @return Job 列表
     */
    List<IndicatorCalculationJob> findByCalculationDate(LocalDate calculationDate);

    /**
     * 根據狀態查詢 Job（分頁）
     *
     * @param status   狀態
     * @param pageable 分頁參數
     * @return Job 分頁結果
     */
    Page<IndicatorCalculationJob> findByStatus(String status, Pageable pageable);

    /**
     * 根據 Job 類型和狀態查詢
     *
     * @param jobType Job 類型
     * @param status  狀態
     * @return Job 列表
     */
    List<IndicatorCalculationJob> findByJobTypeAndStatus(String jobType, String status);

    /**
     * 查詢最新的 Job 執行記錄
     *
     * @param jobType Job 類型
     * @return 最新 Job（Optional）
     */
    Optional<IndicatorCalculationJob> findFirstByJobTypeOrderByCreatedAtDesc(String jobType);

    /**
     * 查詢正在執行的 Job
     *
     * @return Job 列表
     */
    List<IndicatorCalculationJob> findByStatus(String status);

    /**
     * 查詢指定日期是否有成功的 Job
     *
     * @param calculationDate 計算日期
     * @param status          狀態
     * @return 是否存在
     */
    boolean existsByCalculationDateAndStatus(LocalDate calculationDate, String status);

}