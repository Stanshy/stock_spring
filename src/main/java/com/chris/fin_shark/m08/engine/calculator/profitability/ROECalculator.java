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
 * 股東權益報酬率 (ROE) 計算器
 * <p>
 * 功能編號: F-M08-002
 * 計算公式: ROE = 稅後淨利 / 股東權益 × 100%
 * 說明: 衡量公司運用股東資金的獲利能力
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
public class ROECalculator implements FundamentalCalculator {

    private static final String INDICATOR_NAME = "roe";
    private static final String DISPLAY_NAME = "股東權益報酬率";
    private static final String CATEGORY = "PROFITABILITY";

    @Override
    public void calculate(FinancialData data, CalculationResult result) {
        try {
            // 1. 驗證必要欄位
            if (data.getNetIncome() == null || data.getTotalEquity() == null) {
                log.warn("計算 ROE 失敗: 缺少必要欄位");
                return;
            }

            // 2. 股東權益不可為零或負數
            if (data.getTotalEquity().compareTo(BigDecimal.ZERO) <= 0) {
                log.debug("股東權益為負，ROE 不適用: stockId={}, equity={}",
                        data.getStockId(), data.getTotalEquity());
                return;
            }

            // 3. 計算 ROE (%)
            BigDecimal roe = data.getNetIncome()
                    .divide(data.getTotalEquity(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            // 4. 驗證合理性（ROE 通常在 -50% ~ 100% 之間）
            if (roe.compareTo(BigDecimal.valueOf(100)) > 0) {
                log.warn("ROE 異常高: stockId={}, roe={}%",
                        data.getStockId(), roe);
                result.getDiagnostics().addWarning(
                        String.format("ROE 異常高: %.2f%% (淨利=%s, 權益=%s)",
                                roe, data.getNetIncome(), data.getTotalEquity()));
            }

            if (roe.compareTo(BigDecimal.valueOf(-50)) < 0) {
                log.warn("ROE 異常低: stockId={}, roe={}%",
                        data.getStockId(), roe);
                result.getDiagnostics().addWarning(
                        String.format("ROE 異常低: %.2f%%", roe));
            }

            // 5. 儲存結果
            result.addProfitabilityIndicator(INDICATOR_NAME, roe);

            log.debug("ROE 計算成功: stockId={}, roe={}%",
                    data.getStockId(), roe);

        } catch (Exception e) {
            log.error("計算 ROE 時發生錯誤: stockId={}", data.getStockId(), e);
            result.getDiagnostics().addError("ROE 計算失敗: " + e.getMessage());
        }
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name(INDICATOR_NAME)
                .displayName(DISPLAY_NAME)
                .category(CATEGORY)
                .description("股東權益報酬率 = 稅後淨利 / 股東權益 × 100%")
                .unit("%")
                .priority("P0")
                .requiresHistory(false)
                .build();
    }
}
