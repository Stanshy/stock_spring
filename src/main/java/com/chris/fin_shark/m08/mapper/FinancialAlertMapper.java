package com.chris.fin_shark.m08.mapper;

import com.chris.fin_shark.m08.domain.FinancialAlert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 財務異常警示 MyBatis Mapper
 * <p>
 * 功能編號: F-M08-012
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper
public interface FinancialAlertMapper {

    /**
     * 批次插入警示
     *
     * @param alerts 警示列表
     * @return 插入筆數
     */
    int batchInsert(@Param("alerts") List<FinancialAlert> alerts);

    /**
     * 批次更新警示狀態
     *
     * @param alertIds 警示 ID 列表
     * @param status   新狀態
     * @return 更新筆數
     */
    int batchUpdateStatus(@Param("alertIds") List<Long> alertIds,
                          @Param("status") String status);

    // ========== P1 進階功能（TODO） ==========

    /**
     * TODO: P1 - 查詢警示統計資訊
     */
    // AlertStatisticsVO queryStatistics(...);
}
