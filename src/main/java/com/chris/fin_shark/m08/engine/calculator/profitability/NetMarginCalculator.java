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
 * 淨利率計算器
 * <p>
 * 功能編號: F-M08-002
 * 計算公式: 淨利率 = 稅後淨利 / 營收 × 100%
 * 說明: 衡量公司的最終獲利能力
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
public class NetMarginCalculator implements FundamentalCalculator {

    private static final String INDICATOR_NAME = "net_margin";
    private static final String DISPLAY_NAME = "淨利率";
    private static final String CATEGORY = "PROFITABILITY";

    @Override
    public void calculate(FinancialData data, CalculationResult result) {
        try {
            // 1. 驗證必要欄位
            if (data.getNetIncome() == null || data.getRevenue() == null) {
                log.warn("計算淨利率失敗: 缺少必要欄位");
                return;
            }

            // 2. 營收不可為零
            if (data.getRevenue().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("營收為零或負數，淨利率不適用: stockId={}, revenue={}",
                        data.getStockId(), data.getRevenue());
                return;
            }

            // 3. 計算淨利率 (%)
            BigDecimal netMargin = data.getNetIncome()
                    .divide(data.getRevenue(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            // 4. 驗證合理性（淨利率通常在 -50% ~ 60% 之間）
            if (netMargin.compareTo(BigDecimal.valueOf(60)) > 0) {
                log.warn("淨利率異常高: stockId={}, netMargin={}%",
                        data.getStockId(), netMargin);
                result.getDiagnostics().addWarning(
                        String.format("淨利率異常高: %.2f%%", netMargin));
            }

            if (netMargin.compareTo(BigDecimal.valueOf(-50)) < 0) {
                log.warn("淨利率異常低: stockId={}, netMargin={}%",
                        data.getStockId(), netMargin);
            }

            // 5. 儲存結果
            result.addProfitabilityIndicator(INDICATOR_NAME, netMargin);

            log.debug("淨利率計算成功: stockId={}, netMargin={}%",
                    data.getStockId(), netMargin);

        } catch (Exception e) {
            log.error("計算淨利率時發生錯誤: stockId={}", data.getStockId(), e);
            result.getDiagnostics().addError("淨利率計算失敗: " + e.getMessage());
        }
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name(INDICATOR_NAME)
                .displayName(DISPLAY_NAME)
                .category(CATEGORY)
                .description("淨利率 = 稅後淨利 / 營收 × 100%")
                .unit("%")
                .priority("P0")
                .requiresHistory(false)
                .build();
    }
}
