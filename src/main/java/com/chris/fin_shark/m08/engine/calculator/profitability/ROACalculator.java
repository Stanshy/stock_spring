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
 * 總資產報酬率 (ROA) 計算器
 * <p>
 * 功能編號: F-M08-002
 * 計算公式: ROA = 稅後淨利 / 總資產 × 100%
 * 說明: 衡量公司運用總資產的獲利能力
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
public class ROACalculator implements FundamentalCalculator {

    private static final String INDICATOR_NAME = "roa";
    private static final String DISPLAY_NAME = "總資產報酬率";
    private static final String CATEGORY = "PROFITABILITY";

    @Override
    public void calculate(FinancialData data, CalculationResult result) {
        try {
            // 1. 驗證必要欄位
            if (data.getNetIncome() == null || data.getTotalAssets() == null) {
                log.warn("計算 ROA 失敗: 缺少必要欄位");
                return;
            }

            // 2. 總資產不可為零
            if (data.getTotalAssets().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("總資產為零或負數，ROA 不適用: stockId={}, assets={}",
                        data.getStockId(), data.getTotalAssets());
                return;
            }

            // 3. 計算 ROA (%)
            BigDecimal roa = data.getNetIncome()
                    .divide(data.getTotalAssets(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            // 4. 驗證合理性（ROA 通常在 -30% ~ 50% 之間）
            if (roa.compareTo(BigDecimal.valueOf(50)) > 0) {
                log.warn("ROA 異常高: stockId={}, roa={}%",
                        data.getStockId(), roa);
                result.getDiagnostics().addWarning(
                        String.format("ROA 異常高: %.2f%%", roa));
            }

            // 5. 儲存結果
            result.addProfitabilityIndicator(INDICATOR_NAME, roa);

            log.debug("ROA 計算成功: stockId={}, roa={}%",
                    data.getStockId(), roa);

        } catch (Exception e) {
            log.error("計算 ROA 時發生錯誤: stockId={}", data.getStockId(), e);
            result.getDiagnostics().addError("ROA 計算失敗: " + e.getMessage());
        }
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name(INDICATOR_NAME)
                .displayName(DISPLAY_NAME)
                .category(CATEGORY)
                .description("總資產報酬率 = 稅後淨利 / 總資產 × 100%")
                .unit("%")
                .priority("P0")
                .requiresHistory(false)
                .build();
    }
}
