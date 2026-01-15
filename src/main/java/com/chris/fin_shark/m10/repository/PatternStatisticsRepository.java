package com.chris.fin_shark.m10.repository;

import com.chris.fin_shark.m10.domain.PatternStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 型態統計 Repository
 *
 * @author chris
 * @since 1.0.0
 */
@Repository
public interface PatternStatisticsRepository extends JpaRepository<PatternStatistics, Long> {

    /**
     * 根據股票代碼和型態 ID 查詢
     */
    List<PatternStatistics> findByStockIdAndPatternIdOrderByStatPeriodEndDesc(String stockId, String patternId);

    /**
     * 根據股票代碼查詢
     */
    List<PatternStatistics> findByStockIdOrderByOccurrenceCountDesc(String stockId);

    /**
     * 根據型態 ID 查詢（全市場）
     */
    List<PatternStatistics> findByPatternIdOrderBySuccessRateDesc(String patternId);

    /**
     * 查詢最新統計
     */
    @Query("SELECT p FROM PatternStatistics p WHERE p.stockId = :stockId AND p.patternId = :patternId AND p.expiresAt > :now ORDER BY p.statPeriodEnd DESC")
    Optional<PatternStatistics> findLatestStatistics(@Param("stockId") String stockId, @Param("patternId") String patternId, @Param("now") LocalDateTime now);

    /**
     * 查詢高成功率型態
     */
    @Query("SELECT p FROM PatternStatistics p WHERE p.stockId = :stockId AND p.successRate >= :minRate ORDER BY p.successRate DESC")
    List<PatternStatistics> findHighSuccessRatePatterns(@Param("stockId") String stockId, @Param("minRate") java.math.BigDecimal minRate);

    /**
     * 查詢過期統計
     */
    @Query("SELECT p FROM PatternStatistics p WHERE p.expiresAt < :now")
    List<PatternStatistics> findExpiredStatistics(@Param("now") LocalDateTime now);

    /**
     * 刪除過期統計
     */
    @Modifying
    @Query("DELETE FROM PatternStatistics p WHERE p.expiresAt < :now")
    void deleteExpiredStatistics(@Param("now") LocalDateTime now);
}
