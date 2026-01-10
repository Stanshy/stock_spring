package com.chris.fin_shark.m08.engine;

import com.chris.fin_shark.m08.engine.model.CalculationPlan;
import com.chris.fin_shark.m08.engine.model.CalculationResult;
import com.chris.fin_shark.m08.engine.model.Diagnostics;
import com.chris.fin_shark.m08.engine.model.FinancialData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 預設基本面分析引擎實作
 * <p>
 * 協調所有計算器執行，收集計算結果與診斷資訊
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DefaultFundamentalEngine implements FundamentalEngine {

    private final M08IndicatorRegistry registry;

    /**
     * 執行完整指標計算
     */
    @Override
    public CalculationResult calculate(FinancialData data) {
        log.debug("開始執行完整指標計算: stockId={}, year={}, quarter={}",
                data.getStockId(), data.getYear(), data.getQuarter());

        long startTime = System.currentTimeMillis();

        // 1. 建立結果容器
        CalculationResult result = CalculationResult.builder()
                .stockId(data.getStockId())
                .year(data.getYear())
                .quarter(data.getQuarter())
                .build();

        // 2. 取得所有已註冊的計算器
        List<FundamentalCalculator> calculators = registry.getAllCalculators();
        log.debug("已載入 {} 個計算器", calculators.size());

        // 3. 逐一執行計算
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        for (FundamentalCalculator calculator : calculators) {
            try {
                calculator.calculate(data, result);
                log.trace("計算器執行成功: {}", calculator.getMetadata().getName());

            } catch (Exception e) {
                String errorMsg = String.format("計算器 %s 執行失敗: %s",
                        calculator.getMetadata().getName(), e.getMessage());
                errors.add(errorMsg);
                log.error(errorMsg, e);
            }
        }

        // 4. 建立診斷資訊
        long calculationTime = System.currentTimeMillis() - startTime;
        Diagnostics diagnostics = Diagnostics.builder()
                .calculationTime(calculationTime)
                .totalCalculators(calculators.size())
                .successfulCalculators(calculators.size() - errors.size())
                .errors(errors)
                .warnings(warnings)
                .build();

        result.setDiagnostics(diagnostics);

        log.info("指標計算完成: stockId={}, 總計算器={}, 成功={}, 失敗={}, 耗時={}ms",
                data.getStockId(), calculators.size(),
                calculators.size() - errors.size(), errors.size(), calculationTime);

        return result;
    }

    /**
     * 執行部分指標計算（依計劃）
     */
    @Override
    public CalculationResult calculate(FinancialData data, CalculationPlan plan) {
        log.debug("開始執行部分指標計算: stockId={}, plan={}",
                data.getStockId(), plan);

        long startTime = System.currentTimeMillis();

        // 1. 建立結果容器
        CalculationResult result = CalculationResult.builder()
                .stockId(data.getStockId())
                .year(data.getYear())
                .quarter(data.getQuarter())
                .build();

        // 2. 根據計劃篩選計算器
        List<FundamentalCalculator> calculators = filterCalculatorsByPlan(plan);
        log.debug("根據計劃篩選出 {} 個計算器", calculators.size());

        // 3. 執行計算
        List<String> errors = new ArrayList<>();
        for (FundamentalCalculator calculator : calculators) {
            try {
                calculator.calculate(data, result);
            } catch (Exception e) {
                String errorMsg = String.format("計算器 %s 執行失敗: %s",
                        calculator.getMetadata().getName(), e.getMessage());
                errors.add(errorMsg);
                log.error(errorMsg, e);
            }
        }

        // 4. 建立診斷資訊
        long calculationTime = System.currentTimeMillis() - startTime;
        Diagnostics diagnostics = Diagnostics.builder()
                .calculationTime(calculationTime)
                .totalCalculators(calculators.size())
                .successfulCalculators(calculators.size() - errors.size())
                .errors(errors)
                .build();

        result.setDiagnostics(diagnostics);

        return result;
    }

    /**
     * 取得支援的指標清單
     */
    @Override
    public List<String> getSupportedIndicators() {
        return registry.getAllCalculators().stream()
                .map(calc -> calc.getMetadata().getName())
                .collect(Collectors.toList());
    }

    /**
     * 根據計劃篩選計算器
     */
    private List<FundamentalCalculator> filterCalculatorsByPlan(CalculationPlan plan) {
        List<FundamentalCalculator> allCalculators = registry.getAllCalculators();
        List<FundamentalCalculator> filtered = new ArrayList<>();

        for (FundamentalCalculator calculator : allCalculators) {
            String category = calculator.getCategory();

            // 根據計劃的設定決定是否包含此類別
            boolean shouldInclude = switch (category) {
                case "VALUATION" -> plan.isIncludeValuation();
                case "PROFITABILITY" -> plan.isIncludeProfitability();
                case "FINANCIAL_STRUCTURE" -> plan.isIncludeFinancialStructure();
                case "SOLVENCY" -> plan.isIncludeSolvency();
                case "CASH_FLOW" -> plan.isIncludeCashFlow();
                case "GROWTH" -> plan.isIncludeGrowth();
                case "DIVIDEND" -> plan.isIncludeDividend();
                case "SCORE" -> plan.isIncludeScore();
                default -> false;
            };

            if (shouldInclude) {
                filtered.add(calculator);
            }
        }

        return filtered;
    }
}
