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
 * 負債比率計算器
 * <p>
 * 功能編號: F-M08-003
 * 計算公式: 負債比率 = 總負債 / 總資產 × 100%
 * 說明: 衡量公司資產中由負債支應的比例
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
public class DebtRatioCalculator implements FundamentalCalculator {

    private static final String INDICATOR_NAME = "debt_ratio";
    private static final String DISPLAY_NAME = "負債比率";
    private static final String CATEGORY = "FINANCIAL_STRUCTURE";

    @Override
    public void calculate(FinancialData data, CalculationResult result) {
        try {
            // 1. 驗證必要欄位
            if (data.getTotalLiabilities() == null || data.getTotalAssets() == null) {
                log.warn("計算負債比率失敗: 缺少必要欄位");
                return;
            }

            // 2. 總資產不可為零
            if (data.getTotalAssets().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("總資產為零或負數，負債比率不適用: stockId={}, assets={}",
                        data.getStockId(), data.getTotalAssets());
                return;
            }

            // 3. 計算負債比率 (%)
            BigDecimal debtRatio = data.getTotalLiabilities()
                    .divide(data.getTotalAssets(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            // 4. 驗證合理性與風險警示
            if (debtRatio.compareTo(BigDecimal.valueOf(70)) > 0) {
                log.warn("負債比率偏高: stockId={}, debtRatio={}%",
                        data.getStockId(), debtRatio);
                result.getDiagnostics().addWarning(
                        String.format("負債比率偏高: %.2f%% (高於70%%)", debtRatio));
            }

            if (debtRatio.compareTo(BigDecimal.valueOf(90)) > 0) {
                log.error("負債比率過高，財務風險極高: stockId={}, debtRatio={}%",
                        data.getStockId(), debtRatio);
                result.getDiagnostics().addWarning(
                        String.format("負債比率過高: %.2f%% (高於90%%, 財務風險極高)", debtRatio));
            }

            // 5. 儲存結果
            result.addFinancialStructureIndicator(INDICATOR_NAME, debtRatio);

            log.debug("負債比率計算成功: stockId={}, debtRatio={}%",
                    data.getStockId(), debtRatio);

        } catch (Exception e) {
            log.error("計算負債比率時發生錯誤: stockId={}", data.getStockId(), e);
            result.getDiagnostics().addError("負債比率計算失敗: " + e.getMessage());
        }
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name(INDICATOR_NAME)
                .displayName(DISPLAY_NAME)
                .category(CATEGORY)
                .description("負債比率 = 總負債 / 總資產 × 100%")
                .unit("%")
                .priority("P0")
                .requiresHistory(false)
                .build();
    }
}
