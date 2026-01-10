package com.chris.fin_shark.m08.converter;

import com.chris.fin_shark.m08.domain.FundamentalIndicator;
import com.chris.fin_shark.m08.dto.FundamentalIndicatorDTO;
import com.chris.fin_shark.m08.dto.FundamentalIndicatorDTO.*;
import com.chris.fin_shark.m08.engine.model.CalculationResult;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基本面財務指標 Converter
 * 🔴 使用自定義映射方法處理 Map 到子 DTO 的轉換
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FundamentalIndicatorConverter {

    /**
     * Entity → DTO
     * 🔴 使用自定義映射方法
     */
    @Mapping(target = "stockName", ignore = true)
    @Mapping(source = "valuationIndicators", target = "valuation", qualifiedByName = "toValuationDTO")
    @Mapping(source = "profitabilityIndicators", target = "profitability", qualifiedByName = "toProfitabilityDTO")
    @Mapping(source = "financialStructureIndicators", target = "financialStructure", qualifiedByName = "toFinancialStructureDTO")
    @Mapping(source = "solvencyIndicators", target = "solvency", qualifiedByName = "toSolvencyDTO")
    @Mapping(source = "efficiencyIndicators", target = "efficiency", qualifiedByName = "toEfficiencyDTO")
    @Mapping(source = "cashFlowIndicators", target = "cashFlow", qualifiedByName = "toCashFlowDTO")
    @Mapping(source = "growthIndicators", target = "growth", qualifiedByName = "toGrowthDTO")
    @Mapping(source = "dividendIndicators", target = "dividend", qualifiedByName = "toDividendDTO")
    FundamentalIndicatorDTO toDTO(FundamentalIndicator entity);

    /**
     * Entity List → DTO List
     */
    List<FundamentalIndicatorDTO> toDTOList(List<FundamentalIndicator> entities);

    // ========== 自定義映射方法 ==========

    /**
     * 🔴 估值指標映射
     */
    @Named("toValuationDTO")
    default ValuationIndicatorsDTO toValuationDTO(Map<String, BigDecimal> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        return ValuationIndicatorsDTO.builder()
                .peRatio(source.get("pe_ratio"))
                .forwardPe(source.get("forward_pe"))
                .trailingPe(source.get("trailing_pe"))
                .pbRatio(source.get("pb_ratio"))
                .psRatio(source.get("ps_ratio"))
                .pcfRatio(source.get("pcf_ratio"))
                .pfcfRatio(source.get("pfcf_ratio"))
                .pegRatio(source.get("peg_ratio"))
                .evEbitda(source.get("ev_ebitda"))
                .tobinsQ(source.get("tobins_q"))
                .grahamNumber(source.get("graham_number"))
                .ev(source.get("ev"))
                .marketCap(source.get("market_cap"))
                .others(source)
                .build();
    }

    /**
     * 🔴 獲利能力指標映射
     */
    @Named("toProfitabilityDTO")
    default ProfitabilityIndicatorsDTO toProfitabilityDTO(Map<String, BigDecimal> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        return ProfitabilityIndicatorsDTO.builder()
                .grossMargin(source.get("gross_margin"))
                .operatingMargin(source.get("operating_margin"))
                .netMargin(source.get("net_margin"))
                .ebitdaMargin(source.get("ebitda_margin"))
                .roe(source.get("roe"))
                .roa(source.get("roa"))
                .roic(source.get("roic"))
                .rota(source.get("rota"))
                .eps(source.get("eps"))
                .dilutedEps(source.get("diluted_eps"))
                .operatingEps(source.get("operating_eps"))
                .ebitda(source.get("ebitda"))
                .others(source)
                .build();
    }

    /**
     * 🔴 財務結構指標映射
     */
    @Named("toFinancialStructureDTO")
    default FinancialStructureIndicatorsDTO toFinancialStructureDTO(Map<String, BigDecimal> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        return FinancialStructureIndicatorsDTO.builder()
                .debtToEquity(source.get("debt_to_equity"))
                .debtRatio(source.get("debt_ratio"))
                .equityRatio(source.get("equity_ratio"))
                .longTermDebtToEquity(source.get("long_term_debt_to_equity"))
                .others(source)
                .build();
    }

    /**
     * 🔴 償債能力指標映射
     */
    @Named("toSolvencyDTO")
    default SolvencyIndicatorsDTO toSolvencyDTO(Map<String, BigDecimal> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        return SolvencyIndicatorsDTO.builder()
                .currentRatio(source.get("current_ratio"))
                .quickRatio(source.get("quick_ratio"))
                .cashRatio(source.get("cash_ratio"))
                .interestCoverage(source.get("interest_coverage"))
                .debtServiceCoverage(source.get("debt_service_coverage"))
                .ocfToCurrentLiabilities(source.get("ocf_to_current_liabilities"))
                .others(source)
                .build();
    }

    /**
     * 🔴 經營效率指標映射
     */
    @Named("toEfficiencyDTO")
    default EfficiencyIndicatorsDTO toEfficiencyDTO(Map<String, BigDecimal> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        return EfficiencyIndicatorsDTO.builder()
                .assetTurnover(source.get("asset_turnover"))
                .inventoryTurnover(source.get("inventory_turnover"))
                .receivablesTurnover(source.get("receivables_turnover"))
                .payablesTurnover(source.get("payables_turnover"))
                .dio(source.get("dio"))
                .dso(source.get("dso"))
                .dpo(source.get("dpo"))
                .cashConversionCycle(source.get("cash_conversion_cycle"))
                .operatingCycle(source.get("operating_cycle"))
                .totalAssetTurnover(source.get("total_asset_turnover"))
                .others(source)
                .build();
    }

    /**
     * 🔴 現金流量指標映射
     */
    @Named("toCashFlowDTO")
    default CashFlowIndicatorsDTO toCashFlowDTO(Map<String, BigDecimal> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        return CashFlowIndicatorsDTO.builder()
                .operatingCashFlow(source.get("operating_cash_flow"))
                .freeCashFlow(source.get("free_cash_flow"))
                .fcfYield(source.get("fcf_yield"))
                .ocfRatio(source.get("ocf_ratio"))
                .cfToSales(source.get("cf_to_sales"))
                .accrualRatio(source.get("accrual_ratio"))
                .others(source)
                .build();
    }

    /**
     * 🔴 成長性指標映射
     */
    @Named("toGrowthDTO")
    default GrowthIndicatorsDTO toGrowthDTO(Map<String, BigDecimal> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        return GrowthIndicatorsDTO.builder()
                .revenueGrowthYoy(source.get("revenue_growth_yoy"))
                .revenueGrowthQoq(source.get("revenue_growth_qoq"))
                .epsGrowthYoy(source.get("eps_growth_yoy"))
                .epsGrowthQoq(source.get("eps_growth_qoq"))
                .netIncomeGrowthYoy(source.get("net_income_growth_yoy"))
                .roeGrowthYoy(source.get("roe_growth_yoy"))
                .revenueCagr3y(source.get("revenue_cagr_3y"))
                .revenueCagr5y(source.get("revenue_cagr_5y"))
                .epsCagr3y(source.get("eps_cagr_3y"))
                .epsCagr5y(source.get("eps_cagr_5y"))
                .others(source)
                .build();
    }

    /**
     * 🔴 股利政策指標映射
     */
    @Named("toDividendDTO")
    default DividendIndicatorsDTO toDividendDTO(Map<String, BigDecimal> source) {
        if (source == null || source.isEmpty()) {
            return null;
        }
        return DividendIndicatorsDTO.builder()
                .dividendYield(source.get("dividend_yield"))
                .dividendPayoutRatio(source.get("dividend_payout_ratio"))
                .dividendPerShare(source.get("dividend_per_share"))
                .dividendGrowthYoy(source.get("dividend_growth_yoy"))
                .dividendCoverage(source.get("dividend_coverage"))
                .others(source)
                .build();
    }

    /**
     * CalculationResult → Entity
     * ✅ 這部分不需要自定義映射，MapStruct 可以自動處理 Map 到 Map
     */
    @Mapping(source = "stockId", target = "stockId")
    @Mapping(source = "year", target = "year")
    @Mapping(source = "quarter", target = "quarter")
    @Mapping(source = "valuationIndicators", target = "valuationIndicators")
    @Mapping(source = "profitabilityIndicators", target = "profitabilityIndicators")
    @Mapping(source = "financialStructureIndicators", target = "financialStructureIndicators")
    @Mapping(source = "solvencyIndicators", target = "solvencyIndicators")
    // @Mapping(source = "efficiencyIndicators", target = "efficiencyIndicators")
    @Mapping(source = "cashFlowIndicators", target = "cashFlowIndicators")
    @Mapping(source = "growthIndicators", target = "growthIndicators")
    @Mapping(source = "dividendIndicators", target = "dividendIndicators")
    FundamentalIndicator toEntity(CalculationResult result);
}