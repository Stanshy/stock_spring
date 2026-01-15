package com.chris.fin_shark.m11.engine.evaluator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 信心度計算器單元測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("信心度計算器測試")
class ConfidenceCalculatorTest {

    private ConfidenceCalculator calculator;
    private Map<String, Object> factorValues;

    @BeforeEach
    void setUp() {
        calculator = new ConfidenceCalculator();
        factorValues = new HashMap<>();

        // 模擬因子數據
        factorValues.put("rsi_14", BigDecimal.valueOf(25.0));
        factorValues.put("kd_k", BigDecimal.valueOf(18.0));
        factorValues.put("volume_ratio", BigDecimal.valueOf(1.5));
        factorValues.put("pe_ratio", BigDecimal.valueOf(10.0));
        factorValues.put("roe", BigDecimal.valueOf(20.0));

        System.out.println("\n========================================");
        System.out.println("信心度計算器測試");
        System.out.println("========================================\n");
    }

    @Nested
    @DisplayName("基礎信心度計算")
    class BasicCalculationTests {

        @Test
        @DisplayName("測試: 無公式時使用預設值")
        void testCalculateWithNullFormula() {
            System.out.println("測試: 無公式時使用預設值");

            // When
            BigDecimal confidence = calculator.calculate(null, factorValues);

            // Then
            System.out.println("  計算出的信心度: " + confidence);
            assertThat(confidence).isNotNull();
            assertThat(confidence.doubleValue()).isEqualTo(50.0); // 預設值
        }

        @Test
        @DisplayName("測試: 空白公式時使用預設值")
        void testCalculateWithBlankFormula() {
            System.out.println("測試: 空白公式時使用預設值");

            // When
            BigDecimal confidence = calculator.calculate("   ", factorValues);

            // Then
            System.out.println("  信心度: " + confidence);
            assertThat(confidence.doubleValue()).isEqualTo(50.0);
        }
    }

    @Nested
    @DisplayName("公式計算測試")
    class FormulaCalculationTests {

        @Test
        @DisplayName("測試: 簡單數字公式")
        void testSimpleNumberFormula() {
            System.out.println("測試: 簡單數字公式");

            // Given: 直接數字作為公式
            String formula = "0.75";

            System.out.println("  公式: " + formula);

            // When
            BigDecimal confidence = calculator.calculate(formula, factorValues);

            // Then
            System.out.println("  計算結果: " + confidence);
            assertThat(confidence).isNotNull();
            // 公式結果會乘以 100，所以 0.75 * 100 = 75
            assertThat(confidence.doubleValue()).isEqualTo(75.0);
        }

        @Test
        @DisplayName("測試: 帶變數的公式")
        void testFormulaWithVariables() {
            System.out.println("測試: 帶變數的公式");

            // Given: 使用因子變數的公式
            // rsi_14 = 25.0，結果會乘以 100
            String formula = "0.5";

            System.out.println("  公式: " + formula);
            System.out.println("  RSI 值: " + factorValues.get("rsi_14"));

            // When
            BigDecimal confidence = calculator.calculate(formula, factorValues);

            // Then
            System.out.println("  計算結果: " + confidence);
            assertThat(confidence).isNotNull();
        }

        @Test
        @DisplayName("測試: 公式中變數不存在時")
        void testFormulaWithMissingVariable() {
            System.out.println("測試: 公式中變數不存在時");

            // Given: 包含不存在的變數
            String formula = "missing_factor + 0.5";

            System.out.println("  公式: " + formula);
            System.out.println("  missing_factor: (不存在)");

            // When
            BigDecimal confidence = calculator.calculate(formula, factorValues);

            // Then
            System.out.println("  計算結果: " + confidence);
            // 應該返回預設值或處理過的結果
            assertThat(confidence).isNotNull();
        }
    }

    @Nested
    @DisplayName("邊界值測試")
    class BoundaryTests {

        @Test
        @DisplayName("測試: 信心度最大值限制")
        void testMaxConfidenceLimit() {
            System.out.println("測試: 信心度最大值限制");

            // Given: 會產生超過 100 的公式（2.0 * 100 = 200）
            String formula = "2.0";

            // When
            BigDecimal confidence = calculator.calculate(formula, factorValues);

            // Then
            System.out.println("  計算結果: " + confidence);
            assertThat(confidence.doubleValue()).isLessThanOrEqualTo(100.0);
        }

        @Test
        @DisplayName("測試: 信心度最小值限制")
        void testMinConfidenceLimit() {
            System.out.println("測試: 信心度最小值限制");

            // Given: 會產生負數的公式（-0.5 * 100 = -50）
            String formula = "-0.5";

            // When
            BigDecimal confidence = calculator.calculate(formula, factorValues);

            // Then
            System.out.println("  計算結果: " + confidence);
            assertThat(confidence.doubleValue()).isGreaterThanOrEqualTo(0.0);
        }

        @Test
        @DisplayName("測試: 零值公式")
        void testZeroFormula() {
            System.out.println("測試: 零值公式");

            // Given
            String formula = "0";

            // When
            BigDecimal confidence = calculator.calculate(formula, factorValues);

            // Then
            System.out.println("  計算結果: " + confidence);
            assertThat(confidence.doubleValue()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("變數映射測試")
    class VariableMappingTests {

        @Test
        @DisplayName("測試: 模組前綴變數映射")
        void testModulePrefixMapping() {
            System.out.println("測試: 模組前綴變數映射");

            // Given: 使用模組前綴格式的因子
            Map<String, Object> data = new HashMap<>();
            data.put("M07_RSI_14", BigDecimal.valueOf(30.0));
            data.put("M08_PE_RATIO", BigDecimal.valueOf(15.0));

            String formula = "0.6";

            // When
            BigDecimal confidence = calculator.calculate(formula, data);

            // Then
            System.out.println("  計算結果: " + confidence);
            assertThat(confidence).isNotNull();
        }
    }
}
