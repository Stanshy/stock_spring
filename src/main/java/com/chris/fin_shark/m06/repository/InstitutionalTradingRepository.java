package com.chris.fin_shark.m06.repository;

import com.chris.fin_shark.m06.domain.InstitutionalTrading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 三大法人買賣超 Repository
 * <p>
 * 功能編號: F-M06-004, F-M06-007
 * 功能名稱: 籌碼資料同步、資料查詢API
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Repository
public interface InstitutionalTradingRepository extends JpaRepository<InstitutionalTrading, Long> {

    /**
     * 查詢指定股票的最新法人買賣超
     *
     * @param stockId 股票代碼
     * @return 最新法人買賣超
     */
    Optional<InstitutionalTrading> findTopByStockIdOrderByTradeDateDesc(String stockId);

    /**
     * 查詢指定股票的法人買賣超歷史（倒序）
     *
     * @param stockId  股票代碼
     * @param pageable 分頁參數
     * @return 法人買賣超分頁結果
     */
    Page<InstitutionalTrading> findByStockIdOrderByTradeDateDesc(String stockId, Pageable pageable);

    /**
     * 查詢指定股票在某日期範圍的法人買賣超
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 法人買賣超列表
     */
    @Query("""
        SELECT it
        FROM InstitutionalTrading it
        WHERE it.stockId = :stockId
          AND it.tradeDate BETWEEN :startDate AND :endDate
        ORDER BY it.tradeDate DESC
        """)
    List<InstitutionalTrading> findByStockIdAndDateRange(@Param("stockId") String stockId,
                                                          @Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    /**
     * 查詢指定股票在某日的法人買賣超
     *
     * @param stockId   股票代碼
     * @param tradeDate 交易日期
     * @return 法人買賣超
     */
    Optional<InstitutionalTrading> findByStockIdAndTradeDate(String stockId, LocalDate tradeDate);

    /**
     * 檢查法人買賣超資料是否存在
     *
     * @param stockId   股票代碼
     * @param tradeDate 交易日期
     * @return 是否存在
     */
    boolean existsByStockIdAndTradeDate(String stockId, LocalDate tradeDate);

    /**
     * 查詢指定日期的所有股票法人買賣超
     *
     * @param tradeDate 交易日期
     * @return 法人買賣超列表
     */
    List<InstitutionalTrading> findByTradeDate(LocalDate tradeDate);

    /**
     * 刪除指定日期範圍的法人買賣超資料
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     */
    void deleteByStockIdAndTradeDateBetween(String stockId, LocalDate startDate, LocalDate endDate);

    /**
     * 查詢指定股票在某日期範圍的法人買賣超（升序）
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 法人買賣超列表（由舊到新）
     */
    @Query("""
        SELECT it
        FROM InstitutionalTrading it
        WHERE it.stockId = :stockId
          AND it.tradeDate BETWEEN :startDate AND :endDate
        ORDER BY it.tradeDate ASC
        """)
    List<InstitutionalTrading> findByStockIdAndTradeDateBetweenOrderByTradeDateAsc(
            @Param("stockId") String stockId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
