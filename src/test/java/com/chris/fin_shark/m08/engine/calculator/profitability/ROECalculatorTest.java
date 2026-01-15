package com.chris.fin_shark.m08.engine.calculator.profitability;

import com.chris.fin_shark.m08.engine.model.CalculationResult;
import com.chris.fin_shark.m08.engine.model.Diagnostics;
import com.chris.fin_shark.m08.engine.model.FinancialData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 股東權益報酬率 (ROE) 計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("ROE 計算器測試")
class ROECalculatorTest {

    private ROECalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ROECalculator();
    }

    @Test
    @DisplayName("測試: 正常計算 ROE")
    void testNormalCalculation() {
        // Given - 淨利 100000, 權益 500000, ROE = 20%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .netIncome(BigDecimal.valueOf(100000))
                .totalEquity(BigDecimal.valueOf(500000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).containsKey("roe");
        BigDecimal roe = result.getProfitabilityIndicators().get("roe");
        assertThat(roe).isEqualByComparingTo(BigDecimal.valueOf(20.00));
    }

    @Test
    @DisplayName("測試: 缺少淨利")
    void testMissingNetIncome() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalEquity(BigDecimal.valueOf(500000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).doesNotContainKey("roe");
    }

    @Test
    @DisplayName("測試: 缺少股東權益")
    void testMissingEquity() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .netIncome(BigDecimal.valueOf(100000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).doesNotContainKey("roe");
    }

    @Test
    @DisplayName("測試: 股東權益為零")
    void testZeroEquity() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .netIncome(BigDecimal.valueOf(100000))
                .totalEquity(BigDecimal.ZERO)
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).doesNotContainKey("roe");
    }

    @Test
    @DisplayName("測試: 股東權益為負")
    void testNegativeEquity() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .netIncome(BigDecimal.valueOf(100000))
                .totalEquity(BigDecimal.valueOf(-100000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).doesNotContainKey("roe");
    }

    @Test
    @DisplayName("測試: 虧損公司 ROE 為負")
    void testNegativeRoe() {
        // Given - 淨利 -50000, 權益 500000, ROE = -10%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .netIncome(BigDecimal.valueOf(-50000))
                .totalEquity(BigDecimal.valueOf(500000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).containsKey("roe");
        BigDecimal roe = result.getProfitabilityIndicators().get("roe");
        assertThat(roe).isEqualByComparingTo(BigDecimal.valueOf(-10.00));
    }

    @Test
    @DisplayName("測試: ROE 異常高警告")
    void testHighRoeWarning() {
        // Given - ROE > 100%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .netIncome(BigDecimal.valueOf(200000))
                .totalEquity(BigDecimal.valueOf(100000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).containsKey("roe");
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("ROE 異常高");
    }

    @Test
    @DisplayName("測試: ROE 異常低警告")
    void testLowRoeWarning() {
        // Given - ROE < -50%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .netIncome(BigDecimal.valueOf(-150000))
                .totalEquity(BigDecimal.valueOf(200000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).containsKey("roe");
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("ROE 異常低");
    }

    @Test
    @DisplayName("測試: 元數據正確")
    void testMetadata() {
        // When
        var metadata = calculator.getMetadata();

        // Then
        assertThat(metadata.getName()).isEqualTo("roe");
        assertThat(metadata.getCategory()).isEqualTo("PROFITABILITY");
        assertThat(metadata.getPriority()).isEqualTo("P0");
        assertThat(metadata.getUnit()).isEqualTo("%");
    }

    private CalculationResult createResult() {
        return CalculationResult.builder()
                .diagnostics(new Diagnostics())
                .build();
    }
}
