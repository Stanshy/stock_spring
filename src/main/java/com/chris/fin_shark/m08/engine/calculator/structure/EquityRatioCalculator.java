package com.chris.fin_shark.m08.engine.calculator.structure;

import com.chris.fin_shark.m08.engine.FundamentalCalculator;
import com.chris.fin_shark.m08.engine.model.CalculationResult;
import com.chris.fin_shark.m08.engine.model.FinancialData;
import com.chris.fin_shark.m08.engine.model.IndicatorMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 權益比率計算器
 * <p>
 * 功能編號: F-M08-003
 * 計算公式: 權益比率 = 股東權益 / 總資產 × 100%
 * 說明: 衡量公司資產中由股東權益支應的比例
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
public class EquityRatioCalculator implements FundamentalCalculator {

    private static final String INDICATOR_NAME = "equity_ratio";
    private static final String DISPLAY_NAME = "權益比率";
    private static final String CATEGORY = "FINANCIAL_STRUCTURE";

    @Override
    public void calculate(FinancialData data, CalculationResult result) {
        try {
            // 1. 驗證必要欄位
            if (data.getTotalEquity() == null || data.getTotalAssets() == null) {
                log.warn("計算權益比率失敗: 缺少必要欄位");
                return;
            }

            // 2. 總資產不可為零
            if (data.getTotalAssets().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("總資產為零或負數，權益比率不適用: stockId={}, assets={}",
                        data.getStockId(), data.getTotalAssets());
                return;
            }

            // 3. 計算權益比率 (%)
            BigDecimal equityRatio = data.getTotalEquity()
                    .divide(data.getTotalAssets(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            // 4. 驗證合理性
            if (equityRatio.compareTo(BigDecimal.valueOf(30)) < 0) {
                log.warn("權益比率偏低: stockId={}, equityRatio={}%",
                        data.getStockId(), equityRatio);
                result.getDiagnostics().addWarning(
                        String.format("權益比率偏低: %.2f%% (低於30%%, 自有資金不足)", equityRatio));
            }

            if (equityRatio.compareTo(BigDecimal.ZERO) < 0) {
                log.error("權益比率為負，淨值為負: stockId={}, equityRatio={}%",
                        data.getStockId(), equityRatio);
                result.getDiagnostics().addWarning(
                        String.format("權益比率為負: %.2f%% (淨值為負，資不抵債)", equityRatio));
            }

            // 5. 儲存結果
            result.addFinancialStructureIndicator(INDICATOR_NAME, equityRatio);

            log.debug("權益比率計算成功: stockId={}, equityRatio={}%",
                    data.getStockId(), equityRatio);

        } catch (Exception e) {
            log.error("計算權益比率時發生錯誤: stockId={}", data.getStockId(), e);
            result.getDiagnostics().addError("權益比率計算失敗: " + e.getMessage());
        }
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name(INDICATOR_NAME)
                .displayName(DISPLAY_NAME)
                .category(CATEGORY)
                .description("權益比率 = 股東權益 / 總資產 × 100%")
                .unit("%")
                .priority("P0")
                .requiresHistory(false)
                .build();
    }
}
