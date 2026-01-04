package com.chris.fin_shark.m07.mapper;

import com.chris.fin_shark.m07.domain.TechnicalIndicator;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 技術指標 MyBatis Mapper
 * <p>
 * 功能編號: F-M07-001, F-M07-002
 * 功能名稱: 技術指標批次操作與複雜查詢
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper
public interface TechnicalIndicatorMapper {

    /**
     * 批次插入或更新技術指標
     * <p>
     * 使用 PostgreSQL ON CONFLICT DO UPDATE 避免重複插入
     * </p>
     *
     * @param indicators 技術指標列表
     * @return 影響筆數
     */
    int batchUpsert(@Param("indicators") List<TechnicalIndicator> indicators);

    /**
     * 批次查詢指定股票和日期的指標
     *
     * @param stockIds 股票代碼列表
     * @param date     計算日期
     * @return 技術指標列表
     */
    List<TechnicalIndicator> findByStockIdsAndDate(
            @Param("stockIds") List<String> stockIds,
            @Param("date") LocalDate date
    );

    /**
     * 查詢最新指標
     *
     * @param stockIds 股票代碼列表
     * @return 技術指標列表
     */
    List<TechnicalIndicator> findLatestIndicators(@Param("stockIds") List<String> stockIds);

    /**
     * 查詢黃金交叉候選股票
     * <p>
     * 偵測 MA5 上穿 MA20 的股票
     * </p>
     *
     * @param date 查詢日期
     * @return 黃金交叉候選股票資訊
     */
    List<Map<String, Object>> findGoldenCrossCandidates(@Param("date") LocalDate date);

    /**
     * 查詢死亡交叉候選股票
     * <p>
     * 偵測 MA5 下穿 MA20 的股票
     * </p>
     *
     * @param date 查詢日期
     * @return 死亡交叉候選股票資訊
     */
    List<Map<String, Object>> findDeathCrossCandidates(@Param("date") LocalDate date);

    /**
     * 查詢超買股票
     * <p>
     * RSI > 70 或 KD > 80
     * </p>
     *
     * @param date 查詢日期
     * @return 超買股票列表
     */
    List<Map<String, Object>> findOverboughtStocks(@Param("date") LocalDate date);

    /**
     * 查詢超賣股票
     * <p>
     * RSI < 30 或 KD < 20
     * </p>
     *
     * @param date 查詢日期
     * @return 超賣股票列表
     */
    List<Map<String, Object>> findOversoldStocks(@Param("date") LocalDate date);

    /**
     * 計算並回寫移動平均線
     * <p>
     * 回寫至 stock_prices 表的 ma5, ma20, volume_ma5
     * </p>
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 更新筆數
     */
    int updateMovingAveragesToStockPrices(
            @Param("stockId") String stockId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
