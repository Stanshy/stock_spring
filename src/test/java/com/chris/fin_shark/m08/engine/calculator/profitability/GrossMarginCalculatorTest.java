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
 * 毛利率計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("毛利率計算器測試")
class GrossMarginCalculatorTest {

    private GrossMarginCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new GrossMarginCalculator();
    }

    @Test
    @DisplayName("測試: 正常計算毛利率")
    void testNormalCalculation() {
        // Given - 營收 1000000, 成本 600000, 毛利率 = 40%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .revenue(BigDecimal.valueOf(1000000))
                .operatingCost(BigDecimal.valueOf(600000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).containsKey("gross_margin");
        BigDecimal grossMargin = result.getProfitabilityIndicators().get("gross_margin");
        assertThat(grossMargin).isEqualByComparingTo(BigDecimal.valueOf(40.00));
    }

    @Test
    @DisplayName("測試: 高毛利率產業")
    void testHighMarginIndustry() {
        // Given - 營收 1000000, 成本 200000, 毛利率 = 80%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .revenue(BigDecimal.valueOf(1000000))
                .operatingCost(BigDecimal.valueOf(200000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).containsKey("gross_margin");
        BigDecimal grossMargin = result.getProfitabilityIndicators().get("gross_margin");
        assertThat(grossMargin).isEqualByComparingTo(BigDecimal.valueOf(80.00));
    }

    @Test
    @DisplayName("測試: 缺少營收")
    void testMissingRevenue() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingCost(BigDecimal.valueOf(600000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).doesNotContainKey("gross_margin");
    }

    @Test
    @DisplayName("測試: 缺少營業成本")
    void testMissingOperatingCost() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .revenue(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).doesNotContainKey("gross_margin");
    }

    @Test
    @DisplayName("測試: 營收為零")
    void testZeroRevenue() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .revenue(BigDecimal.ZERO)
                .operatingCost(BigDecimal.valueOf(600000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).doesNotContainKey("gross_margin");
    }

    @Test
    @DisplayName("測試: 毛利率為負（成本高於營收）")
    void testNegativeGrossMargin() {
        // Given - 營收 500000, 成本 700000, 毛利率 = -40%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .revenue(BigDecimal.valueOf(500000))
                .operatingCost(BigDecimal.valueOf(700000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).containsKey("gross_margin");
        BigDecimal grossMargin = result.getProfitabilityIndicators().get("gross_margin");
        assertThat(grossMargin).isEqualByComparingTo(BigDecimal.valueOf(-40.00));
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("毛利率為負");
    }

    @Test
    @DisplayName("測試: 毛利率異常高警告")
    void testHighGrossMarginWarning() {
        // Given - 毛利率 > 90%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .revenue(BigDecimal.valueOf(1000000))
                .operatingCost(BigDecimal.valueOf(50000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).containsKey("gross_margin");
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("毛利率異常高");
    }

    @Test
    @DisplayName("測試: 元數據正確")
    void testMetadata() {
        // When
        var metadata = calculator.getMetadata();

        // Then
        assertThat(metadata.getName()).isEqualTo("gross_margin");
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
