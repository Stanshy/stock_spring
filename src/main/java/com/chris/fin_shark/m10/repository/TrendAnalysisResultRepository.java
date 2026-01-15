package com.chris.fin_shark.m10.repository;

import com.chris.fin_shark.m10.domain.TrendAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 趨勢分析結果 Repository
 *
 * @author chris
 * @since 1.0.0
 */
@Repository
public interface TrendAnalysisResultRepository extends JpaRepository<TrendAnalysisResult, Long> {

    /**
     * 根據股票代碼和分析日期查詢
     */
    Optional<TrendAnalysisResult> findByStockIdAndAnalysisDate(String stockId, LocalDate analysisDate);

    /**
     * 根據股票代碼查詢最近的分析
     */
    List<TrendAnalysisResult> findByStockIdOrderByAnalysisDateDesc(String stockId);

    /**
     * 根據股票代碼和日期範圍查詢
     */
    List<TrendAnalysisResult> findByStockIdAndAnalysisDateBetweenOrderByAnalysisDateDesc(
            String stockId, LocalDate startDate, LocalDate endDate);

    /**
     * 根據主要趨勢 ID 查詢
     */
    List<TrendAnalysisResult> findByPrimaryTrendIdAndAnalysisDateOrderByPrimaryStrengthDesc(
            String primaryTrendId, LocalDate analysisDate);

    /**
     * 查詢指定日期的強趨勢
     */
    @Query("SELECT t FROM TrendAnalysisResult t WHERE t.analysisDate = :date AND t.primaryStrength >= :minStrength ORDER BY t.primaryStrength DESC")
    List<TrendAnalysisResult> findStrongTrends(@Param("date") LocalDate date, @Param("minStrength") Integer minStrength);

    /**
     * 查詢上升趨勢
     */
    @Query("SELECT t FROM TrendAnalysisResult t WHERE t.analysisDate = :date AND t.primaryTrendId = 'TREND001' ORDER BY t.primaryStrength DESC")
    List<TrendAnalysisResult> findUptrends(@Param("date") LocalDate date);

    /**
     * 查詢下降趨勢
     */
    @Query("SELECT t FROM TrendAnalysisResult t WHERE t.analysisDate = :date AND t.primaryTrendId = 'TREND002' ORDER BY t.primaryStrength DESC")
    List<TrendAnalysisResult> findDowntrends(@Param("date") LocalDate date);

    /**
     * 刪除指定股票和日期的記錄
     */
    @Modifying
    @Query("DELETE FROM TrendAnalysisResult t WHERE t.stockId = :stockId AND t.analysisDate = :analysisDate")
    void deleteByStockIdAndAnalysisDate(@Param("stockId") String stockId, @Param("analysisDate") LocalDate analysisDate);
}
