package com.chris.fin_shark.m06.repository;

import com.chris.fin_shark.m06.domain.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 股票資料 Repository
 * <p>
 * 提供股票基本資料的 CRUD 操作
 * 使用 Spring Data JPA
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, String> {

    /**
     * 根據市場類型查詢股票
     *
     * @param marketType 市場類型（TWSE/OTC/EMERGING）
     * @return 股票列表
     */
    List<Stock> findByMarketType(String marketType);

    /**
     * 查詢所有活躍股票
     *
     * @return 活躍股票列表
     */
    List<Stock> findByIsActiveTrue();


    /**
     * 查詢所有活躍股票代碼
     *
     * @return 股票代碼列表
     */
    @Query(value = """
        SELECT stock_id
        FROM stocks
        WHERE is_active = true
    """, nativeQuery = true)
    List<String> findActiveStockIds();


    /**
     * 根據產業查詢股票
     *
     * @param industry 產業別
     * @return 股票列表
     */
    List<Stock> findByIndustry(String industry);

    /**
     * 根據市場類型查詢活躍股票（分頁）
     *
     * @param marketType 市場類型
     * @param pageable   分頁參數
     * @return 股票分頁結果
     */
    Page<Stock> findByMarketTypeAndIsActiveTrue(String marketType, Pageable pageable);

    /**
     * 模糊查詢股票名稱（分頁）
     *
     * @param stockName 股票名稱（模糊查詢）
     * @param pageable  分頁參數
     * @return 股票分頁結果
     */
    Page<Stock> findByStockNameContaining(String stockName, Pageable pageable);

    /**
     * 根據股票代碼查詢（忽略大小寫）
     *
     * @param stockId 股票代碼
     * @return 股票（Optional）
     */
    Optional<Stock> findByStockIdIgnoreCase(String stockId);

    /**
     * 檢查股票是否存在
     *
     * @param stockId 股票代碼
     * @return 是否存在
     */
    boolean existsByStockId(String stockId);

    /**
     * 查詢指定市場類型的股票數量
     *
     * @param marketType 市場類型
     * @return 股票數量
     */
    long countByMarketType(String marketType);

    /**
     * 查詢活躍股票數量
     *
     * @return 活躍股票數量
     */
    long countByIsActiveTrue();
}
