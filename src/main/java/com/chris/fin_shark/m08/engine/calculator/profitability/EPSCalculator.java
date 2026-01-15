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
 * 每股盈餘 (EPS) 計算器
 * <p>
 * 功能編號: F-M08-002
 * 計算公式: EPS = 稅後淨利 / 流通股數
 * 說明: 衡量每股可分配的盈餘
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
public class EPSCalculator implements FundamentalCalculator {

    private static final String INDICATOR_NAME = "eps";
    private static final String DISPLAY_NAME = "每股盈餘";
    private static final String CATEGORY = "PROFITABILITY";

    @Override
    public void calculate(FinancialData data, CalculationResult result) {
        try {
            BigDecimal epsValue;

            // 優先使用財報中已有的 EPS
            if (data.getEps() != null) {
                epsValue = data.getEps();
                log.debug("使用財報提供的 EPS: stockId={}, eps={}",
                        data.getStockId(), epsValue);
            } else {
                // 自行計算 EPS
                if (data.getNetIncome() == null || data.getOutstandingShares() == null) {
                    log.warn("計算 EPS 失敗: 缺少必要欄位 (netIncome={}, outstandingShares={})",
                            data.getNetIncome(), data.getOutstandingShares());
                    return;
                }

                if (data.getOutstandingShares() <= 0) {
                    log.warn("流通股數無效: stockId={}, outstandingShares={}",
                            data.getStockId(), data.getOutstandingShares());
                    return;
                }

                // 注意：netIncome 單位是千元，需轉換
                // EPS = 淨利(千元) × 1000 / 流通股數(股)
                BigDecimal netIncomeInYuan = data.getNetIncome()
                        .multiply(BigDecimal.valueOf(1000));

                epsValue = netIncomeInYuan
                        .divide(BigDecimal.valueOf(data.getOutstandingShares()),
                                2, RoundingMode.HALF_UP);

                log.debug("自行計算 EPS: stockId={}, netIncome={}, shares={}, eps={}",
                        data.getStockId(), data.getNetIncome(),
                        data.getOutstandingShares(), epsValue);
            }

            // 儲存結果
            result.addProfitabilityIndicator(INDICATOR_NAME, epsValue);

            // 記錄虧損情況
            if (epsValue.compareTo(BigDecimal.ZERO) < 0) {
                log.info("公司虧損: stockId={}, eps={}",
                        data.getStockId(), epsValue);
            }

        } catch (Exception e) {
            log.error("計算 EPS 時發生錯誤: stockId={}", data.getStockId(), e);
            result.getDiagnostics().addError("EPS 計算失敗: " + e.getMessage());
        }
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name(INDICATOR_NAME)
                .displayName(DISPLAY_NAME)
                .category(CATEGORY)
                .description("每股盈餘 = 稅後淨利 / 流通股數")
                .unit("元")
                .priority("P0")
                .requiresHistory(false)
                .build();
    }
}
