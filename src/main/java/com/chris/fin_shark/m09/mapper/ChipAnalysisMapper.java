package com.chris.fin_shark.m09.mapper;

import com.chris.fin_shark.m09.domain.ChipAnalysisResult;
import com.chris.fin_shark.m09.dto.ChipRankingDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 籌碼分析 MyBatis Mapper
 * <p>
 * 功能編號: F-M09-001, F-M09-002, F-M09-005, F-M09-007
 * 功能名稱: 籌碼指標批次操作、排行榜查詢
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper
public interface ChipAnalysisMapper {

    /**
     * 批次插入或更新籌碼分析結果
     * <p>
     * 使用 PostgreSQL ON CONFLICT DO UPDATE 避免重複插入
     * </p>
     *
     * @param results 籌碼分析結果列表
     * @return 影響筆數
     */
    int batchUpsertResults(@Param("list") List<ChipAnalysisResult> results);

    /**
     * 查詢籌碼排行榜
     *
     * @param tradeDate      交易日期
     * @param orderBy        排序欄位
     * @param orderDirection 排序方向（ASC/DESC）
     * @param valueColumn    取值欄位
     * @param marketType     市場類型（可選）
     * @param minVolume      最小成交量（可選）
     * @param limit          筆數限制
     * @return 排行榜列表
     */
    List<ChipRankingDTO> selectRanking(
            @Param("tradeDate") LocalDate tradeDate,
            @Param("orderBy") String orderBy,
            @Param("orderDirection") String orderDirection,
            @Param("valueColumn") String valueColumn,
            @Param("marketType") String marketType,
            @Param("minVolume") Long minVolume,
            @Param("limit") int limit
    );

    /**
     * 查詢外資買超排行
     *
     * @param tradeDate  交易日期
     * @param marketType 市場類型（可選）
     * @param limit      筆數限制
     * @return 排行榜列表
     */
    List<ChipRankingDTO> selectForeignBuyRanking(
            @Param("tradeDate") LocalDate tradeDate,
            @Param("marketType") String marketType,
            @Param("limit") int limit
    );

    /**
     * 查詢外資賣超排行
     *
     * @param tradeDate  交易日期
     * @param marketType 市場類型（可選）
     * @param limit      筆數限制
     * @return 排行榜列表
     */
    List<ChipRankingDTO> selectForeignSellRanking(
            @Param("tradeDate") LocalDate tradeDate,
            @Param("marketType") String marketType,
            @Param("limit") int limit
    );

    /**
     * 查詢投信買超排行
     *
     * @param tradeDate  交易日期
     * @param marketType 市場類型（可選）
     * @param limit      筆數限制
     * @return 排行榜列表
     */
    List<ChipRankingDTO> selectTrustBuyRanking(
            @Param("tradeDate") LocalDate tradeDate,
            @Param("marketType") String marketType,
            @Param("limit") int limit
    );

    /**
     * 查詢投信賣超排行
     *
     * @param tradeDate  交易日期
     * @param marketType 市場類型（可選）
     * @param limit      筆數限制
     * @return 排行榜列表
     */
    List<ChipRankingDTO> selectTrustSellRanking(
            @Param("tradeDate") LocalDate tradeDate,
            @Param("marketType") String marketType,
            @Param("limit") int limit
    );

    /**
     * 查詢外資連續買超排行
     *
     * @param tradeDate  交易日期
     * @param marketType 市場類型（可選）
     * @param limit      筆數限制
     * @return 排行榜列表
     */
    List<ChipRankingDTO> selectForeignContinuousBuyRanking(
            @Param("tradeDate") LocalDate tradeDate,
            @Param("marketType") String marketType,
            @Param("limit") int limit
    );

    /**
     * 查詢融資增加排行
     *
     * @param tradeDate  交易日期
     * @param marketType 市場類型（可選）
     * @param limit      筆數限制
     * @return 排行榜列表
     */
    List<ChipRankingDTO> selectMarginIncreaseRanking(
            @Param("tradeDate") LocalDate tradeDate,
            @Param("marketType") String marketType,
            @Param("limit") int limit
    );

    /**
     * 查詢融資減少排行
     *
     * @param tradeDate  交易日期
     * @param marketType 市場類型（可選）
     * @param limit      筆數限制
     * @return 排行榜列表
     */
    List<ChipRankingDTO> selectMarginDecreaseRanking(
            @Param("tradeDate") LocalDate tradeDate,
            @Param("marketType") String marketType,
            @Param("limit") int limit
    );

    /**
     * 查詢券資比排行
     *
     * @param tradeDate  交易日期
     * @param marketType 市場類型（可選）
     * @param limit      筆數限制
     * @return 排行榜列表
     */
    List<ChipRankingDTO> selectShortRatioRanking(
            @Param("tradeDate") LocalDate tradeDate,
            @Param("marketType") String marketType,
            @Param("limit") int limit
    );

    /**
     * 查詢三大法人合計買超排行
     *
     * @param tradeDate  交易日期
     * @param marketType 市場類型（可選）
     * @param limit      筆數限制
     * @return 排行榜列表
     */
    List<ChipRankingDTO> selectTotalNetBuyRanking(
            @Param("tradeDate") LocalDate tradeDate,
            @Param("marketType") String marketType,
            @Param("limit") int limit
    );

    /**
     * 批次查詢指定股票和日期的籌碼分析結果
     *
     * @param stockIds 股票代碼列表
     * @param date     交易日期
     * @return 籌碼分析結果列表
     */
    List<ChipAnalysisResult> findByStockIdsAndDate(
            @Param("stockIds") List<String> stockIds,
            @Param("date") LocalDate date
    );

    /**
     * 統計指定日期的籌碼分析摘要
     *
     * @param tradeDate 交易日期
     * @return 統計摘要
     */
    Map<String, Object> selectDailySummary(@Param("tradeDate") LocalDate tradeDate);
}
