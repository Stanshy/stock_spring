package com.chris.fin_shark.m06.repository;

import com.chris.fin_shark.common.domain.JobExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Job 執行記錄 Repository
 * <p>
 * 功能編號: F-M06-008
 * 功能名稱: 資料更新排程
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Repository
public interface JobExecutionRepository extends JpaRepository<JobExecution, Long> {

    /**
     * 根據 Job 名稱查詢最新執行記錄
     *
     * @param jobName Job 名稱
     * @return 最新執行記錄
     */
    @Query("SELECT je FROM JobExecution je WHERE je.jobName = :jobName ORDER BY je.startTime DESC LIMIT 1")
    Optional<JobExecution> findLatestByJobName(@Param("jobName") String jobName);

    /**
     * 查詢指定 Job 的執行歷史（分頁）
     *
     * @param jobName  Job 名稱
     * @param pageable 分頁參數
     * @return 執行記錄分頁
     */
    Page<JobExecution> findByJobNameOrderByStartTimeDesc(String jobName, Pageable pageable);

    /**
     * 查詢指定狀態的 Job
     *
     * @param jobStatus Job 狀態
     * @return Job 列表
     */
    List<JobExecution> findByJobStatus(String jobStatus);

    /**
     * 查詢執行中的 Job
     *
     * @return 執行中的 Job 列表
     */
    @Query("SELECT je FROM JobExecution je WHERE je.jobStatus = 'RUNNING'")
    List<JobExecution> findRunningJobs();

    /**
     * 查詢失敗的 Job
     *
     * @param startTime 開始時間
     * @return 失敗的 Job 列表
     */
    @Query("SELECT je FROM JobExecution je WHERE je.jobStatus = 'FAILED' AND je.startTime >= :startTime ORDER BY je.startTime DESC")
    List<JobExecution> findFailedJobsSince(@Param("startTime") LocalDateTime startTime);

    /**
     * 統計 Job 執行次數
     *
     * @param jobName Job 名稱
     * @return 執行次數
     */
    long countByJobName(String jobName);

    /**
     * 統計指定狀態的 Job 數量
     *
     * @param jobName   Job 名稱（可為 null）
     * @param jobStatus Job 狀態
     * @return Job 數量
     */
    long countByJobNameAndJobStatus(String jobName, String jobStatus);
}
