package com.chris.fin_shark.m08.engine.calculator.solvency;

import com.chris.fin_shark.m08.engine.FundamentalCalculator;
import com.chris.fin_shark.m08.engine.model.CalculationResult;
import com.chris.fin_shark.m08.engine.model.FinancialData;
import com.chris.fin_shark.m08.engine.model.IndicatorMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 流動比率計算器
 * <p>
 * 功能編號: F-M08-004
 * 計算公式: 流動比率 = 流動資產 / 流動負債
 * 說明: 衡量公司短期償債能力
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
public class CurrentRatioCalculator implements FundamentalCalculator {

    private static final String INDICATOR_NAME = "current_ratio";
    private static final String DISPLAY_NAME = "流動比率";
    private static final String CATEGORY = "SOLVENCY";

    @Override
    public void calculate(FinancialData data, CalculationResult result) {
        try {
            // 1. 驗證必要欄位
            if (data.getCurrentAssets() == null || data.getCurrentLiabilities() == null) {
                log.warn("計算流動比率失敗: 缺少必要欄位");
                return;
            }

            // 2. 流動負債不可為零
            if (data.getCurrentLiabilities().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("流動負債為零或負數，流動比率不適用: stockId={}, currentLiabilities={}",
                        data.getStockId(), data.getCurrentLiabilities());
                return;
            }

            // 3. 計算流動比率
            BigDecimal currentRatio = data.getCurrentAssets()
                    .divide(data.getCurrentLiabilities(), 2, RoundingMode.HALF_UP);

            // 4. 驗證合理性與風險警示
            if (currentRatio.compareTo(BigDecimal.valueOf(1.0)) < 0) {
                log.warn("流動比率低於1，短期償債能力不足: stockId={}, currentRatio={}",
                        data.getStockId(), currentRatio);
                result.getDiagnostics().addWarning(
                        String.format("流動比率低於1: %.2f (短期償債能力不足)", currentRatio));
            }

            if (currentRatio.compareTo(BigDecimal.valueOf(0.8)) < 0) {
                log.error("流動比率過低，流動性風險高: stockId={}, currentRatio={}",
                        data.getStockId(), currentRatio);
                result.getDiagnostics().addWarning(
                        String.format("流動比率過低: %.2f (流動性風險高)", currentRatio));
            }

            if (currentRatio.compareTo(BigDecimal.valueOf(5.0)) > 0) {
                log.warn("流動比率過高，資產運用效率可能不佳: stockId={}, currentRatio={}",
                        data.getStockId(), currentRatio);
                result.getDiagnostics().addWarning(
                        String.format("流動比率過高: %.2f (資產運用效率可能不佳)", currentRatio));
            }

            // 5. 儲存結果
            result.addSolvencyIndicator(INDICATOR_NAME, currentRatio);

            log.debug("流動比率計算成功: stockId={}, currentRatio={}",
                    data.getStockId(), currentRatio);

        } catch (Exception e) {
            log.error("計算流動比率時發生錯誤: stockId={}", data.getStockId(), e);
            result.getDiagnostics().addError("流動比率計算失敗: " + e.getMessage());
        }
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name(INDICATOR_NAME)
                .displayName(DISPLAY_NAME)
                .category(CATEGORY)
                .description("流動比率 = 流動資產 / 流動負債")
                .unit("倍")
                .priority("P0")
                .requiresHistory(false)
                .build();
    }
}
