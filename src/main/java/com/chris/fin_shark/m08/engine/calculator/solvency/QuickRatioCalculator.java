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
 * 速動比率計算器
 * <p>
 * 功能編號: F-M08-004
 * 計算公式: 速動比率 = (流動資產 - 存貨) / 流動負債
 * 說明: 衡量公司即時償債能力（扣除變現較慢的存貨）
 *
 * 註: 目前簡化版假設存貨為流動資產的30%
 * TODO: P1 - 從財報中取得實際存貨金額
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
public class QuickRatioCalculator implements FundamentalCalculator {

    private static final String INDICATOR_NAME = "quick_ratio";
    private static final String DISPLAY_NAME = "速動比率";
    private static final String CATEGORY = "SOLVENCY";
    private static final BigDecimal INVENTORY_RATIO = BigDecimal.valueOf(0.30);

    @Override
    public void calculate(FinancialData data, CalculationResult result) {
        try {
            // 1. 驗證必要欄位
            if (data.getCurrentAssets() == null || data.getCurrentLiabilities() == null) {
                log.warn("計算速動比率失敗: 缺少必要欄位");
                return;
            }

            // 2. 流動負債不可為零
            if (data.getCurrentLiabilities().compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("流動負債為零或負數，速動比率不適用: stockId={}",
                        data.getStockId());
                return;
            }

            // 3. 估算存貨（簡化版：假設為流動資產的30%）
            // TODO: P1 - 從 M06 財報中取得實際 inventory 欄位
            BigDecimal estimatedInventory = data.getCurrentAssets()
                    .multiply(INVENTORY_RATIO);

            // 4. 計算速動資產
            BigDecimal quickAssets = data.getCurrentAssets().subtract(estimatedInventory);

            // 5. 計算速動比率
            BigDecimal quickRatio = quickAssets
                    .divide(data.getCurrentLiabilities(), 2, RoundingMode.HALF_UP);

            // 6. 驗證合理性與風險警示
            if (quickRatio.compareTo(BigDecimal.valueOf(0.8)) < 0) {
                log.warn("速動比率偏低: stockId={}, quickRatio={}",
                        data.getStockId(), quickRatio);
                result.getDiagnostics().addWarning(
                        String.format("速動比率偏低: %.2f (低於0.8，即時償債能力不足)", quickRatio));
            }

            // 7. 儲存結果
            result.addSolvencyIndicator(INDICATOR_NAME, quickRatio);

            log.debug("速動比率計算成功: stockId={}, quickRatio={}",
                    data.getStockId(), quickRatio);

        } catch (Exception e) {
            log.error("計算速動比率時發生錯誤: stockId={}", data.getStockId(), e);
            result.getDiagnostics().addError("速動比率計算失敗: " + e.getMessage());
        }
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name(INDICATOR_NAME)
                .displayName(DISPLAY_NAME)
                .category(CATEGORY)
                .description("速動比率 = (流動資產 - 存貨) / 流動負債（目前存貨以流動資產30%估算）")
                .unit("倍")
                .priority("P0")
                .requiresHistory(false)
                .build();
    }
}
