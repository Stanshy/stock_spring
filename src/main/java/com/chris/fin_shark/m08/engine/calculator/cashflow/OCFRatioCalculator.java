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
 * 營運現金流比率 (Operating Cash Flow Ratio) 計算器
 * <p>
 * 功能編號: F-M08-006
 * 計算公式: 營運現金流比率 = 營運現金流 / 流動負債
 * 說明: 衡量公司以營運現金流償還短期負債的能力
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
public class OCFRatioCalculator implements FundamentalCalculator {

    private static final String INDICATOR_NAME = "ocf_ratio";
    private static final String DISPLAY_NAME = "營運現金流比率";
    private static final String CATEGORY = "CASH_FLOW";

    @Override
    public void calculate(FinancialData data, CalculationResult result) {
        try {
            // 1. 驗證必要欄位
            if (data.getOperatingCashFlow() == null || data.getCurrentLiabilities() == null) {
                log.warn("計算營運現金流比率失敗: 缺少必要欄位 (ocf={}, currentLiabilities={})",
                        data.getOperatingCashFlow(), data.getCurrentLiabilities());
                return;
            }

            // 2. 流動負債不可為零
            if (data.getCurrentLiabilities().compareTo(BigDecimal.ZERO) == 0) {
                log.debug("流動負債為零，營運現金流比率不適用: stockId={}",
                        data.getStockId());
                return;
            }

            // 3. 流動負債為負（異常情況）
            if (data.getCurrentLiabilities().compareTo(BigDecimal.ZERO) < 0) {
                log.warn("流動負債為負（異常）: stockId={}, currentLiabilities={}",
                        data.getStockId(), data.getCurrentLiabilities());
                return;
            }

            // 4. 計算營運現金流比率
            BigDecimal ocfRatio = data.getOperatingCashFlow()
                    .divide(data.getCurrentLiabilities(), 2, RoundingMode.HALF_UP);

            // 5. 驗證與記錄
            if (ocfRatio.compareTo(BigDecimal.valueOf(1.0)) < 0) {
                log.info("營運現金流比率小於 1: stockId={}, ocfRatio={}",
                        data.getStockId(), ocfRatio);
            }

            if (ocfRatio.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("營運現金流為負（燒錢）: stockId={}, ocfRatio={}",
                        data.getStockId(), ocfRatio);
                result.getDiagnostics().addWarning(
                        String.format("營運現金流為負: %.2f (現金流出)", ocfRatio));
            }

            // 6. 儲存結果
            result.addCashFlowIndicator(INDICATOR_NAME, ocfRatio);

            log.debug("營運現金流比率計算成功: stockId={}, ocf={}, currentLiabilities={}, ocfRatio={}",
                    data.getStockId(), data.getOperatingCashFlow(),
                    data.getCurrentLiabilities(), ocfRatio);

        } catch (Exception e) {
            log.error("計算營運現金流比率時發生錯誤: stockId={}", data.getStockId(), e);
            result.getDiagnostics().addError("營運現金流比率計算失敗: " + e.getMessage());
        }
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name(INDICATOR_NAME)
                .displayName(DISPLAY_NAME)
                .category(CATEGORY)
                .description("營運現金流比率 = 營運現金流 / 流動負債")
                .unit("倍")
                .priority("P0")
                .requiresHistory(false)
                .build();
    }
}
