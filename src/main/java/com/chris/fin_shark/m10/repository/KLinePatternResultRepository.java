package com.chris.fin_shark.m10.repository;

import com.chris.fin_shark.m10.domain.KLinePatternResult;
import com.chris.fin_shark.m10.domain.KLinePatternResultId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * K 線型態結果 Repository
 *
 * @author chris
 * @since 1.0.0
 */
@Repository
public interface KLinePatternResultRepository extends JpaRepository<KLinePatternResult, KLinePatternResultId> {

    /**
     * 根據股票代碼和交易日期查詢
     */
    List<KLinePatternResult> findByStockIdAndTradeDate(String stockId, LocalDate tradeDate);

    /**
     * 根據股票代碼查詢最近的型態
     */
    List<KLinePatternResult> findByStockIdOrderByTradeDateDesc(String stockId);

    /**
     * 根據股票代碼和日期範圍查詢
     */
    List<KLinePatternResult> findByStockIdAndTradeDateBetweenOrderByTradeDateDesc(
            String stockId, LocalDate startDate, LocalDate endDate);

    /**
     * 根據股票代碼和型態編號查詢
     */
    List<KLinePatternResult> findByStockIdAndPatternIdOrderByTradeDateDesc(
            String stockId, String patternId);

    /**
     * 根據股票代碼和類別查詢
     */
    List<KLinePatternResult> findByStockIdAndPatternCategoryOrderByTradeDateDesc(
            String stockId, String patternCategory);

    /**
     * 根據訊號類型查詢（全市場）
     */
    List<KLinePatternResult> findBySignalTypeAndTradeDateOrderByStrengthDesc(
            String signalType, LocalDate tradeDate);

    /**
     * 查詢指定日期強度大於門檻的型態
     */
    @Query("SELECT k FROM KLinePatternResult k WHERE k.tradeDate = :date AND k.strength >= :minStrength ORDER BY k.strength DESC")
    List<KLinePatternResult> findStrongPatterns(@Param("date") LocalDate date, @Param("minStrength") Integer minStrength);

    /**
     * 查詢指定日期的看漲型態
     */
    @Query("SELECT k FROM KLinePatternResult k WHERE k.tradeDate = :date AND k.signalType IN ('BULLISH_REVERSAL', 'BULLISH_CONTINUATION') ORDER BY k.strength DESC")
    List<KLinePatternResult> findBullishPatterns(@Param("date") LocalDate date);

    /**
     * 查詢指定日期的看跌型態
     */
    @Query("SELECT k FROM KLinePatternResult k WHERE k.tradeDate = :date AND k.signalType IN ('BEARISH_REVERSAL', 'BEARISH_CONTINUATION') ORDER BY k.strength DESC")
    List<KLinePatternResult> findBearishPatterns(@Param("date") LocalDate date);

    /**
     * 刪除指定股票和日期的記錄
     */
    @Modifying
    @Query("DELETE FROM KLinePatternResult k WHERE k.stockId = :stockId AND k.tradeDate = :tradeDate")
    void deleteByStockIdAndTradeDate(@Param("stockId") String stockId, @Param("tradeDate") LocalDate tradeDate);

    /**
     * 檢查是否已存在記錄
     */
    boolean existsByStockIdAndPatternIdAndTradeDate(String stockId, String patternId, LocalDate tradeDate);
}
