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
 * 負債權益比 (Debt to Equity Ratio) 計算器
 * <p>
 * 功能編號: F-M08-003
 * 計算公式: 負債權益比 = 總負債 / 股東權益
 * 說明: 衡量公司財務槓桿程度，比率越高表示財務風險越大
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
public class DebtToEquityCalculator implements FundamentalCalculator {

    private static final String INDICATOR_NAME = "debt_to_equity";
    private static final String DISPLAY_NAME = "負債權益比";
    private static final String CATEGORY = "FINANCIAL_STRUCTURE";

    @Override
    public void calculate(FinancialData data, CalculationResult result) {
        try {
            // 1. 驗證必要欄位
            if (data.getTotalLiabilities() == null || data.getTotalEquity() == null) {
                log.warn("計算負債權益比失敗: 缺少必要欄位 (totalLiabilities={}, totalEquity={})",
                        data.getTotalLiabilities(), data.getTotalEquity());
                return;
            }

            // 2. 股東權益不可為零或負數
            if (data.getTotalEquity().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("股東權益為負或零，負債權益比不適用: stockId={}, equity={}",
                        data.getStockId(), data.getTotalEquity());
                result.getDiagnostics().addWarning(
                        String.format("股東權益為負或零: %s (淨值為負)", data.getTotalEquity()));
                return;
            }

            // 3. 計算負債權益比
            BigDecimal debtToEquity = data.getTotalLiabilities()
                    .divide(data.getTotalEquity(), 2, RoundingMode.HALF_UP);

            // 4. 驗證合理性
            if (debtToEquity.compareTo(BigDecimal.valueOf(2.0)) > 0) {
                log.warn("負債權益比過高（財務槓桿高）: stockId={}, debtToEquity={}",
                        data.getStockId(), debtToEquity);
                result.getDiagnostics().addWarning(
                        String.format("負債權益比過高: %.2f (財務槓桿風險)", debtToEquity));
            }

            if (debtToEquity.compareTo(BigDecimal.valueOf(3.0)) > 0) {
                log.warn("負債權益比異常高: stockId={}, debtToEquity={}",
                        data.getStockId(), debtToEquity);
            }

            // 5. 儲存結果
            result.addFinancialStructureIndicator(INDICATOR_NAME, debtToEquity);

            log.debug("負債權益比計算成功: stockId={}, totalLiabilities={}, totalEquity={}, debtToEquity={}",
                    data.getStockId(), data.getTotalLiabilities(),
                    data.getTotalEquity(), debtToEquity);

        } catch (Exception e) {
            log.error("計算負債權益比時發生錯誤: stockId={}", data.getStockId(), e);
            result.getDiagnostics().addError("負債權益比計算失敗: " + e.getMessage());
        }
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name(INDICATOR_NAME)
                .displayName(DISPLAY_NAME)
                .category(CATEGORY)
                .description("負債權益比 = 總負債 / 股東權益")
                .unit("倍")
                .priority("P0")
                .requiresHistory(false)
                .build();
    }
}
