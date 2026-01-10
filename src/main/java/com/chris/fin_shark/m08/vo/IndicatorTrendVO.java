package com.chris.fin_shark.m08.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 指標歷史趨勢 VO
 * <p>
 * 用於 MyBatis 查詢結果映射
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorTrendVO {

    /** 股票代碼 */
    private String stockId;

    /** 年度 */
    private Integer year;

    /** 季度 */
    private Integer quarter;

    /** 指標值 */
    private BigDecimal value;
}
