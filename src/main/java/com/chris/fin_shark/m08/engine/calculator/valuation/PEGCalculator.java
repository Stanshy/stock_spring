package com.chris.fin_shark.m08.engine.calculator.valuation;

import com.chris.fin_shark.m08.engine.FundamentalCalculator;
import com.chris.fin_shark.m08.engine.model.CalculationResult;
import com.chris.fin_shark.m08.engine.model.FinancialData;
import com.chris.fin_shark.m08.engine.model.IndicatorMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * PEG Ratio 計算器
 * <p>
 * 功能編號: F-M08-001
 * 計算公式: PEG = P/E / EPS成長率
 * 說明: 本益比相對於盈餘成長率，用於評估成長股
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
public class PEGCalculator implements FundamentalCalculator {

    private static final String INDICATOR_NAME = "peg_ratio";
    private static final String DISPLAY_NAME = "PEG比率";
    private static final String CATEGORY = "VALUATION";

    @Override
    public void calculate(FinancialData data, CalculationResult result) {
        try {
            // 1. 驗證必要欄位
            if (data.getStockPrice() == null || data.getEps() == null
                    || data.getLastYearEps() == null) {
                log.debug("計算 PEG 失敗: 缺少必要欄位（需要本季EPS和去年同季EPS）");
                return;
            }

            // 2. EPS 必須為正
            if (data.getEps().compareTo(BigDecimal.ZERO) <= 0
                    || data.getLastYearEps().compareTo(BigDecimal.ZERO) <= 0) {
                log.debug("EPS 為負或零，PEG 不適用: stockId={}", data.getStockId());
                return;
            }

            // 3. 計算 EPS 成長率 (%)
            BigDecimal epsGrowth = data.getEps()
                    .subtract(data.getLastYearEps())
                    .divide(data.getLastYearEps(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));

            // 4. EPS 成長率不可為零或負數
            if (epsGrowth.compareTo(BigDecimal.ZERO) <= 0) {
                log.debug("EPS 成長率為負，PEG 不適用: stockId={}, growth={}",
                        data.getStockId(), epsGrowth);
                return;
            }

            // 5. 計算 P/E
            BigDecimal peRatio = data.getStockPrice()
                    .divide(data.getEps(), 4, RoundingMode.HALF_UP);

            // 6. 計算 PEG
            BigDecimal pegRatio = peRatio
                    .divide(epsGrowth, 2, RoundingMode.HALF_UP);

            // 7. 驗證合理性（PEG < 1 視為低估，> 2 視為高估）
            if (pegRatio.compareTo(BigDecimal.valueOf(3)) > 0) {
                log.warn("PEG 異常高: stockId={}, pegRatio={}",
                        data.getStockId(), pegRatio);
                result.getDiagnostics().addWarning(
                        String.format("PEG 異常高: %.2f (P/E=%.2f, 成長率=%.2f%%)",
                                pegRatio, peRatio, epsGrowth));
            }

            // 8. 儲存結果
            result.addValuationIndicator(INDICATOR_NAME, pegRatio);

            log.debug("PEG 計算成功: stockId={}, pegRatio={}, peRatio={}, growth={}%",
                    data.getStockId(), pegRatio, peRatio, epsGrowth);

        } catch (Exception e) {
            log.error("計算 PEG 時發生錯誤: stockId={}", data.getStockId(), e);
            result.getDiagnostics().addError("PEG 計算失敗: " + e.getMessage());
        }
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name(INDICATOR_NAME)
                .displayName(DISPLAY_NAME)
                .category(CATEGORY)
                .description("PEG比率 = (股價/EPS) / EPS成長率，用於評估成長股價值")
                .unit("倍")
                .priority("P0")
                .requiresHistory(true)
                .build();
    }
}
