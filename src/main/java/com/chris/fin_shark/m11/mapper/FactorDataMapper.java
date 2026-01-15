package com.chris.fin_shark.m11.mapper;

import com.chris.fin_shark.m11.dto.FactorDataDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 因子數據 MyBatis Mapper
 * <p>
 * 用於跨模組（M06/M07/M08/M09）載入因子數據
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper
public interface FactorDataMapper {

    /**
     * 載入策略執行所需的因子數據
     *
     * @param tradeDate        交易日期
     * @param latestReportDate 最近財報日期（用於 M08）
     * @param marketType       市場類型
     * @param minVolume        最低成交量
     * @param excludeEtf       是否排除 ETF
     * @param stockIds         指定股票清單（可選）
     * @param industries       指定產業（可選）
     * @return 因子數據列表
     */
    List<FactorDataDTO> loadFactorData(
            @Param("tradeDate") LocalDate tradeDate,
            @Param("latestReportDate") LocalDate latestReportDate,
            @Param("marketType") String marketType,
            @Param("minVolume") Integer minVolume,
            @Param("excludeEtf") boolean excludeEtf,
            @Param("stockIds") List<String> stockIds,
            @Param("industries") List<String> industries);

    /**
     * 載入單一股票的因子數據
     */
    FactorDataDTO loadFactorDataForStock(
            @Param("stockId") String stockId,
            @Param("tradeDate") LocalDate tradeDate,
            @Param("latestReportDate") LocalDate latestReportDate);

    /**
     * 取得最近的財報日期
     */
    LocalDate getLatestReportDate();
}
