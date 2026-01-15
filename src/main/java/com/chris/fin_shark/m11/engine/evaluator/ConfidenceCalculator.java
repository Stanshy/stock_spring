package com.chris.fin_shark.m11.engine.evaluator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 信心度計算器
 * <p>
 * 根據公式計算信號的信心度分數
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
public class ConfidenceCalculator {

    // 變數提取正則（支援 M07_RSI_14 格式和簡單變數名）
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\b([A-Za-z][A-Za-z0-9_]*)\\b");

    /**
     * 計算信心度分數
     *
     * @param formula    信心度公式
     * @param factorData 因子數據
     * @return 信心度分數（0-100）
     */
    public BigDecimal calculate(String formula, Map<String, Object> factorData) {
        if (formula == null || formula.isBlank()) {
            return BigDecimal.valueOf(50); // 預設信心度
        }

        try {
            // 替換公式中的變數
            String expression = substituteVariables(formula, factorData);

            // 評估表達式
            double rawScore = evaluateExpression(expression);

            // 正規化至 0-100 範圍
            BigDecimal score = BigDecimal.valueOf(rawScore * 100)
                    .max(BigDecimal.ZERO)
                    .min(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);

            log.debug("信心度計算: formula={}, result={}", formula, score);
            return score;

        } catch (Exception e) {
            log.error("信心度計算失敗: formula={}", formula, e);
            return BigDecimal.valueOf(50); // 計算失敗時使用預設值
        }
    }

    /**
     * 替換公式中的變數
     */
    private String substituteVariables(String formula, Map<String, Object> factorData) {
        StringBuffer result = new StringBuffer();
        Matcher matcher = VARIABLE_PATTERN.matcher(formula);

        while (matcher.find()) {
            String variable = matcher.group(1);

            // 跳過數學函數名稱
            if (isMathFunction(variable)) {
                continue;
            }

            // 嘗試多種變數格式查找
            Object value = findFactorValue(variable, factorData);

            if (value != null) {
                String replacement = String.valueOf(toDouble(value));
                matcher.appendReplacement(result, replacement);
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 查找因子值（支援多種格式）
     */
    private Object findFactorValue(String variable, Map<String, Object> factorData) {
        // 直接查找
        if (factorData.containsKey(variable)) {
            return factorData.get(variable);
        }

        // 嘗試轉換為標準格式（如 RSI -> M07_RSI_14）
        String upperVar = variable.toUpperCase();

        // 常見縮寫映射
        switch (upperVar) {
            case "RSI":
                return factorData.get("M07_RSI_14");
            case "PE":
                return factorData.get("M08_PE_RATIO");
            case "PB":
                return factorData.get("M08_PB_RATIO");
            case "ROE":
                return factorData.get("M08_ROE");
            case "KD_K":
            case "K":
                return factorData.get("M07_KD_K");
            case "KD_D":
            case "D":
                return factorData.get("M07_KD_D");
            case "FOREIGN_NET":
                return factorData.get("M09_FOREIGN_NET");
            case "TRUST_NET":
                return factorData.get("M09_TRUST_NET");
            case "VOLUME_RATIO":
                return factorData.get("M06_VOLUME_RATIO");
            case "CHIP_SCORE":
                return factorData.get("M09_CHIP_SCORE");
            default:
                // 嘗試加上模組前綴
                for (String prefix : new String[]{"M06_", "M07_", "M08_", "M09_"}) {
                    String key = prefix + upperVar;
                    if (factorData.containsKey(key)) {
                        return factorData.get(key);
                    }
                }
        }

        log.warn("找不到因子值: {}", variable);
        return null;
    }

    /**
     * 檢查是否為數學函數
     */
    private boolean isMathFunction(String name) {
        return name.equalsIgnoreCase("MIN") ||
                name.equalsIgnoreCase("MAX") ||
                name.equalsIgnoreCase("ABS") ||
                name.equalsIgnoreCase("NORMALIZE");
    }

    /**
     * 評估數學表達式（簡化版本）
     */
    private double evaluateExpression(String expression) {
        try {
            // 使用 JavaScript 引擎評估（簡化實作）
            // 實際生產環境可考慮使用專門的表達式解析庫如 exp4j

            // 簡化處理：只支援基本四則運算
            expression = expression.replaceAll("\\s+", "");

            // 嘗試直接解析數字
            if (expression.matches("-?\\d+(\\.\\d+)?")) {
                return Double.parseDouble(expression);
            }

            // 使用 Java ScriptEngine 評估
            javax.script.ScriptEngineManager mgr = new javax.script.ScriptEngineManager();
            javax.script.ScriptEngine engine = mgr.getEngineByName("JavaScript");

            if (engine != null) {
                Object result = engine.eval(expression);
                if (result instanceof Number) {
                    return ((Number) result).doubleValue();
                }
            }

            // 備用：簡單解析
            return simpleEvaluate(expression);

        } catch (Exception e) {
            log.warn("表達式評估失敗: {}", expression, e);
            return 0.5; // 預設值
        }
    }

    /**
     * 簡單表達式評估（備用）
     */
    private double simpleEvaluate(String expression) {
        // 這是一個非常簡化的實作，只處理簡單情況
        // 生產環境應使用成熟的表達式解析庫

        try {
            // 移除空格
            expression = expression.trim();

            // 嘗試解析為單一數字
            return Double.parseDouble(expression);
        } catch (NumberFormatException e) {
            // 無法解析，返回預設值
            return 0.5;
        }
    }

    /**
     * 轉換為 double
     */
    private double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
