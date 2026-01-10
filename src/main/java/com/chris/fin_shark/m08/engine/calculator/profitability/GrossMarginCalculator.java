package com.chris.fin_shark.m08.engine.calculator.profitability;

import com.chris.fin_shark.m08.engine.FundamentalCalculator;
import com.chris.fin_shark.m08.engine.model.CalculationResult;
import com.chris.fin_shark.m08.engine.model.FinancialData;
import com.chris.fin_shark.m08.engine.model.IndicatorMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 毛利率計算器
 * <p>
 * 功能編號: F-M08-002
 * 計算公式: 毛利率 = (營收 - 營業成本) / 營收 × 100%
 * 說明: 衡量產品銷售的基本獲利能力
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
public class GrossMarginCalculator implements FundamentalCalculator {

    private static final String INDICATOR_NAME = "gross_margin";
    private static final String DISPLAY_NAME = "毛利率";
    private static final String CATEGORY = "PROFITABILITY";

    @Override
    public void calculate(FinancialData data, CalculationResult result) {
        try {
            // 1. 驗證必要欄位
            if (data.getRevenue() == null || data.getOperatingCost() == null) {
                log.warn("計算毛利率失敗: 缺少必要欄位");
                return;
            }

            // 2. 營收不可為零
            if (data.getRevenue().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("營收為零或負數，毛利率不適用: stockId={}, revenue={}",
                        data.getStockId(), data.getRevenue());
                return;
            }

            // 3. 計算毛利
            BigDecimal grossProfit = data.getRevenue().subtract(data.getOperatingCost());

            // 4. 計算毛利率 (%)
            BigDecimal grossMargin = grossProfit
                    .divide(data.getRevenue(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            // 5. 驗證合理性（毛利率通常在 0% ~ 90% 之間）
            if (grossMargin.compareTo(BigDecimal.valueOf(90)) > 0) {
                log.warn("毛利率異常高: stockId={}, grossMargin={}%",
                        data.getStockId(), grossMargin);
                result.getDiagnostics().addWarning(
                        String.format("毛利率異常高: %.2f%%", grossMargin));
            }

            if (grossMargin.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("毛利率為負: stockId={}, grossMargin={}%",
                        data.getStockId(), grossMargin);
                result.getDiagnostics().addWarning(
                        String.format("毛利率為負: %.2f%% (營業成本高於營收)", grossMargin));
            }

            // 6. 儲存結果
            result.addProfitabilityIndicator(INDICATOR_NAME, grossMargin);

            log.debug("毛利率計算成功: stockId={}, grossMargin={}%",
                    data.getStockId(), grossMargin);

        } catch (Exception e) {
            log.error("計算毛利率時發生錯誤: stockId={}", data.getStockId(), e);
            result.getDiagnostics().addError("毛利率計算失敗: " + e.getMessage());
        }
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name(INDICATOR_NAME)
                .displayName(DISPLAY_NAME)
                .category(CATEGORY)
                .description("毛利率 = (營收 - 營業成本) / 營收 × 100%")
                .unit("%")
                .priority("P0")
                .requiresHistory(false)
                .build();
    }
}
