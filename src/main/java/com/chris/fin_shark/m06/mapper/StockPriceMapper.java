package com.chris.fin_shark.m06.mapper;

import com.chris.fin_shark.m06.domain.StockPrice;
import com.chris.fin_shark.m06.vo.StockPriceStatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 股價 MyBatis Mapper
 * <p>
 * 功能編號: F-M06-002
 * 功能名稱: 股價資料同步（批次操作）
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper
public interface StockPriceMapper {

    /**
     * 批次插入股價資料
     * <p>
     * 使用 ON CONFLICT DO UPDATE 避免重複插入
     * </p>
     *
     * @param prices 股價列表
     * @return 插入筆數
     */
    int batchInsert(@Param("prices") List<StockPrice> prices);

    /**
     * 批次更新股價資料
     *
     * @param prices 股價列表
     * @return 更新筆數
     */
    int batchUpdate(@Param("prices") List<StockPrice> prices);

    /**
     * 查詢股價統計資訊
     * <p>
     * 計算指定期間的 MA5, MA20, 漲跌幅等
     * </p>
     *
     * @param stockId 股票代碼
     * @param days    查詢天數
     * @return 股價統計列表
     */
    List<StockPriceStatisticsVO> getStatistics(@Param("stockId") String stockId,
                                               @Param("days") int days);

    /**
     * 更新移動平均線
     * <p>
     * 計算並更新 MA5, MA20, volume_ma5
     * </p>
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 更新筆數
     */
    int updateMovingAverages(@Param("stockId") String stockId,
                             @Param("startDate") LocalDate startDate,
                             @Param("endDate") LocalDate endDate);

    /**
     * 查詢缺少的股價日期
     * <p>
     * 找出交易日有但股價表沒有的日期
     * </p>
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 缺少的日期列表
     */
    List<LocalDate> findMissingDates(@Param("stockId") String stockId,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);
}
