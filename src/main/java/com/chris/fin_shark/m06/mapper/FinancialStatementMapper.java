package com.chris.fin_shark.m06.mapper;

import com.chris.fin_shark.m06.domain.FinancialStatement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 財務報表 MyBatis Mapper
 * <p>
 * 功能編號: F-M06-003
 * 功能名稱: 財報資料同步
 * 用於批次操作和複雜查詢
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper
public interface FinancialStatementMapper {

    /**
     * 批次插入財報資料（UPSERT）
     *
     * @param statements 財報列表
     * @return 影響筆數
     */
    int batchInsert(@Param("statements") List<FinancialStatement> statements);

    /**
     * 批次更新財報資料
     *
     * @param statements 財報列表
     * @return 影響筆數
     */
    int batchUpdate(@Param("statements") List<FinancialStatement> statements);

    /**
     * 查詢財務比率趨勢（多季度）
     *
     * @param stockId  股票代碼
     * @param quarters 查詢季度數
     * @return 財報趨勢
     */
    List<FinancialStatement> getFinancialTrend(@Param("stockId") String stockId,
                                                @Param("quarters") int quarters);

    /**
     * 查詢缺少的財報期間
     *
     * @param stockId   股票代碼
     * @param startYear 開始年度
     * @param endYear   結束年度
     * @return 缺少財報的期間列表（year-quarter 格式）
     */
    List<String> findMissingPeriods(@Param("stockId") String stockId,
                                     @Param("startYear") int startYear,
                                     @Param("endYear") int endYear);

    /**
     * 查詢 ROE 排行
     *
     * @param year    年度
     * @param quarter 季度
     * @param limit   筆數限制
     * @return ROE 排行
     */
    List<FinancialStatement> getTopROE(@Param("year") int year,
                                        @Param("quarter") short quarter,
                                        @Param("limit") int limit);
}
