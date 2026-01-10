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
 * 股價營收比 (P/S Ratio) 計算器
 * <p>
 * 功能編號: F-M08-001
 * 計算公式: P/S = 市值 / 營收
 * 說明: 衡量股票市值相對於營收的倍數
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
public class PSCalculator implements FundamentalCalculator {

    private static final String INDICATOR_NAME = "ps_ratio";
    private static final String DISPLAY_NAME = "股價營收比";
    private static final String CATEGORY = "VALUATION";

    @Override
    public void calculate(FinancialData data, CalculationResult result) {
        try {
            // 1. 驗證必要欄位
            if (data.getMarketCap() == null || data.getRevenue() == null) {
                log.warn("計算 P/S 失敗: 缺少必要欄位");
                return;
            }

            // 2. 營收不可為零
            if (data.getRevenue().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("營收為零或負數，P/S 不適用: stockId={}, revenue={}",
                        data.getStockId(), data.getRevenue());
                return;
            }

            // 3. 計算 P/S
            BigDecimal psRatio = data.getMarketCap()
                    .divide(data.getRevenue(), 2, RoundingMode.HALF_UP);

            // 4. 驗證合理性（P/S 通常在 0.5-10 之間）
            if (psRatio.compareTo(BigDecimal.valueOf(20)) > 0) {
                log.warn("P/S 異常高: stockId={}, psRatio={}",
                        data.getStockId(), psRatio);
                result.getDiagnostics().addWarning(
                        String.format("P/S 異常高: %.2f", psRatio));
            }

            // 5. 儲存結果
            result.addValuationIndicator(INDICATOR_NAME, psRatio);

            log.debug("P/S 計算成功: stockId={}, psRatio={}",
                    data.getStockId(), psRatio);

        } catch (Exception e) {
            log.error("計算 P/S 時發生錯誤: stockId={}", data.getStockId(), e);
            result.getDiagnostics().addError("P/S 計算失敗: " + e.getMessage());
        }
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name(INDICATOR_NAME)
                .displayName(DISPLAY_NAME)
                .category(CATEGORY)
                .description("股價營收比 = 市值 / 營收")
                .unit("倍")
                .priority("P0")
                .requiresHistory(false)
                .build();
    }
}
