package com.chris.fin_shark.m09.repository;

import com.chris.fin_shark.m09.domain.ChipSignalEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * 籌碼異常訊號 Repository
 * <p>
 * 提供籌碼異常訊號的 CRUD 操作。
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Repository
public interface ChipSignalRepository extends JpaRepository<ChipSignalEntity, Long> {

    /**
     * 查詢股票在指定日期的訊號
     *
     * @param stockId   股票代碼
     * @param tradeDate 交易日期
     * @return 訊號列表
     */
    List<ChipSignalEntity> findByStockIdAndTradeDate(String stockId, LocalDate tradeDate);

    /**
     * 查詢股票在日期區間的訊號（依日期降冪排序）
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 訊號列表
     */
    List<ChipSignalEntity> findByStockIdAndTradeDateBetweenOrderByTradeDateDesc(
            String stockId,
            LocalDate startDate,
            LocalDate endDate
    );

    /**
     * 查詢股票在日期區間的訊號
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 訊號列表
     */
    default List<ChipSignalEntity> findByStockIdAndTradeDateBetween(
            String stockId,
            LocalDate startDate,
            LocalDate endDate) {
        return findByStockIdAndTradeDateBetweenOrderByTradeDateDesc(stockId, startDate, endDate);
    }

    /**
     * 查詢指定日期的所有訊號
     *
     * @param tradeDate 交易日期
     * @return 訊號列表
     */
    List<ChipSignalEntity> findByTradeDate(LocalDate tradeDate);

    /**
     * 查詢指定日期的高嚴重度訊號（CRITICAL, HIGH）
     *
     * @param tradeDate 交易日期
     * @param pageable  分頁參數
     * @return 訊號分頁
     */
    @Query("SELECT s FROM ChipSignalEntity s WHERE s.tradeDate = :tradeDate " +
            "AND s.severity IN ('CRITICAL', 'HIGH') AND s.isActive = true " +
            "ORDER BY CASE s.severity WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 END")
    Page<ChipSignalEntity> findHighSeveritySignalsByTradeDate(
            @Param("tradeDate") LocalDate tradeDate,
            Pageable pageable
    );

    /**
     * 查詢指定日期特定類型的訊號
     *
     * @param tradeDate  交易日期
     * @param signalType 訊號類型
     * @return 訊號列表
     */
    List<ChipSignalEntity> findByTradeDateAndSignalType(
            LocalDate tradeDate,
            String signalType
    );

    /**
     * 查詢指定日期特定嚴重度的訊號
     *
     * @param tradeDate 交易日期
     * @param severity  嚴重度
     * @return 訊號列表
     */
    List<ChipSignalEntity> findByTradeDateAndSeverity(
            LocalDate tradeDate,
            String severity
    );

    /**
     * 查詢未確認的活躍訊號
     *
     * @param pageable 分頁參數
     * @return 訊號分頁
     */
    @Query("SELECT s FROM ChipSignalEntity s WHERE s.isActive = true " +
            "AND s.acknowledgedAt IS NULL ORDER BY s.tradeDate DESC, " +
            "CASE s.severity WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 " +
            "WHEN 'MEDIUM' THEN 3 WHEN 'LOW' THEN 4 END")
    Page<ChipSignalEntity> findUnacknowledgedSignals(Pageable pageable);

    /**
     * 查詢特定訊號代碼的訊號
     *
     * @param signalCode 訊號代碼
     * @param tradeDate  交易日期
     * @param pageable   分頁參數
     * @return 訊號分頁
     */
    Page<ChipSignalEntity> findBySignalCodeAndTradeDate(
            String signalCode,
            LocalDate tradeDate,
            Pageable pageable
    );

    /**
     * 統計指定日期各嚴重度的訊號數量
     *
     * @param tradeDate 交易日期
     * @return 統計結果
     */
    @Query("SELECT s.severity, COUNT(s) FROM ChipSignalEntity s " +
            "WHERE s.tradeDate = :tradeDate AND s.isActive = true " +
            "GROUP BY s.severity")
    List<Object[]> countBySeverityAndTradeDate(@Param("tradeDate") LocalDate tradeDate);

    /**
     * 刪除指定日期之前的歷史訊號
     *
     * @param date 日期
     * @return 刪除筆數
     */
    int deleteByTradeDateBefore(LocalDate date);
}
