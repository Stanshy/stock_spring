package com.chris.fin_shark.m06.repository;

import com.chris.fin_shark.m06.domain.DataQualityIssue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 資料品質問題記錄 Repository
 * <p>
 * 功能編號: F-M06-006
 * 功能名稱: 資料品質檢核
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Repository
public interface DataQualityIssueRepository extends JpaRepository<DataQualityIssue, Long> {

    /**
     * 查詢未解決的問題
     *
     * @param pageable 分頁參數
     * @return 問題分頁
     */
    @Query("SELECT dqi FROM DataQualityIssue dqi WHERE dqi.status IN ('OPEN', 'IN_PROGRESS') ORDER BY dqi.severity DESC, dqi.createdAt DESC")
    Page<DataQualityIssue> findOpenIssues(Pageable pageable);

    /**
     * 根據檢核ID查詢問題
     *
     * @param checkId 檢核ID
     * @return 問題列表
     */
    List<DataQualityIssue> findByCheckId(Long checkId);

    /**
     * 根據日期和狀態查詢問題
     *
     * @param issueDate 問題日期
     * @param status    狀態
     * @return 問題列表
     */
    List<DataQualityIssue> findByIssueDateAndStatus(LocalDate issueDate, String status);

    /**
     * 統計未解決問題數量
     *
     * @return 未解決問題數量
     */
    @Query("SELECT COUNT(dqi) FROM DataQualityIssue dqi WHERE dqi.status IN ('OPEN', 'IN_PROGRESS')")
    long countOpenIssues();

    /**
     * 統計嚴重問題數量
     *
     * @param severity 嚴重性
     * @return 問題數量
     */
    long countBySeverityAndStatus(String severity, String status);
}
