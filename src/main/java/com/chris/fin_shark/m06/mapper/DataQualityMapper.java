package com.chris.fin_shark.m06.mapper;

import com.chris.fin_shark.m06.vo.MissingDataVO;
import com.chris.fin_shark.m06.vo.QualityCheckExecutionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 資料品質檢核 MyBatis Mapper
 * <p>
 * 功能編號: F-M06-006
 * 功能名稱: 資料品質檢核
 * 用於執行品質檢核 SQL 和複雜查詢
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper
public interface DataQualityMapper {

    /**
     * 檢核股價資料完整性（找出缺漏日期）
     *
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 缺漏資料列表
     */
    List<MissingDataVO> checkStockPriceCompleteness(@Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    /**
     * 檢核法人資料完整性
     *
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 缺漏資料列表
     */
    List<MissingDataVO> checkInstitutionalCompleteness(@Param("startDate") LocalDate startDate,
                                                        @Param("endDate") LocalDate endDate);

    /**
     * 檢核融資融券資料完整性
     *
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 缺漏資料列表
     */
    List<MissingDataVO> checkMarginCompleteness(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    /**
     * 檢核股價四價關係（low <= open,close <= high）
     *
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 違反規則的資料列表
     */
    List<QualityCheckExecutionVO> checkPriceRelationship(@Param("startDate") LocalDate startDate,
                                                          @Param("endDate") LocalDate endDate);

    /**
     * 檢核資料時效性（最新資料是否為最近交易日）
     *
     * @param tableName 資料表名稱
     * @return 檢核結果
     */
    QualityCheckExecutionVO checkDataTimeliness(@Param("tableName") String tableName);

    /**
     * 統計各資料表的資料筆數
     *
     * @return 各資料表統計
     */
    List<QualityCheckExecutionVO> getDataStatistics();
}
