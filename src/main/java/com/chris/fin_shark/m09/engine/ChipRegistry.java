package com.chris.fin_shark.m09.engine;

import com.chris.fin_shark.m09.engine.calculator.ChipCalculator;
import com.chris.fin_shark.m09.engine.model.ChipMetadata;
import com.chris.fin_shark.m09.enums.ChipCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 籌碼計算器註冊表
 * <p>
 * 管理所有可用的籌碼計算器，提供查詢與分類功能。
 * 與 M07 IndicatorRegistry 結構對齊。
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
public class ChipRegistry {

    private final Map<String, ChipCalculator> calculators = new ConcurrentHashMap<>();
    private final Map<String, ChipMetadata> metadata = new ConcurrentHashMap<>();

    /**
     * 建構子：自動註冊所有計算器
     *
     * @param calculatorList Spring 自動注入的所有 ChipCalculator 實現
     */
    public ChipRegistry(List<ChipCalculator> calculatorList) {
        calculatorList.forEach(this::register);

        log.info("========================================");
        log.info("籌碼計算器註冊表初始化完成");
        log.info("========================================");
        log.info("已註冊 {} 個籌碼計算器:", calculators.size());

        calculators.forEach((name, calc) -> {
            ChipMetadata meta = calc.getMetadata();
            log.info("  [OK] {} ({}) - {} - 優先級: {}",
                    name,
                    meta.getNameZh(),
                    meta.getCategory(),
                    meta.getPriority());
        });
    }

    /**
     * 註冊計算器
     *
     * @param calculator 計算器
     */
    public void register(ChipCalculator calculator) {
        String name = calculator.getName();
        calculators.put(name, calculator);
        metadata.put(name, calculator.getMetadata());
    }

    /**
     * 取得計算器
     *
     * @param calculatorName 計算器名稱
     * @return 計算器（Optional）
     */
    public Optional<ChipCalculator> getCalculator(String calculatorName) {
        return Optional.ofNullable(calculators.get(calculatorName));
    }

    /**
     * 取得計算器元資料
     *
     * @param calculatorName 計算器名稱
     * @return 元資料（Optional）
     */
    public Optional<ChipMetadata> getMetadata(String calculatorName) {
        return Optional.ofNullable(metadata.get(calculatorName));
    }

    /**
     * 取得所有已註冊的計算器名稱
     */
    public Set<String> getAllCalculatorNames() {
        return calculators.keySet();
    }

    /**
     * 取得所有計算器
     */
    public Map<String, ChipCalculator> getAllCalculators() {
        return new HashMap<>(calculators);
    }

    /**
     * 根據類別取得計算器
     *
     * @param category 類別
     * @return 計算器名稱列表
     */
    public List<String> getCalculatorsByCategory(ChipCategory category) {
        return metadata.values().stream()
                .filter(m -> category.equals(m.getCategory()))
                .map(ChipMetadata::getName)
                .collect(Collectors.toList());
    }

    /**
     * 根據優先級取得計算器
     *
     * @param priority 優先級（P0, P1, P2）
     * @return 計算器名稱列表
     */
    public List<String> getCalculatorsByPriority(String priority) {
        return metadata.values().stream()
                .filter(m -> priority.equals(m.getPriority()))
                .map(ChipMetadata::getName)
                .collect(Collectors.toList());
    }

    /**
     * 根據類別取得計算器實例
     *
     * @param category 類別
     * @return 計算器列表
     */
    public List<ChipCalculator> getCalculatorInstancesByCategory(ChipCategory category) {
        return calculators.values().stream()
                .filter(c -> category.equals(c.getCategory()))
                .collect(Collectors.toList());
    }
}
