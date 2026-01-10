package com.chris.fin_shark.m08.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 指標註冊表
 * <p>
 * 管理所有已註冊的財務指標計算器
 * 參考 M07 IndicatorRegistry 設計
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
public class M08IndicatorRegistry {

    /**
     * 計算器註冊表（key: 指標名稱, value: 計算器實例）
     */
    private final Map<String, FundamentalCalculator> calculators = new ConcurrentHashMap<>();

    /**
     * 註冊計算器
     *
     * @param calculator 計算器實例
     */
    public void register(FundamentalCalculator calculator) {
        String indicatorName = calculator.getMetadata().getName();
        calculators.put(indicatorName, calculator);
        log.info("註冊計算器: name={}, category={}",
                indicatorName, calculator.getCategory());
    }

    /**
     * 批次註冊計算器
     *
     * @param calculatorList 計算器列表
     */
    public void registerAll(List<FundamentalCalculator> calculatorList) {
        calculatorList.forEach(this::register);
        log.info("批次註冊計算器完成，共 {} 個", calculatorList.size());
    }

    /**
     * 取得單一計算器
     *
     * @param indicatorName 指標名稱
     * @return 計算器實例
     */
    public FundamentalCalculator getCalculator(String indicatorName) {
        return calculators.get(indicatorName);
    }

    /**
//     * 取得所有計算器
     *
     * @return 計算器列表
     */
    public List<FundamentalCalculator> getAllCalculators() {
        return new ArrayList<>(calculators.values());
    }

    /**
     * 取得已註冊的指標數量
     *
     * @return 指標數量
     */
    public int size() {
        return calculators.size();
    }
}
