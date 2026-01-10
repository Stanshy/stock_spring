package com.chris.fin_shark.m08.engine.calculator.cashflow;

import com.chris.fin_shark.m08.engine.FundamentalCalculator;
import com.chris.fin_shark.m08.engine.model.CalculationResult;
import com.chris.fin_shark.m08.engine.model.FinancialData;
import com.chris.fin_shark.m08.engine.model.IndicatorMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 自由現金流殖利率 (FCF Yield) 計算器
 * <p>
 * 功能編號: F-M08-006
 * 計算公式: FCF Yield = FCF / 市值 × 100%
 * 說明: 衡量公司現金流相對於市值的報酬率
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
public class FCFYieldCalculator implements FundamentalCalculator {

    private static final String INDICATOR_NAME = "fcf_yield";
    private static final String DISPLAY_NAME = "自由現金流殖利率";
    private static final String CATEGORY = "CASH_FLOW";

    @Override
    public void calculate(FinancialData data, CalculationResult result) {
        try {
            // 1. 驗證必要欄位
            if (data.getMarketCap() == null) {
                log.warn("計算 FCF Yield 失敗: 缺少市值資料");
                return;
            }

            // 2. 計算 FCF（若尚未計算）
            BigDecimal fcf = data.calculateFreeCashFlow();
            if (fcf == null) {
                log.warn("無法計算 FCF Yield: FCF 計算失敗");
                return;
            }

            // 3. 市值不可為零
            if (data.getMarketCap().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("市值為零或負數，FCF Yield 不適用: stockId={}",
                        data.getStockId());
                return;
            }

            // 4. 計算 FCF Yield (%)
            BigDecimal fcfYield = fcf
                    .divide(data.getMarketCap(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            // 5. 驗證與分析
            if (fcfYield.compareTo(BigDecimal.valueOf(10)) > 0) {
                log.info("FCF Yield 優秀: stockId={}, fcfYield={}%",
                        data.getStockId(), fcfYield);
            }

            if (fcfYield.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("FCF Yield 為負: stockId={}, fcfYield={}%",
                        data.getStockId(), fcfYield);
                result.getDiagnostics().addWarning(
                        String.format("FCF Yield 為負: %.2f%%", fcfYield));
            }

            // 6. 儲存結果
            result.addCashFlowIndicator(INDICATOR_NAME, fcfYield);

            log.debug("FCF Yield 計算成功: stockId={}, fcfYield={}%",
                    data.getStockId(), fcfYield);

        } catch (Exception e) {
            log.error("計算 FCF Yield 時發生錯誤: stockId={}", data.getStockId(), e);
            result.getDiagnostics().addError("FCF Yield 計算失敗: " + e.getMessage());
        }
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name(INDICATOR_NAME)
                .displayName(DISPLAY_NAME)
                .category(CATEGORY)
                .description("自由現金流殖利率 = FCF / 市值 × 100%")
                .unit("%")
                .priority("P0")
                .requiresHistory(false)
                .build();
    }
}
