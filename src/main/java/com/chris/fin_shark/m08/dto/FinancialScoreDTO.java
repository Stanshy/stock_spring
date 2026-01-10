package com.chris.fin_shark.m08.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * 財務綜合評分 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialScoreDTO {

    /** 股票代碼 */
    private String stockId;

    /** 股票名稱 */
    private String stockName;

    /** 年度 */
    private Integer year;

    /** 季度 */
    private Integer quarter;

    /** 計算日期 */
    private LocalDate calculationDate;

    /** Piotroski F-Score (0-9) */
    private Integer piotroskiFScore;

    /** Piotroski 解讀 */
    private String piotroskiInterpretation;

    /** Piotroski 詳細分數 */
    private Map<String, Integer> piotroskiDetails;

    /** Altman Z-Score */
    private BigDecimal altmanZScore;

    /** Altman 狀態 */
    private String altmanStatus;

    /** Altman 解讀 */
    private String altmanInterpretation;

    /** Beneish M-Score */
    private BigDecimal beneishMScore;

    /** Beneish 狀態 */
    private String beneishStatus;

    /** Beneish 解讀 */
    private String beneishInterpretation;

    /** Graham 評分 (0-10) */
    private Integer grahamScore;

    /** 綜合評分 (0-100) */
    private BigDecimal compositeScore;

    /** 綜合評級 */
    private String compositeGrade;
}
