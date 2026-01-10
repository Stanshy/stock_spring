package com.chris.fin_shark.m08.engine.calculator.cashflow;

import com.chris.fin_shark.m08.engine.FundamentalCalculator;
import com.chris.fin_shark.m08.engine.model.CalculationResult;
import com.chris.fin_shark.m08.engine.model.FinancialData;
import com.chris.fin_shark.m08.engine.model.IndicatorMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 自由現金流 (FCF) 計算器
 * <p>
 * 功能編號: F-M08-006
 * 計算公式: FCF = 營運現金流 - 資本支出
 * 說明: 衡量公司可自由運用的現金流量
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
public class FCFCalculator implements FundamentalCalculator {

    private static final String INDICATOR_NAME = "free_cash_flow";
    private static final String DISPLAY_NAME = "自由現金流";
    private static final String CATEGORY = "CASH_FLOW";

    @Override
    public void calculate(FinancialData data, CalculationResult result) {
        try {
            // 1. 驗證必要欄位
            if (data.getOperatingCashFlow() == null || data.getCapitalExpenditure() == null) {
                log.warn("計算 FCF 失敗: 缺少必要欄位");
                return;
            }

            // 2. 計算自由現金流
            BigDecimal fcf = data.getOperatingCashFlow()
                    .subtract(data.getCapitalExpenditure());

            // 3. 驗證與警示
            if (fcf.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("自由現金流為負: stockId={}, fcf={}",
                        data.getStockId(), fcf);
                result.getDiagnostics().addWarning(
                        String.format("自由現金流為負: %s (燒錢中)", fcf));
            }

            // 4. 儲存結果（單位：千元）
            result.addCashFlowIndicator(INDICATOR_NAME, fcf);

            log.debug("FCF 計算成功: stockId={}, fcf={}",
                    data.getStockId(), fcf);

        } catch (Exception e) {
            log.error("計算 FCF 時發生錯誤: stockId={}", data.getStockId(), e);
            result.getDiagnostics().addError("FCF 計算失敗: " + e.getMessage());
        }
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name(INDICATOR_NAME)
                .displayName(DISPLAY_NAME)
                .category(CATEGORY)
                .description("自由現金流 = 營運現金流 - 資本支出")
                .unit("千元")
                .priority("P0")
                .requiresHistory(false)
                .build();
    }
}
