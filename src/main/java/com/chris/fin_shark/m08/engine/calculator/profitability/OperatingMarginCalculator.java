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
 * 營業利益率 (Operating Margin) 計算器
 * <p>
 * 功能編號: F-M08-002
 * 計算公式: 營業利益率 = 營業利益 / 營收 × 100%
 * 說明: 衡量公司本業經營的獲利能力
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
public class OperatingMarginCalculator implements FundamentalCalculator {

    private static final String INDICATOR_NAME = "operating_margin";
    private static final String DISPLAY_NAME = "營業利益率";
    private static final String CATEGORY = "PROFITABILITY";

    @Override
    public void calculate(FinancialData data, CalculationResult result) {
        try {
            // 1. 驗證必要欄位
            if (data.getOperatingIncome() == null || data.getRevenue() == null) {
                log.warn("計算營業利益率失敗: 缺少必要欄位 (operatingIncome={}, revenue={})",
                        data.getOperatingIncome(), data.getRevenue());
                return;
            }

            // 2. 營收不可為零
            if (data.getRevenue().compareTo(BigDecimal.ZERO) == 0) {
                log.debug("營收為零，營業利益率不適用: stockId={}",
                        data.getStockId());
                return;
            }

            // 3. 計算營業利益率 (%)
            BigDecimal operatingMargin = data.getOperatingIncome()
                    .divide(data.getRevenue(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            // 4. 驗證合理性
            if (operatingMargin.compareTo(BigDecimal.valueOf(50)) > 0) {
                log.info("營業利益率極高: stockId={}, operatingMargin={}%",
                        data.getStockId(), operatingMargin);
            }

            if (operatingMargin.compareTo(BigDecimal.valueOf(-30)) < 0) {
                log.warn("營業利益率異常低: stockId={}, operatingMargin={}%",
                        data.getStockId(), operatingMargin);
                result.getDiagnostics().addWarning(
                        String.format("營業利益率異常低: %.2f%%", operatingMargin));
            }

            // 5. 儲存結果
            result.addProfitabilityIndicator(INDICATOR_NAME, operatingMargin);

            log.debug("營業利益率計算成功: stockId={}, operatingMargin={}%",
                    data.getStockId(), operatingMargin);

        } catch (Exception e) {
            log.error("計算營業利益率時發生錯誤: stockId={}", data.getStockId(), e);
            result.getDiagnostics().addError("營業利益率計算失敗: " + e.getMessage());
        }
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name(INDICATOR_NAME)
                .displayName(DISPLAY_NAME)
                .category(CATEGORY)
                .description("營業利益率 = 營業利益 / 營收 × 100%")
                .unit("%")
                .priority("P0")
                .requiresHistory(false)
                .build();
    }
}
