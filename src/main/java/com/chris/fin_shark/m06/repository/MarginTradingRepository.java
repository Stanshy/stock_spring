package com.chris.fin_shark.m06.repository;

import com.chris.fin_shark.m06.domain.MarginTrading;
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
 * 融資融券 Repository
 * <p>
 * 功能編號: F-M06-004, F-M06-007
 * 功能名稱: 籌碼資料同步、資料查詢API
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Repository
public interface MarginTradingRepository extends JpaRepository<MarginTrading, Long> {

    /**
     * 查詢指定股票的最新融資融券
     *
     * @param stockId 股票代碼
     * @return 最新融資融券
     */
    Optional<MarginTrading> findTopByStockIdOrderByTradeDateDesc(String stockId);

    /**
     * 查詢指定股票的融資融券歷史（倒序）
     *
     * @param stockId  股票代碼
     * @param pageable 分頁參數
     * @return 融資融券分頁結果
     */
    Page<MarginTrading> findByStockIdOrderByTradeDateDesc(String stockId, Pageable pageable);

    /**
     * 查詢指定股票在某日期範圍的融資融券
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 融資融券列表
     */
    @Query("""
        SELECT mt
        FROM MarginTrading mt
        WHERE mt.stockId = :stockId
          AND mt.tradeDate BETWEEN :startDate AND :endDate
        ORDER BY mt.tradeDate DESC
        """)
    List<MarginTrading> findByStockIdAndDateRange(@Param("stockId") String stockId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    /**
     * 查詢指定股票在某日的融資融券
     *
     * @param stockId   股票代碼
     * @param tradeDate 交易日期
     * @return 融資融券
     */
    Optional<MarginTrading> findByStockIdAndTradeDate(String stockId, LocalDate tradeDate);

    /**
     * 檢查融資融券資料是否存在
     *
     * @param stockId   股票代碼
     * @param tradeDate 交易日期
     * @return 是否存在
     */
    boolean existsByStockIdAndTradeDate(String stockId, LocalDate tradeDate);

    /**
     * 查詢指定日期的所有股票融資融券
     *
     * @param tradeDate 交易日期
     * @return 融資融券列表
     */
    List<MarginTrading> findByTradeDate(LocalDate tradeDate);

    /**
     * 刪除指定日期範圍的融資融券資料
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     */
    void deleteByStockIdAndTradeDateBetween(String stockId, LocalDate startDate, LocalDate endDate);

    /**
     * 查詢指定股票在某日期範圍的融資融券（升序）
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 融資融券列表（由舊到新）
     */
    @Query("""
        SELECT mt
        FROM MarginTrading mt
        WHERE mt.stockId = :stockId
          AND mt.tradeDate BETWEEN :startDate AND :endDate
        ORDER BY mt.tradeDate ASC
        """)
    List<MarginTrading> findByStockIdAndTradeDateBetweenOrderByTradeDateAsc(
            @Param("stockId") String stockId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
