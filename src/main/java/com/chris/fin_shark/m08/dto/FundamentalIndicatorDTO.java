package com.chris.fin_shark.m08.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * 基本面財務指標 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundamentalIndicatorDTO {

    /** 股票代碼 */
    private String stockId;

    /** 股票名稱 */
    private String stockName;

    /** 年度 */
    private Integer year;

    /** 季度 */
    private Integer quarter;

    /** 報表類型 */
    private String reportType;

    /** 計算日期 */
    private LocalDate calculationDate;

    /** 股價 */
    private BigDecimal stockPrice;

    /** 估值指標 */
    private ValuationIndicatorsDTO valuation;

    /** 獲利能力指標 */
    private ProfitabilityIndicatorsDTO profitability;

    /** 財務結構指標 */
    private FinancialStructureIndicatorsDTO financialStructure;

    /** 償債能力指標 */
    private SolvencyIndicatorsDTO solvency;

    /** 經營效率指標 */
    private EfficiencyIndicatorsDTO efficiency;

    /** 現金流量指標 */
    private CashFlowIndicatorsDTO cashFlow;

    /** 成長性指標 */
    private GrowthIndicatorsDTO growth;

    /** 股利政策指標 */
    private DividendIndicatorsDTO dividend;

    /**
     * 估值指標子 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValuationIndicatorsDTO {
        /** 本益比 */
        private BigDecimal peRatio;
        /** 預估本益比 */
        private BigDecimal forwardPe;
        /** 追蹤本益比 */
        private BigDecimal trailingPe;
        /** 股價淨值比 */
        private BigDecimal pbRatio;
        /** 股價營收比 */
        private BigDecimal psRatio;
        /** 股價現金流比 */
        private BigDecimal pcfRatio;
        /** 股價自由現金流比 */
        private BigDecimal pfcfRatio;
        /** PEG 比率 */
        private BigDecimal pegRatio;
        /** EV/EBITDA */
        private BigDecimal evEbitda;
        /** Tobin's Q */
        private BigDecimal tobinsQ;
        /** Graham Number */
        private BigDecimal grahamNumber;
        /** 企業價值 */
        private BigDecimal ev;
        /** 市值 */
        private BigDecimal marketCap;
        /** 其他估值指標 */
        private Map<String, BigDecimal> others;
    }

    /**
     * 獲利能力指標子 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfitabilityIndicatorsDTO {
        /** 毛利率 */
        private BigDecimal grossMargin;
        /** 營業利益率 */
        private BigDecimal operatingMargin;
        /** 淨利率 */
        private BigDecimal netMargin;
        /** EBITDA 利益率 */
        private BigDecimal ebitdaMargin;
        /** ROE (股東權益報酬率) */
        private BigDecimal roe;
        /** ROA (資產報酬率) */
        private BigDecimal roa;
        /** ROIC (投資資本報酬率) */
        private BigDecimal roic;
        /** ROTA (總資產報酬率) */
        private BigDecimal rota;
        /** 每股盈餘 */
        private BigDecimal eps;
        /** 稀釋每股盈餘 */
        private BigDecimal dilutedEps;
        /** 營業每股盈餘 */
        private BigDecimal operatingEps;
        /** EBITDA */
        private BigDecimal ebitda;
        /** 其他獲利指標 */
        private Map<String, BigDecimal> others;
    }

    /**
     * 財務結構指標子 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialStructureIndicatorsDTO {
        /** 負債權益比 */
        private BigDecimal debtToEquity;
        /** 負債比率 */
        private BigDecimal debtRatio;
        /** 權益比率 */
        private BigDecimal equityRatio;
        /** 長期負債權益比 */
        private BigDecimal longTermDebtToEquity;
        /** 其他結構指標 */
        private Map<String, BigDecimal> others;
    }

    /**
     * 償債能力指標子 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SolvencyIndicatorsDTO {
        /** 流動比率 */
        private BigDecimal currentRatio;
        /** 速動比率 */
        private BigDecimal quickRatio;
        /** 現金比率 */
        private BigDecimal cashRatio;
        /** 利息保障倍數 */
        private BigDecimal interestCoverage;
        /** 債務償還保障比率 */
        private BigDecimal debtServiceCoverage;
        /** 營運現金流對流動負債比 */
        private BigDecimal ocfToCurrentLiabilities;
        /** 其他償債指標 */
        private Map<String, BigDecimal> others;
    }

    /**
     * 經營效率指標子 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EfficiencyIndicatorsDTO {
        /** 總資產週轉率 */
        private BigDecimal assetTurnover;
        /** 存貨週轉率 */
        private BigDecimal inventoryTurnover;
        /** 應收帳款週轉率 */
        private BigDecimal receivablesTurnover;
        /** 應付帳款週轉率 */
        private BigDecimal payablesTurnover;
        /** 存貨週轉天數 DIO */
        private BigDecimal dio;
        /** 應收帳款週轉天數 DSO */
        private BigDecimal dso;
        /** 應付帳款週轉天數 DPO */
        private BigDecimal dpo;
        /** 現金轉換週期 */
        private BigDecimal cashConversionCycle;
        /** 營運週期 */
        private BigDecimal operatingCycle;
        /** 總資產週轉率（完整） */
        private BigDecimal totalAssetTurnover;
        /** 其他效率指標 */
        private Map<String, BigDecimal> others;
    }

    /**
     * 現金流量指標子 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CashFlowIndicatorsDTO {
        /** 營運現金流 */
        private BigDecimal operatingCashFlow;
        /** 自由現金流 */
        private BigDecimal freeCashFlow;
        /** FCF 殖利率 */
        private BigDecimal fcfYield;
        /** 營運現金流比率 */
        private BigDecimal ocfRatio;
        /** 現金流對營收比 */
        private BigDecimal cfToSales;
        /** 應計比率 */
        private BigDecimal accrualRatio;
        /** 其他現金流指標 */
        private Map<String, BigDecimal> others;
    }

    /**
     * 成長性指標子 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrowthIndicatorsDTO {
        /** 營收成長率 YoY */
        private BigDecimal revenueGrowthYoy;
        /** 營收成長率 QoQ */
        private BigDecimal revenueGrowthQoq;
        /** EPS 成長率 YoY */
        private BigDecimal epsGrowthYoy;
        /** EPS 成長率 QoQ */
        private BigDecimal epsGrowthQoq;
        /** 淨利成長率 YoY */
        private BigDecimal netIncomeGrowthYoy;
        /** ROE 成長率 YoY */
        private BigDecimal roeGrowthYoy;
        /** 營收年複合成長率 3年 */
        private BigDecimal revenueCagr3y;
        /** 營收年複合成長率 5年 */
        private BigDecimal revenueCagr5y;
        /** EPS 年複合成長率 3年 */
        private BigDecimal epsCagr3y;
        /** EPS 年複合成長率 5年 */
        private BigDecimal epsCagr5y;
        /** 其他成長指標 */
        private Map<String, BigDecimal> others;
    }

    /**
     * 股利政策指標子 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DividendIndicatorsDTO {
        /** 現金殖利率 */
        private BigDecimal dividendYield;
        /** 股利發放率 */
        private BigDecimal dividendPayoutRatio;
        /** 每股股利 */
        private BigDecimal dividendPerShare;
        /** 股利成長率 YoY */
        private BigDecimal dividendGrowthYoy;
        /** 股利保障倍數 */
        private BigDecimal dividendCoverage;
        /** 其他股利指標 */
        private Map<String, BigDecimal> others;
    }
}