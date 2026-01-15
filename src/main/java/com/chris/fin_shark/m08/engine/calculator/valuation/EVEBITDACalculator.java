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
 * 企業價值倍數 (EV/EBITDA) 計算器
 * <p>
 * 功能編號: F-M08-001
 * 計算公式: EV/EBITDA = 企業價值 / EBITDA
 *          企業價值 (EV) = 市值 + 總負債 - 現金及約當現金
 * 說明: 衡量企業整體價值相對於現金獲利能力的倍數
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
public class EVEBITDACalculator implements FundamentalCalculator {

    private static final String INDICATOR_NAME = "ev_ebitda";
    private static final String DISPLAY_NAME = "企業價值倍數";
    private static final String CATEGORY = "VALUATION";

    @Override
    public void calculate(FinancialData data, CalculationResult result) {
        try {
            // 1. 驗證必要欄位
            if (data.getMarketCap() == null || data.getEbitda() == null) {
                log.warn("計算 EV/EBITDA 失敗: 缺少必要欄位 (marketCap={}, ebitda={})",
                        data.getMarketCap(), data.getEbitda());
                return;
            }

            // 2. EBITDA 不可為零或負數
            if (data.getEbitda().compareTo(BigDecimal.ZERO) <= 0) {
                log.debug("EBITDA 為負，EV/EBITDA 不適用: stockId={}, ebitda={}",
                        data.getStockId(), data.getEbitda());
                return;
            }

            // 3. 計算企業價值 (EV) = 市值 + 總負債 - 現金
            BigDecimal totalLiabilities = data.getTotalLiabilities() != null
                    ? data.getTotalLiabilities() : BigDecimal.ZERO;
            BigDecimal cash = data.getCashAndEquivalents() != null
                    ? data.getCashAndEquivalents() : BigDecimal.ZERO;

            BigDecimal ev = data.getMarketCap()
                    .add(totalLiabilities)
                    .subtract(cash);

            // 4. 計算 EV/EBITDA
            BigDecimal evEbitda = ev.divide(data.getEbitda(), 2, RoundingMode.HALF_UP);

            // 5. 驗證合理性（EV/EBITDA 通常在 3-20 之間）
            if (evEbitda.compareTo(BigDecimal.valueOf(30)) > 0) {
                log.warn("EV/EBITDA 異常高: stockId={}, evEbitda={}",
                        data.getStockId(), evEbitda);
                result.getDiagnostics().addWarning(
                        String.format("EV/EBITDA 異常高: %.2f", evEbitda));
            }

            if (evEbitda.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("EV/EBITDA 為負: stockId={}, evEbitda={}",
                        data.getStockId(), evEbitda);
                result.getDiagnostics().addWarning(
                        String.format("EV/EBITDA 為負: %.2f (EV=%s)", evEbitda, ev));
            }

            // 6. 儲存結果
            result.addValuationIndicator(INDICATOR_NAME, evEbitda);
            // 同時儲存 EV 供其他計算使用
            result.addValuationIndicator("ev", ev);

            log.debug("EV/EBITDA 計算成功: stockId={}, ev={}, ebitda={}, evEbitda={}",
                    data.getStockId(), ev, data.getEbitda(), evEbitda);

        } catch (Exception e) {
            log.error("計算 EV/EBITDA 時發生錯誤: stockId={}", data.getStockId(), e);
            result.getDiagnostics().addError("EV/EBITDA 計算失敗: " + e.getMessage());
        }
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name(INDICATOR_NAME)
                .displayName(DISPLAY_NAME)
                .category(CATEGORY)
                .description("企業價值倍數 = (市值 + 負債 - 現金) / EBITDA")
                .unit("倍")
                .priority("P0")
                .requiresHistory(false)
                .build();
    }
}
