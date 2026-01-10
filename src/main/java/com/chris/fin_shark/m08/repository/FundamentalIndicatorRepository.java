package com.chris.fin_shark.m08.repository;

import com.chris.fin_shark.m08.domain.FundamentalIndicator;
import com.chris.fin_shark.m08.enums.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 基本面財務指標 Repository
 * <p>
 * 功能編號: F-M08-001 ~ F-M08-008
 * 使用 JPA 處理簡單查詢
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Repository
public interface FundamentalIndicatorRepository extends JpaRepository<FundamentalIndicator, Long> {

    /**
     * 查詢指定股票的最新財務指標
     *
     * @param stockId 股票代碼
     * @return 最新財務指標
     */
    @Query("""
        SELECT fi
        FROM FundamentalIndicator fi
        WHERE fi.stockId = :stockId
        ORDER BY fi.year DESC, fi.quarter DESC
        LIMIT 1
        """)
    Optional<FundamentalIndicator> findLatestByStockId(@Param("stockId") String stockId);

    /**
     * 查詢指定股票、年度、季度的財務指標
     *
     * @param stockId    股票代碼
     * @param year       年度
     * @param quarter    季度
     * @param reportType 報表類型
     * @return 財務指標
     */
    Optional<FundamentalIndicator> findByStockIdAndYearAndQuarterAndReportType(
            String stockId, Integer year, Integer quarter, ReportType reportType);

    /**
     * 查詢指定股票的歷史財務指標（最近 N 季）
     *
     * @param stockId 股票代碼
     * @param limit   數量限制
     * @return 財務指標列表
     */
    @Query("""
        SELECT fi
        FROM FundamentalIndicator fi
        WHERE fi.stockId = :stockId
        ORDER BY fi.year DESC, fi.quarter DESC
        LIMIT :limit
        """)
    List<FundamentalIndicator> findRecentByStockId(@Param("stockId") String stockId,
                                                   @Param("limit") int limit);

    /**
     * 查詢指定日期範圍的財務指標
     *
     * @param stockId   股票代碼
     * @param startYear 起始年度
     * @param endYear   結束年度
     * @return 財務指標列表
     */
    @Query("""
        SELECT fi
        FROM FundamentalIndicator fi
        WHERE fi.stockId = :stockId
          AND fi.year BETWEEN :startYear AND :endYear
        ORDER BY fi.year ASC, fi.quarter ASC
        """)
    List<FundamentalIndicator> findByStockIdAndYearRange(@Param("stockId") String stockId,
                                                         @Param("startYear") Integer startYear,
                                                         @Param("endYear") Integer endYear);

    /**
     * 查詢指定年度季度的所有股票財務指標
     *
     * @param year    年度
     * @param quarter 季度
     * @return 財務指標列表
     */
    List<FundamentalIndicator> findByYearAndQuarter(Integer year, Integer quarter);

    /**
     * 檢查財務指標是否存在
     *
     * @param stockId    股票代碼
     * @param year       年度
     * @param quarter    季度
     * @param reportType 報表類型
     * @return 是否存在
     */
    boolean existsByStockIdAndYearAndQuarterAndReportType(
            String stockId, Integer year, Integer quarter, ReportType reportType);

    /**
     * 查詢在指定計算日期之後更新的財務指標
     *
     * @param calculationDate 計算日期
     * @return 財務指標列表
     */
    List<FundamentalIndicator> findByCalculationDateAfter(LocalDate calculationDate);

    /**
     * 刪除指定股票的所有財務指標
     *
     * @param stockId 股票代碼
     */
    void deleteByStockId(String stockId);
}
