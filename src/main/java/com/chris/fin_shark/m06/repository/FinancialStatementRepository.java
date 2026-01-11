package com.chris.fin_shark.m06.repository;

import com.chris.fin_shark.m06.domain.FinancialStatement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 財務報表 Repository
 * <p>
 * 功能編號: F-M06-003, F-M06-007
 * 功能名稱: 財報資料同步、資料查詢API
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Repository
public interface FinancialStatementRepository extends JpaRepository<FinancialStatement, Long> {

    /**
     * 查詢指定股票的最新財報
     *
     * @param stockId 股票代碼
     * @return 最新財報
     */
    @Query("""
        SELECT fs
        FROM FinancialStatement fs
        WHERE fs.stockId = :stockId
        ORDER BY fs.year DESC, fs.quarter DESC
        LIMIT 1
        """)
    Optional<FinancialStatement> findLatestByStockId(@Param("stockId") String stockId);

    /**
     * 查詢指定股票的財報歷史（倒序）
     *
     * @param stockId  股票代碼
     * @param pageable 分頁參數
     * @return 財報分頁結果
     */
    @Query("""
        SELECT fs
        FROM FinancialStatement fs
        WHERE fs.stockId = :stockId
        ORDER BY fs.year DESC, fs.quarter DESC
        """)
    Page<FinancialStatement> findByStockIdOrderByYearDescQuarterDesc(
            @Param("stockId") String stockId, Pageable pageable);

    /**
     * 查詢指定股票特定年度季度的財報
     *
     * @param stockId 股票代碼
     * @param year    年度
     * @param quarter 季度
     * @return 財報
     */
    Optional<FinancialStatement> findByStockIdAndYearAndQuarter(
            String stockId, Integer year, Short quarter);

    /**
     * 檢查財報資料是否存在
     *
     * @param stockId 股票代碼
     * @param year    年度
     * @param quarter 季度
     * @return 是否存在
     */
    boolean existsByStockIdAndYearAndQuarter(String stockId, Integer year, Short quarter);

    /**
     * 查詢指定股票的所有財報
     *
     * @param stockId 股票代碼
     * @return 財報列表
     */
    @Query("""
        SELECT fs
        FROM FinancialStatement fs
        WHERE fs.stockId = :stockId
        ORDER BY fs.year DESC, fs.quarter DESC
        """)
    List<FinancialStatement> findAllByStockId(@Param("stockId") String stockId);

    /**
     * 查詢指定年度的所有財報
     *
     * @param year 年度
     * @return 財報列表
     */
    List<FinancialStatement> findByYear(Integer year);

    /**
     * 查詢指定年度季度的所有股票財報
     *
     * @param year    年度
     * @param quarter 季度
     * @return 財報列表
     */
    List<FinancialStatement> findByYearAndQuarter(Integer year, Short quarter);

    /**
     * 刪除指定股票特定年度季度的財報
     *
     * @param stockId 股票代碼
     * @param year    年度
     * @param quarter 季度
     */
    void deleteByStockIdAndYearAndQuarter(String stockId, Integer year, Short quarter);

    /**
     * 查詢指定股票在某年度範圍的財報
     *
     * @param stockId   股票代碼
     * @param startYear 開始年度
     * @param endYear   結束年度
     * @return 財報列表
     */
    @Query("""
        SELECT fs
        FROM FinancialStatement fs
        WHERE fs.stockId = :stockId
          AND fs.year BETWEEN :startYear AND :endYear
        ORDER BY fs.year DESC, fs.quarter DESC
        """)
    List<FinancialStatement> findByStockIdAndYearBetween(
            @Param("stockId") String stockId,
            @Param("startYear") Integer startYear,
            @Param("endYear") Integer endYear);
}
