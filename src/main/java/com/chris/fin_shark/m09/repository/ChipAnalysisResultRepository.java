package com.chris.fin_shark.m09.repository;

import com.chris.fin_shark.m09.domain.ChipAnalysisResult;
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
 * 籌碼分析結果 Repository
 * <p>
 * 提供籌碼分析結果的 CRUD 操作。
 * 複雜查詢（如排行榜、批次 UPSERT）使用 MyBatis Mapper。
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Repository
public interface ChipAnalysisResultRepository extends JpaRepository<ChipAnalysisResult, Long> {

    /**
     * 根據股票代碼和交易日期查詢
     *
     * @param stockId   股票代碼
     * @param tradeDate 交易日期
     * @return 籌碼分析結果（Optional）
     */
    Optional<ChipAnalysisResult> findByStockIdAndTradeDate(String stockId, LocalDate tradeDate);

    /**
     * 查詢股票在日期區間的籌碼分析結果（依日期升冪排序）
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 籌碼分析結果列表
     */
    List<ChipAnalysisResult> findByStockIdAndTradeDateBetweenOrderByTradeDateAsc(
            String stockId,
            LocalDate startDate,
            LocalDate endDate
    );

    /**
     * 查詢股票在日期區間的籌碼分析結果
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 籌碼分析結果列表
     */
    default List<ChipAnalysisResult> findByStockIdAndTradeDateBetween(
            String stockId,
            LocalDate startDate,
            LocalDate endDate) {
        return findByStockIdAndTradeDateBetweenOrderByTradeDateAsc(stockId, startDate, endDate);
    }

    /**
     * 查詢指定日期的所有股票籌碼分析結果
     *
     * @param tradeDate 交易日期
     * @return 籌碼分析結果列表
     */
    List<ChipAnalysisResult> findByTradeDate(LocalDate tradeDate);

    /**
     * 分頁查詢指定日期的籌碼分析結果
     *
     * @param tradeDate 交易日期
     * @param pageable  分頁參數
     * @return 籌碼分析結果分頁
     */
    Page<ChipAnalysisResult> findByTradeDate(LocalDate tradeDate, Pageable pageable);

    /**
     * 查詢股票的最新籌碼分析結果
     *
     * @param stockId 股票代碼
     * @return 最新籌碼分析結果（Optional）
     */
    @Query("SELECT c FROM ChipAnalysisResult c WHERE c.stockId = :stockId " +
            "ORDER BY c.tradeDate DESC LIMIT 1")
    Optional<ChipAnalysisResult> findLatestByStockId(@Param("stockId") String stockId);

    /**
     * 檢查籌碼分析結果是否存在
     *
     * @param stockId   股票代碼
     * @param tradeDate 交易日期
     * @return 是否存在
     */
    boolean existsByStockIdAndTradeDate(String stockId, LocalDate tradeDate);

    /**
     * 批次查詢多檔股票的最新籌碼分析結果
     *
     * @param stockIds 股票代碼列表
     * @return 籌碼分析結果列表
     */
    @Query("SELECT c FROM ChipAnalysisResult c WHERE c.stockId IN :stockIds " +
            "AND c.tradeDate = (SELECT MAX(c2.tradeDate) FROM ChipAnalysisResult c2 WHERE c2.stockId = c.stockId)")
    List<ChipAnalysisResult> findLatestByStockIds(@Param("stockIds") List<String> stockIds);

    /**
     * 取得最新交易日期
     *
     * @return 最新交易日期
     */
    @Query("SELECT MAX(c.tradeDate) FROM ChipAnalysisResult c")
    Optional<LocalDate> findLatestTradeDate();

    /**
     * 查詢指定日期外資買超前 N 名
     *
     * @param tradeDate 交易日期
     * @param pageable  分頁參數
     * @return 籌碼分析結果列表
     */
    @Query("SELECT c FROM ChipAnalysisResult c WHERE c.tradeDate = :tradeDate " +
            "AND c.foreignNet > 0 ORDER BY c.foreignNet DESC")
    List<ChipAnalysisResult> findTopForeignBuyByTradeDate(
            @Param("tradeDate") LocalDate tradeDate,
            Pageable pageable
    );

    /**
     * 查詢指定日期外資賣超前 N 名
     *
     * @param tradeDate 交易日期
     * @param pageable  分頁參數
     * @return 籌碼分析結果列表
     */
    @Query("SELECT c FROM ChipAnalysisResult c WHERE c.tradeDate = :tradeDate " +
            "AND c.foreignNet < 0 ORDER BY c.foreignNet ASC")
    List<ChipAnalysisResult> findTopForeignSellByTradeDate(
            @Param("tradeDate") LocalDate tradeDate,
            Pageable pageable
    );

    /**
     * 查詢指定日期三大法人合計買超前 N 名
     *
     * @param tradeDate 交易日期
     * @param pageable  分頁參數
     * @return 籌碼分析結果列表
     */
    @Query("SELECT c FROM ChipAnalysisResult c WHERE c.tradeDate = :tradeDate " +
            "AND c.totalNet > 0 ORDER BY c.totalNet DESC")
    List<ChipAnalysisResult> findTopTotalNetByTradeDate(
            @Param("tradeDate") LocalDate tradeDate,
            Pageable pageable
    );

    /**
     * 刪除指定日期之前的歷史資料
     *
     * @param date 日期
     * @return 刪除筆數
     */
    int deleteByTradeDateBefore(LocalDate date);
}
