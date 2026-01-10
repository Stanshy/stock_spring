package com.chris.fin_shark.m08.mapper;

import com.chris.fin_shark.m08.domain.FundamentalIndicator;
import com.chris.fin_shark.m08.vo.IndicatorTrendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 基本面財務指標 MyBatis Mapper
 * <p>
 * 功能編號: F-M08-001 ~ F-M08-008
 * 處理批次操作與複雜查詢
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper
public interface FundamentalIndicatorMapper {

    /**
     * 批次插入財務指標（P0 核心功能）
     * <p>
     * 使用 ON CONFLICT DO UPDATE 避免重複插入
     * </p>
     *
     * @param indicators 財務指標列表
     * @return 插入筆數
     */
    int batchUpsert(@Param("indicators") List<FundamentalIndicator> indicators);

    /**
     * 批次查詢財務指標（P0 核心功能）
     *
     * @param stockIds 股票代碼列表
     * @param year     年度
     * @param quarter  季度
     * @return 財務指標列表
     */
    List<FundamentalIndicator> batchQuery(@Param("stockIds") List<String> stockIds,
                                          @Param("year") Integer year,
                                          @Param("quarter") Integer quarter);

    /**
     * 查詢指標歷史趨勢（P0 核心功能）
     *
     * @param stockId     股票代碼
     * @param indicator   指標名稱
     * @param startYear   起始年度
     * @param startQuarter 起始季度
     * @param endYear     結束年度
     * @param endQuarter  結束季度
     * @return 趨勢資料列表
     */
    List<IndicatorTrendVO> queryTrend(@Param("stockId") String stockId,
                                      @Param("indicator") String indicator,
                                      @Param("startYear") Integer startYear,
                                      @Param("startQuarter") Integer startQuarter,
                                      @Param("endYear") Integer endYear,
                                      @Param("endQuarter") Integer endQuarter);

    /**
     * 查詢需要更新的股票清單
     * <p>
     * 條件：財報已更新但指標尚未計算
     * </p>
     *
     * @param year    年度
     * @param quarter 季度
     * @return 股票代碼列表
     */
    List<String> findStocksNeedingUpdate(@Param("year") Integer year,
                                         @Param("quarter") Integer quarter);

    // ========== P1 進階功能（TODO） ==========

    /**
     * TODO: P1 - 查詢指標排名
     *
     * @param indicator 指標名稱
     * @param year      年度
     * @param quarter   季度
     * @param ascending 是否升序
     * @param limit     數量限制
     * @return 排名列表
     */
    // List<IndicatorRankingVO> queryRanking(...);

    /**
     * TODO: P1 - 查詢指標統計資訊
     *
     * @param indicator 指標名稱
     * @param year      年度
     * @param quarter   季度
     * @return 統計資訊
     */
    // IndicatorStatisticsVO queryStatistics(...);
}
