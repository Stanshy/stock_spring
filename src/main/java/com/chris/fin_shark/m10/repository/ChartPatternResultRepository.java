package com.chris.fin_shark.m10.repository;

import com.chris.fin_shark.m10.domain.ChartPatternResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 圖表型態結果 Repository
 *
 * @author chris
 * @since 1.0.0
 */
@Repository
public interface ChartPatternResultRepository extends JpaRepository<ChartPatternResult, Long> {

    /**
     * 根據股票代碼和偵測日期查詢
     */
    List<ChartPatternResult> findByStockIdAndDetectionDate(String stockId, LocalDate detectionDate);

    /**
     * 根據股票代碼查詢最近的型態
     */
    List<ChartPatternResult> findByStockIdOrderByDetectionDateDesc(String stockId);

    /**
     * 根據股票代碼和日期範圍查詢
     */
    List<ChartPatternResult> findByStockIdAndDetectionDateBetweenOrderByDetectionDateDesc(
            String stockId, LocalDate startDate, LocalDate endDate);

    /**
     * 根據股票代碼和型態編號查詢
     */
    List<ChartPatternResult> findByStockIdAndPatternIdOrderByDetectionDateDesc(
            String stockId, String patternId);

    /**
     * 根據股票代碼和類別查詢
     */
    List<ChartPatternResult> findByStockIdAndPatternCategoryOrderByDetectionDateDesc(
            String stockId, String patternCategory);

    /**
     * 根據訊號類型查詢（全市場）
     */
    List<ChartPatternResult> findBySignalTypeAndDetectionDateOrderByStrengthDesc(
            String signalType, LocalDate detectionDate);

    /**
     * 查詢指定日期強度大於門檻的型態
     */
    @Query("SELECT c FROM ChartPatternResult c WHERE c.detectionDate = :date AND c.strength >= :minStrength ORDER BY c.strength DESC")
    List<ChartPatternResult> findStrongPatterns(@Param("date") LocalDate date, @Param("minStrength") Integer minStrength);

    /**
     * 查詢正在形成中的型態
     */
    @Query("SELECT c FROM ChartPatternResult c WHERE c.stockId = :stockId AND c.status = 'FORMING' ORDER BY c.detectionDate DESC")
    List<ChartPatternResult> findFormingPatterns(@Param("stockId") String stockId);

    /**
     * 查詢已突破的型態
     */
    @Query("SELECT c FROM ChartPatternResult c WHERE c.breakoutLevel IS NOT NULL AND c.detectionDate >= :fromDate ORDER BY c.detectionDate DESC")
    List<ChartPatternResult> findBreakoutPatterns(@Param("fromDate") LocalDate fromDate);

    /**
     * 根據狀態查詢
     */
    List<ChartPatternResult> findByStockIdAndStatusOrderByDetectionDateDesc(String stockId, String status);

    /**
     * 刪除指定股票和日期的記錄
     */
    @Modifying
    @Query("DELETE FROM ChartPatternResult c WHERE c.stockId = :stockId AND c.detectionDate = :detectionDate")
    void deleteByStockIdAndDetectionDate(@Param("stockId") String stockId, @Param("detectionDate") LocalDate detectionDate);

    /**
     * 檢查是否已存在記錄
     */
    boolean existsByStockIdAndPatternIdAndDetectionDate(String stockId, String patternId, LocalDate detectionDate);
}
