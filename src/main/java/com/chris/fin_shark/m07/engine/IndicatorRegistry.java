package com.chris.fin_shark.m07.engine;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 指標註冊表
 * <p>
 * 管理所有可用的指標計算器
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
public class IndicatorRegistry {

    private final Map<String, IndicatorCalculator> calculators = new ConcurrentHashMap<>();
    private final Map<String, IndicatorMetadata> metadata = new ConcurrentHashMap<>();

    /**
     * 建構子：自動註冊所有計算器
     */
    public IndicatorRegistry(List<IndicatorCalculator> calculatorList) {
        calculatorList.forEach(this::register);

        log.info("========================================");
        log.info("🚀 指標註冊表初始化完成");
        log.info("========================================");
        log.info("已註冊 {} 個指標計算器:", calculators.size());

        calculators.forEach((name, calc) -> {
            IndicatorMetadata meta = calc.getMetadata();
            log.info("  ✅ {} ({}) - {} - 優先級: {}",
                    name,
                    meta.getNameZh(),
                    meta.getCategory(),
                    meta.getPriority());
        });
    }

    /**
     * 註冊指標計算器
     */
    public void register(IndicatorCalculator calculator) {
        String name = calculator.getName();
        calculators.put(name, calculator);
        metadata.put(name, calculator.getMetadata());
    }

    /**
     * 取得計算器
     */
    public Optional<IndicatorCalculator> getCalculator(String indicatorName) {
        return Optional.ofNullable(calculators.get(indicatorName));
    }

    /**
     * 取得指標元資料
     */
    public Optional<IndicatorMetadata> getMetadata(String indicatorName) {
        return Optional.ofNullable(metadata.get(indicatorName));
    }

    /**
     * 取得所有已註冊的指標名稱
     */
    public Set<String> getAllIndicatorNames() {
        return calculators.keySet();
    }

    /**
     * 根據優先級取得指標
     */
    public List<String> getIndicatorsByPriority(String priority) {
        return metadata.values().stream()
                .filter(m -> priority.equals(m.getPriority()))
                .map(IndicatorMetadata::getName)
                .collect(Collectors.toList());
    }

    /**
     * 根據類別取得指標
     */
    public List<String> getIndicatorsByCategory(String category) {
        return metadata.values().stream()
                .filter(m -> category.equals(m.getCategory()))
                .map(IndicatorMetadata::getName)
                .collect(Collectors.toList());
    }

    /**
     * 取得所有計算器
     */
    public Map<String, IndicatorCalculator> getAllCalculators() {
        return new HashMap<>(calculators);
    }
}
