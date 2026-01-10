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
 * 股價淨值比 (P/B Ratio) 計算器
 * <p>
 * 功能編號: F-M08-001
 * 計算公式: P/B = 股價 / 每股淨值
 * 說明: 衡量股票價格相對於每股淨值的倍數
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
public class PBCalculator implements FundamentalCalculator {

    private static final String INDICATOR_NAME = "pb_ratio";
    private static final String DISPLAY_NAME = "股價淨值比";
    private static final String CATEGORY = "VALUATION";

    @Override
    public void calculate(FinancialData data, CalculationResult result) {
        try {
            // 1. 驗證必要欄位
            if (data.getStockPrice() == null || data.getBookValuePerShare() == null) {
                log.warn("計算 P/B 失敗: 缺少必要欄位");
                return;
            }

            // 2. 每股淨值不可為零或負數（淨值為負的公司 P/B 無意義）
            if (data.getBookValuePerShare().compareTo(BigDecimal.ZERO) <= 0) {
                log.debug("淨值為負，P/B 不適用: stockId={}, bookValue={}",
                        data.getStockId(), data.getBookValuePerShare());
                return;
            }

            // 3. 計算 P/B
            BigDecimal pbRatio = data.getStockPrice()
                    .divide(data.getBookValuePerShare(), 2, RoundingMode.HALF_UP);

            // 4. 驗證合理性（P/B 通常在 0.5-5 之間，超過 10 視為異常）
            if (pbRatio.compareTo(BigDecimal.valueOf(10)) > 0) {
                log.warn("P/B 異常高: stockId={}, pbRatio={}",
                        data.getStockId(), pbRatio);
                result.getDiagnostics().addWarning(
                        String.format("P/B 異常高: %.2f", pbRatio));
            }

            // 5. 儲存結果
            result.addValuationIndicator(INDICATOR_NAME, pbRatio);

            log.debug("P/B 計算成功: stockId={}, pbRatio={}",
                    data.getStockId(), pbRatio);

        } catch (Exception e) {
            log.error("計算 P/B 時發生錯誤: stockId={}", data.getStockId(), e);
            result.getDiagnostics().addError("P/B 計算失敗: " + e.getMessage());
        }
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name(INDICATOR_NAME)
                .displayName(DISPLAY_NAME)
                .category(CATEGORY)
                .description("股價淨值比 = 股價 / 每股淨值")
                .unit("倍")
                .priority("P0")
                .requiresHistory(false)
                .build();
    }
}
