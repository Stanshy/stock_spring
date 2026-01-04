package com.chris.fin_shark.m07.engine;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 預設指標引擎實現
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
public class DefaultIndicatorEngine implements IndicatorEngine {

    private final Map<String, IndicatorCalculator> calculators;

    public DefaultIndicatorEngine(List<IndicatorCalculator> calculatorList) {
        this.calculators = new HashMap<>();

        // 自動註冊所有計算器
        calculatorList.forEach(calculator -> {
            calculators.put(calculator.getName(), calculator);
            log.info("✅ 註冊指標計算器: {}", calculator.getName());
        });

        log.info("🚀 指標引擎初始化完成，共註冊 {} 個計算器", calculators.size());
    }

    @Override
    public IndicatorResult compute(PriceSeries series, IndicatorPlan plan) {
        log.debug("開始計算指標: stockId={}, indicators={}",
                series.getStockId(), plan.getIndicators().keySet());

        IndicatorResult result = IndicatorResult.builder()
                .stockId(series.getStockId())
                .calculationDate(LocalDate.now())
                .diagnostics(new Diagnostics())
                .build();

        // 執行各個指標計算
        plan.getIndicators().forEach((indicatorName, params) -> {
            try {
                IndicatorCalculator calculator = calculators.get(indicatorName);

                if (calculator == null) {
                    result.getDiagnostics().addError(indicatorName, "找不到計算器");
                    return;
                }

                // 檢查資料是否足夠
                if (!calculator.hasEnoughData(series, params)) {
                    result.getDiagnostics().addWarning(
                            indicatorName,
                            String.format("資料不足：需要%d天，實際%d天",
                                    calculator.getMetadata().getMinDataPoints(),
                                    series.size())
                    );
                    return;
                }

                // 計算
                Map<String, Object> values = calculator.calculate(series, params);

                // 根據類別儲存結果
                String category = calculator.getCategory();
                switch (category) {
                    case "TREND" -> values.forEach(result::addTrendIndicator);
                    case "MOMENTUM" -> values.forEach(result.getMomentumIndicators()::put);
                    case "VOLATILITY" -> values.forEach(result.getVolatilityIndicators()::put);
                    case "VOLUME" -> values.forEach(result.getVolumeIndicators()::put);
                }

                log.debug("✅ 計算完成: {}, values={}", indicatorName, values);

            } catch (Exception e) {
                log.error("❌ 計算失敗: {}, error={}", indicatorName, e.getMessage());
                result.getDiagnostics().addError(indicatorName, e.getMessage());
            }
        });

        return result;
    }

    @Override
    public Map<String, IndicatorResult> batchCompute(
            Map<String, PriceSeries> seriesMap,
            IndicatorPlan plan) {

        Map<String, IndicatorResult> results = new HashMap<>();

        seriesMap.forEach((stockId, series) -> {
            IndicatorResult result = compute(series, plan);
            results.put(stockId, result);
        });

        return results;
    }
}
