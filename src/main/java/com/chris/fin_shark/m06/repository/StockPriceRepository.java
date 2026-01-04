package com.chris.fin_shark.m06.repository;

import com.chris.fin_shark.m06.domain.StockPrice;
import com.chris.fin_shark.m06.domain.StockPriceId;
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
 * 股價 Repository
 * <p>
 * 功能編號: F-M06-002, F-M06-007
 * 功能名稱: 股價資料同步、資料查詢API
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Repository
public interface StockPriceRepository extends JpaRepository<StockPrice, StockPriceId> {

    /**
     * 查詢指定股票的最新股價
     *
     * @param stockId 股票代碼
     * @return 最新股價
     */
    Optional<StockPrice> findTopByStockIdOrderByTradeDateDesc(String stockId);



    /**
     * 查詢指定股票的歷史股價（倒序）
     *
     * @param stockId 股票代碼
     * @param pageable 分頁參數
     * @return 股價分頁結果
     */
    Page<StockPrice> findByStockIdOrderByTradeDateDesc(String stockId, Pageable pageable);



    /**
     * 查詢指定股票在某日期範圍的股價
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 股價列表
     */
    @Query("""
        SELECT sp
        FROM StockPrice sp
        WHERE sp.stockId = :stockId
          AND sp.tradeDate BETWEEN :startDate AND :endDate
        ORDER BY sp.tradeDate DESC
        """)
    List<StockPrice> findByStockIdAndDateRange(@Param("stockId") String stockId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /**
     * 查詢指定股票在某日的股價
     *
     * @param stockId   股票代碼
     * @param tradeDate 交易日期
     * @return 股價
     */
    Optional<StockPrice> findByStockIdAndTradeDate(String stockId, LocalDate tradeDate);

    /**
     * 檢查股價資料是否存在
     *
     * @param stockId   股票代碼
     * @param tradeDate 交易日期
     * @return 是否存在
     */
    boolean existsByStockIdAndTradeDate(String stockId, LocalDate tradeDate);

    /**
     * 查詢指定日期的所有股票股價
     *
     * @param tradeDate 交易日期
     * @return 股價列表
     */
    List<StockPrice> findByTradeDate(LocalDate tradeDate);

    /**
     * 刪除指定日期範圍的股價資料
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     */
    void deleteByStockIdAndTradeDateBetween(String stockId, LocalDate startDate, LocalDate endDate);

    /**
     * 查詢指定股票在某日期範圍的股價（升序 - 用於指標計算）
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 股價列表（由舊到新）
     */
    @Query("""
        SELECT sp
        FROM StockPrice sp
        WHERE sp.stockId = :stockId
          AND sp.tradeDate BETWEEN :startDate AND :endDate
        ORDER BY sp.tradeDate ASC
        """)
    List<StockPrice> findByStockIdAndTradeDateBetweenOrderByTradeDateAsc(
            @Param("stockId") String stockId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}

