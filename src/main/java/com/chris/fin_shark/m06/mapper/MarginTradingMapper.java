package com.chris.fin_shark.m06.mapper;

import com.chris.fin_shark.m06.domain.MarginTrading;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 融資融券 MyBatis Mapper
 * <p>
 * 功能編號: F-M06-004
 * 功能名稱: 籌碼資料同步
 * 用於批次操作和複雜查詢
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper
public interface MarginTradingMapper {

    /**
     * 批次插入融資融券資料（UPSERT）
     *
     * @param marginList 融資融券列表
     * @return 影響筆數
     */
    int batchInsert(@Param("marginList") List<MarginTrading> marginList);

    /**
     * 批次更新融資融券資料
     *
     * @param marginList 融資融券列表
     * @return 影響筆數
     */
    int batchUpdate(@Param("marginList") List<MarginTrading> marginList);

    /**
     * 查詢融資增減趨勢
     *
     * @param stockId 股票代碼
     * @param days    查詢天數
     * @return 融資融券趨勢
     */
    List<MarginTrading> getMarginTrend(@Param("stockId") String stockId,
                                        @Param("days") int days);

    /**
     * 查詢缺少的融資融券資料日期
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 缺少資料的日期列表
     */
    List<LocalDate> findMissingDates(@Param("stockId") String stockId,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

    /**
     * 查詢券資比排行
     *
     * @param tradeDate 交易日期
     * @param limit     筆數限制
     * @return 券資比排行
     */
    List<MarginTrading> getTopShortRatio(@Param("tradeDate") LocalDate tradeDate,
                                          @Param("limit") int limit);
}
