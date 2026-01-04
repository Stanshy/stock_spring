package com.chris.fin_shark.m07.repository;

import com.chris.fin_shark.m07.domain.TechnicalIndicator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 技術指標 Repository
 * <p>
 * 提供技術指標的 CRUD 操作
 * 使用 Spring Data JPA
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Repository
public interface TechnicalIndicatorRepository extends JpaRepository<TechnicalIndicator, Long> {

    /**
     * 根據股票代碼和計算日期查詢指標
     *
     * @param stockId         股票代碼
     * @param calculationDate 計算日期
     * @return 技術指標（Optional）
     */
    Optional<TechnicalIndicator> findByStockIdAndCalculationDate(
            String stockId,
            LocalDate calculationDate
    );

    /**
     * 查詢股票在日期區間的指標
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 技術指標列表
     */
    List<TechnicalIndicator> findByStockIdAndCalculationDateBetween(
            String stockId,
            LocalDate startDate,
            LocalDate endDate
    );

    /**
     * 查詢指定日期的所有股票指標
     *
     * @param calculationDate 計算日期
     * @return 技術指標列表
     */
    List<TechnicalIndicator> findByCalculationDate(LocalDate calculationDate);

    /**
     * 檢查指標是否存在
     *
     * @param stockId         股票代碼
     * @param calculationDate 計算日期
     * @return 是否存在
     */
    boolean existsByStockIdAndCalculationDate(
            String stockId,
            LocalDate calculationDate
    );

    /**
     * 查詢股票的最新指標
     *
     * @param stockId 股票代碼
     * @return 最新技術指標（Optional）
     */
    @Query("SELECT t FROM TechnicalIndicator t WHERE t.stockId = :stockId " +
            "ORDER BY t.calculationDate DESC LIMIT 1")
    Optional<TechnicalIndicator> findLatestByStockId(@Param("stockId") String stockId);

    /**
     * 刪除指定日期之前的歷史資料
     *
     * @param date 日期
     * @return 刪除筆數
     */
    int deleteByCalculationDateBefore(LocalDate date);
}
