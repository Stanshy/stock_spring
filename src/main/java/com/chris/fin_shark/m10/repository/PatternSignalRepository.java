package com.chris.fin_shark.m10.repository;

import com.chris.fin_shark.m10.domain.PatternSignal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 型態訊號 Repository
 *
 * @author chris
 * @since 1.0.0
 */
@Repository
public interface PatternSignalRepository extends JpaRepository<PatternSignal, Long> {

    /**
     * 根據股票代碼和交易日期查詢
     */
    List<PatternSignal> findByStockIdAndTradeDate(String stockId, LocalDate tradeDate);

    /**
     * 根據股票代碼查詢最近的訊號
     */
    List<PatternSignal> findByStockIdOrderByTradeDateDesc(String stockId);

    /**
     * 根據股票代碼和日期範圍查詢
     */
    List<PatternSignal> findByStockIdAndTradeDateBetweenOrderByTradeDateDesc(
            String stockId, LocalDate startDate, LocalDate endDate);

    /**
     * 根據訊號類型查詢
     */
    List<PatternSignal> findBySignalTypeAndTradeDateOrderByConfidenceDesc(
            String signalType, LocalDate tradeDate);

    /**
     * 根據來源類別查詢
     */
    List<PatternSignal> findBySourceCategoryAndTradeDateOrderByConfidenceDesc(
            String sourceCategory, LocalDate tradeDate);

    /**
     * 查詢有效的訊號
     */
    List<PatternSignal> findByStockIdAndStatusOrderByTradeDateDesc(String stockId, String status);

    /**
     * 查詢指定日期的買入訊號
     */
    @Query("SELECT s FROM PatternSignal s WHERE s.tradeDate = :date AND s.signalType = 'BUY' ORDER BY s.confidence DESC")
    List<PatternSignal> findBuySignals(@Param("date") LocalDate date);

    /**
     * 查詢指定日期的賣出訊號
     */
    @Query("SELECT s FROM PatternSignal s WHERE s.tradeDate = :date AND s.signalType = 'SELL' ORDER BY s.confidence DESC")
    List<PatternSignal> findSellSignals(@Param("date") LocalDate date);

    /**
     * 查詢高信心度訊號
     */
    @Query("SELECT s FROM PatternSignal s WHERE s.tradeDate = :date AND s.confidence >= :minConfidence ORDER BY s.confidence DESC")
    List<PatternSignal> findHighConfidenceSignals(@Param("date") LocalDate date, @Param("minConfidence") Integer minConfidence);

    /**
     * 刪除指定股票和日期的記錄
     */
    @Modifying
    @Query("DELETE FROM PatternSignal s WHERE s.stockId = :stockId AND s.tradeDate = :tradeDate")
    void deleteByStockIdAndTradeDate(@Param("stockId") String stockId, @Param("tradeDate") LocalDate tradeDate);

    /**
     * 更新訊號狀態
     */
    @Modifying
    @Query("UPDATE PatternSignal s SET s.status = :status WHERE s.signalId = :signalId")
    void updateStatus(@Param("signalId") Long signalId, @Param("status") String status);

    /**
     * 更新訊號結果
     */
    @Modifying
    @Query("UPDATE PatternSignal s SET s.outcome = :outcome, s.outcomeDate = :outcomeDate, s.actualGainPct = :actualGainPct WHERE s.signalId = :signalId")
    void updateOutcome(@Param("signalId") Long signalId, @Param("outcome") String outcome,
                       @Param("outcomeDate") LocalDate outcomeDate, @Param("actualGainPct") java.math.BigDecimal actualGainPct);
}
