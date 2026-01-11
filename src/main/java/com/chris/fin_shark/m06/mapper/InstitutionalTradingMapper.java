package com.chris.fin_shark.m06.mapper;

import com.chris.fin_shark.m06.domain.InstitutionalTrading;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 三大法人買賣超 MyBatis Mapper
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
public interface InstitutionalTradingMapper {

    /**
     * 批次插入法人買賣超資料（UPSERT）
     *
     * @param tradingList 法人買賣超列表
     * @return 影響筆數
     */
    int batchInsert(@Param("tradingList") List<InstitutionalTrading> tradingList);

    /**
     * 批次更新法人買賣超資料
     *
     * @param tradingList 法人買賣超列表
     * @return 影響筆數
     */
    int batchUpdate(@Param("tradingList") List<InstitutionalTrading> tradingList);

    /**
     * 查詢法人連續買超天數
     *
     * @param stockId 股票代碼
     * @param days    查詢天數
     * @return 連續買超統計
     */
    List<InstitutionalTrading> getConsecutiveBuyDays(@Param("stockId") String stockId,
                                                      @Param("days") int days);

    /**
     * 查詢缺少的法人資料日期
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
     * 查詢法人買賣超排行
     *
     * @param tradeDate 交易日期
     * @param limit     筆數限制
     * @return 法人買賣超排行
     */
    List<InstitutionalTrading> getTopNetBuying(@Param("tradeDate") LocalDate tradeDate,
                                                @Param("limit") int limit);
}
