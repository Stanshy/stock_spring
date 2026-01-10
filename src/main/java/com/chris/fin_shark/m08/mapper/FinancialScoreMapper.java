package com.chris.fin_shark.m08.mapper;

import com.chris.fin_shark.m08.domain.FinancialScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 財務綜合評分 MyBatis Mapper
 * <p>
 * 功能編號: F-M08-009
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper
public interface FinancialScoreMapper {

    /**
     * 批次插入綜合評分
     *
     * @param scores 評分列表
     * @return 插入筆數
     */
    int batchUpsert(@Param("scores") List<FinancialScore> scores);

    /**
     * 批次查詢綜合評分
     *
     * @param stockIds 股票代碼列表
     * @param year     年度
     * @param quarter  季度
     * @return 評分列表
     */
    List<FinancialScore> batchQuery(@Param("stockIds") List<String> stockIds,
                                    @Param("year") Integer year,
                                    @Param("quarter") Integer quarter);

    // ========== P1 進階功能（TODO） ==========

    /**
     * TODO: P1 - 查詢 Piotroski F-Score 排名前 N 股票
     */
    // List<FinancialScore> queryTopByPiotroskiScore(...);

    /**
     * TODO: P1 - 查詢 Altman Z-Score 低於門檻的股票（破產風險）
     */
    // List<FinancialScore> queryLowAltmanScore(...);
}
