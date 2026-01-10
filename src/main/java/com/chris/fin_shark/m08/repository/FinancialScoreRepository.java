package com.chris.fin_shark.m08.repository;

import com.chris.fin_shark.m08.domain.FinancialScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 財務綜合評分 Repository
 * <p>
 * 功能編號: F-M08-009
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Repository
public interface FinancialScoreRepository extends JpaRepository<FinancialScore, Long> {

    /**
     * 查詢指定股票的最新綜合評分
     *
     * @param stockId 股票代碼
     * @return 最新綜合評分
     */
    @Query("""
        SELECT fs
        FROM FinancialScore fs
        WHERE fs.stockId = :stockId
        ORDER BY fs.year DESC, fs.quarter DESC
        LIMIT 1
        """)
    Optional<FinancialScore> findLatestByStockId(@Param("stockId") String stockId);

    /**
     * 查詢指定股票、年度、季度的綜合評分
     *
     * @param stockId 股票代碼
     * @param year    年度
     * @param quarter 季度
     * @return 綜合評分
     */
    Optional<FinancialScore> findByStockIdAndYearAndQuarter(String stockId, Integer year, Integer quarter);

    /**
     * 查詢指定 Piotroski F-Score 範圍的股票
     *
     * @param minScore 最小分數
     * @param year     年度
     * @param quarter  季度
     * @return 評分列表
     */
    @Query("""
        SELECT fs
        FROM FinancialScore fs
        WHERE fs.year = :year
          AND fs.quarter = :quarter
          AND fs.piotroskiFScore >= :minScore
        ORDER BY fs.piotroskiFScore DESC
        """)
    List<FinancialScore> findByPiotroskiFScoreGreaterThanEqual(
            @Param("minScore") Integer minScore,
            @Param("year") Integer year,
            @Param("quarter") Integer quarter);

    /**
     * 查詢指定年度季度的所有綜合評分
     *
     * @param year    年度
     * @param quarter 季度
     * @return 評分列表
     */
    List<FinancialScore> findByYearAndQuarter(Integer year, Integer quarter);

    /**
     * 檢查綜合評分是否存在
     *
     * @param stockId 股票代碼
     * @param year    年度
     * @param quarter 季度
     * @return 是否存在
     */
    boolean existsByStockIdAndYearAndQuarter(String stockId, Integer year, Integer quarter);
}
