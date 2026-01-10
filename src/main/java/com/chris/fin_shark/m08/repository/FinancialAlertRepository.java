package com.chris.fin_shark.m08.repository;

import com.chris.fin_shark.m08.domain.FinancialAlert;
import com.chris.fin_shark.m08.enums.AlertCategory;
import com.chris.fin_shark.m08.enums.AlertStatus;
import com.chris.fin_shark.m08.enums.Severity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 財務異常警示 Repository
 * <p>
 * 功能編號: F-M08-012
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Repository
public interface FinancialAlertRepository extends JpaRepository<FinancialAlert, Long> {

    /**
     * 查詢指定股票的活躍警示
     *
     * @param stockId 股票代碼
     * @return 警示列表
     */
    List<FinancialAlert> findByStockIdAndAlertStatus(String stockId, AlertStatus alertStatus);

    /**
     * 查詢指定股票、年度、季度的警示
     *
     * @param stockId 股票代碼
     * @param year    年度
     * @param quarter 季度
     * @return 警示列表
     */
    List<FinancialAlert> findByStockIdAndYearAndQuarter(String stockId, Integer year, Integer quarter);

    /**
     * 查詢指定嚴重程度的 ACTIVE 警示（依建立時間新到舊）
     *
     * @param severity 嚴重程度
     * @param year     年度
     * @param quarter  季度
     * @return 警示列表
     */
    @Query("""
    SELECT fa
    FROM FinancialAlert fa
    WHERE fa.severity = :severity
      AND fa.year = :year
      AND fa.quarter = :quarter
      AND fa.alertStatus = 'ACTIVE'
    ORDER BY fa.createdAt DESC
    """)
    List<FinancialAlert> findBySeverityAndYearAndQuarter(
            @Param("severity") Severity severity,
            @Param("year") Integer year,
            @Param("quarter") Integer quarter);


    /**
     * 查詢指定類別的警示
     *
     * @param alertCategory 警示類別
     * @param stockId       股票代碼
     * @return 警示列表
     */
    List<FinancialAlert> findByAlertCategoryAndStockIdOrderByCreatedAtDesc(
            AlertCategory alertCategory, String stockId);

    /**
     * 統計指定股票的活躍警示數量
     *
     * @param stockId 股票代碼
     * @return 警示數量
     */
    @Query("""
        SELECT COUNT(fa)
        FROM FinancialAlert fa
        WHERE fa.stockId = :stockId
          AND fa.alertStatus = 'ACTIVE'
        """)
    Long countActiveAlertsByStockId(@Param("stockId") String stockId);
}
