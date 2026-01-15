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
 * 總資產報酬率 (ROA) 計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("ROA 計算器測試")
class ROACalculatorTest {

    private ROACalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ROACalculator();
    }

    @Test
    @DisplayName("測試: 正常計算 ROA")
    void testNormalCalculation() {
        // Given - 淨利 100000, 總資產 1000000, ROA = 10%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .netIncome(BigDecimal.valueOf(100000))
                .totalAssets(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).containsKey("roa");
        BigDecimal roa = result.getProfitabilityIndicators().get("roa");
        assertThat(roa).isEqualByComparingTo(BigDecimal.valueOf(10.00));
    }

    @Test
    @DisplayName("測試: 缺少淨利")
    void testMissingNetIncome() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalAssets(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).doesNotContainKey("roa");
    }

    @Test
    @DisplayName("測試: 缺少總資產")
    void testMissingTotalAssets() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .netIncome(BigDecimal.valueOf(100000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).doesNotContainKey("roa");
    }

    @Test
    @DisplayName("測試: 總資產為零")
    void testZeroTotalAssets() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .netIncome(BigDecimal.valueOf(100000))
                .totalAssets(BigDecimal.ZERO)
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).doesNotContainKey("roa");
    }

    @Test
    @DisplayName("測試: 虧損公司 ROA 為負")
    void testNegativeRoa() {
        // Given - 淨利 -50000, 資產 1000000, ROA = -5%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .netIncome(BigDecimal.valueOf(-50000))
                .totalAssets(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).containsKey("roa");
        BigDecimal roa = result.getProfitabilityIndicators().get("roa");
        assertThat(roa).isEqualByComparingTo(BigDecimal.valueOf(-5.00));
    }

    @Test
    @DisplayName("測試: ROA 異常高警告")
    void testHighRoaWarning() {
        // Given - ROA > 50%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .netIncome(BigDecimal.valueOf(600000))
                .totalAssets(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).containsKey("roa");
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("ROA 異常高");
    }

    @Test
    @DisplayName("測試: 元數據正確")
    void testMetadata() {
        // When
        var metadata = calculator.getMetadata();

        // Then
        assertThat(metadata.getName()).isEqualTo("roa");
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
