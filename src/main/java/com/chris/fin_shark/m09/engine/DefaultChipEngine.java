package com.chris.fin_shark.m09.engine;

import com.chris.fin_shark.m09.engine.calculator.ChipCalculator;
import com.chris.fin_shark.m09.engine.model.ChipSeries;
import com.chris.fin_shark.m09.engine.model.Diagnostics;
import com.chris.fin_shark.m09.enums.ChipCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 預設籌碼計算引擎實現
 * <p>
 * 根據 ChipPlan 執行指定類別的計算器，彙整計算結果。
 * 與 M07 DefaultIndicatorEngine 結構對齊。
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
public class DefaultChipEngine implements ChipEngine {

    private final ChipRegistry registry;

    public DefaultChipEngine(ChipRegistry registry) {
        this.registry = registry;
        log.info("籌碼計算引擎初始化完成");
    }

    @Override
    public ChipResult compute(ChipSeries series, ChipPlan plan) {
        log.debug("開始計算籌碼指標: stockId={}", series.getStockId());

        long startTime = System.currentTimeMillis();

        ChipResult result = ChipResult.builder()
                .stockId(series.getStockId())
                .calculationDate(LocalDate.now())
                .diagnostics(new Diagnostics())
                .build();

        // 根據 Plan 決定要執行的類別
        if (plan.isIncludeInstitutional()) {
            executeCategory(series, plan, ChipCategory.INSTITUTIONAL, result);
        }

        if (plan.isIncludeMargin()) {
            executeCategory(series, plan, ChipCategory.MARGIN, result);
        }

        if (plan.isIncludeConcentration()) {
            executeCategory(series, plan, ChipCategory.CONCENTRATION, result);
        }

        if (plan.isIncludeCost()) {
            executeCategory(series, plan, ChipCategory.COST, result);
        }

        if (plan.isIncludeSignals()) {
            executeCategory(series, plan, ChipCategory.SIGNAL, result);
        }

        // 記錄計算耗時
        long duration = System.currentTimeMillis() - startTime;
        result.getDiagnostics().setCalculationTimeMs(duration);

        log.debug("籌碼計算完成: stockId={}, duration={}ms", series.getStockId(), duration);

        return result;
    }

    @Override
    public Map<String, ChipResult> batchCompute(Map<String, ChipSeries> seriesMap, ChipPlan plan) {
        log.info("批次計算籌碼指標: {} 支股票", seriesMap.size());

        Map<String, ChipResult> results = new HashMap<>();

        seriesMap.forEach((stockId, series) -> {
            try {
                ChipResult result = compute(series, plan);
                results.put(stockId, result);
            } catch (Exception e) {
                log.error("計算失敗: stockId={}, error={}", stockId, e.getMessage());
                // 建立包含錯誤的結果
                ChipResult errorResult = ChipResult.builder()
                        .stockId(stockId)
                        .calculationDate(LocalDate.now())
                        .diagnostics(new Diagnostics())
                        .build();
                errorResult.getDiagnostics().addError("engine", e.getMessage());
                results.put(stockId, errorResult);
            }
        });

        log.info("批次計算完成: 成功 {} / 總計 {}",
                results.values().stream().filter(r -> !r.hasErrors()).count(),
                results.size());

        return results;
    }

    /**
     * 執行特定類別的計算器
     */
    private void executeCategory(ChipSeries series,
                                  ChipPlan plan,
                                  ChipCategory category,
                                  ChipResult result) {

        List<ChipCalculator> calculators = registry.getCalculatorInstancesByCategory(category);

        if (calculators.isEmpty()) {
            log.debug("類別 {} 沒有註冊的計算器", category);
            return;
        }

        for (ChipCalculator calculator : calculators) {
            try {
                // 檢查資料是否足夠
                if (!calculator.hasEnoughData(series, Map.of())) {
                    result.getDiagnostics().addWarning(
                            calculator.getName(),
                            String.format("資料不足：需要 %d 天，實際 %d 天",
                                    calculator.getMetadata().getMinDataDays(),
                                    series.size())
                    );
                    continue;
                }

                // 執行計算
                Map<String, Object> values = calculator.calculate(series, Map.of());

                // 根據類別儲存結果
                storeResults(category, values, result);

                log.debug("[OK] 計算完成: {}, values={}", calculator.getName(), values.keySet());

            } catch (Exception e) {
                log.error("[FAIL] 計算失敗: {}, error={}", calculator.getName(), e.getMessage());
                result.getDiagnostics().addError(calculator.getName(), e.getMessage());
            }
        }
    }

    /**
     * 根據類別儲存計算結果
     */
    private void storeResults(ChipCategory category, Map<String, Object> values, ChipResult result) {
        switch (category) {
            case INSTITUTIONAL -> values.forEach(result::addInstitutionalIndicator);
            case MARGIN -> values.forEach(result::addMarginIndicator);
            case CONCENTRATION -> values.forEach(result::addConcentrationIndicator);
            case COST -> values.forEach(result::addCostIndicator);
            case SIGNAL -> {
                // 訊號類別可能返回 ChipSignal 列表
                // TODO(decision): 訊號偵測器的返回格式需要與其他計算器統一處理
                values.forEach(result::addInstitutionalIndicator);
            }
        }
    }
}
