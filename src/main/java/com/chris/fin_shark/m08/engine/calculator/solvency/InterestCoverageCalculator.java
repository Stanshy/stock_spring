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
 * 利息保障倍數 (Interest Coverage Ratio) 計算器
 * <p>
 * 功能編號: F-M08-004
 * 計算公式: 利息保障倍數 = EBIT / 利息費用
 *          若無 EBIT，使用 營業利益 作為近似值
 * 說明: 衡量公司以營業利潤支付利息費用的能力
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
public class InterestCoverageCalculator implements FundamentalCalculator {

    private static final String INDICATOR_NAME = "interest_coverage";
    private static final String DISPLAY_NAME = "利息保障倍數";
    private static final String CATEGORY = "SOLVENCY";

    @Override
    public void calculate(FinancialData data, CalculationResult result) {
        try {
            // 1. 取得 EBIT（優先使用 EBIT，否則使用營業利益近似）
            BigDecimal ebit = data.getEbit();
            if (ebit == null) {
                ebit = data.getOperatingIncome();
            }

            // 2. 驗證必要欄位
            if (ebit == null || data.getInterestExpense() == null) {
                log.debug("計算利息保障倍數跳過: 缺少必要欄位 (ebit={}, interestExpense={})",
                        ebit, data.getInterestExpense());
                return;
            }

            // 3. 利息費用為零（無負債或利息已資本化）
            if (data.getInterestExpense().compareTo(BigDecimal.ZERO) == 0) {
                log.debug("利息費用為零，利息保障倍數不適用: stockId={}",
                        data.getStockId());
                // 可以設定為極大值表示無利息壓力
                result.addSolvencyIndicator(INDICATOR_NAME, BigDecimal.valueOf(999.99));
                return;
            }

            // 4. 利息費用為負（異常情況）
            if (data.getInterestExpense().compareTo(BigDecimal.ZERO) < 0) {
                log.warn("利息費用為負（異常）: stockId={}, interestExpense={}",
                        data.getStockId(), data.getInterestExpense());
                return;
            }

            // 5. 計算利息保障倍數
            BigDecimal interestCoverage = ebit
                    .divide(data.getInterestExpense(), 2, RoundingMode.HALF_UP);

            // 6. 驗證合理性
            if (interestCoverage.compareTo(BigDecimal.valueOf(1.5)) < 0) {
                log.warn("利息保障倍數過低（償債風險）: stockId={}, interestCoverage={}",
                        data.getStockId(), interestCoverage);
                result.getDiagnostics().addWarning(
                        String.format("利息保障倍數過低: %.2f (建議 > 1.5)", interestCoverage));
            }

            if (interestCoverage.compareTo(BigDecimal.valueOf(1.0)) < 0) {
                log.warn("利息保障倍數小於 1（嚴重償債風險）: stockId={}, interestCoverage={}",
                        data.getStockId(), interestCoverage);
                result.getDiagnostics().addWarning(
                        String.format("利息保障倍數不足 1: %.2f (EBIT 不足以支付利息)", interestCoverage));
            }

            if (interestCoverage.compareTo(BigDecimal.ZERO) < 0) {
                log.warn("利息保障倍數為負（EBIT 為負）: stockId={}, interestCoverage={}",
                        data.getStockId(), interestCoverage);
            }

            // 7. 儲存結果
            result.addSolvencyIndicator(INDICATOR_NAME, interestCoverage);

            log.debug("利息保障倍數計算成功: stockId={}, ebit={}, interestExpense={}, interestCoverage={}",
                    data.getStockId(), ebit, data.getInterestExpense(), interestCoverage);

        } catch (Exception e) {
            log.error("計算利息保障倍數時發生錯誤: stockId={}", data.getStockId(), e);
            result.getDiagnostics().addError("利息保障倍數計算失敗: " + e.getMessage());
        }
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name(INDICATOR_NAME)
                .displayName(DISPLAY_NAME)
                .category(CATEGORY)
                .description("利息保障倍數 = EBIT / 利息費用")
                .unit("倍")
                .priority("P0")
                .requiresHistory(false)
                .build();
    }
}
