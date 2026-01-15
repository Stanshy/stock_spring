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
 * 淨利率計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("淨利率計算器測試")
class NetMarginCalculatorTest {

    private NetMarginCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new NetMarginCalculator();
    }

    @Test
    @DisplayName("測試: 正常計算淨利率")
    void testNormalCalculation() {
        // Given - 淨利 150000, 營收 1000000, 淨利率 = 15%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .netIncome(BigDecimal.valueOf(150000))
                .revenue(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).containsKey("net_margin");
        BigDecimal netMargin = result.getProfitabilityIndicators().get("net_margin");
        assertThat(netMargin).isEqualByComparingTo(BigDecimal.valueOf(15.00));
    }

    @Test
    @DisplayName("測試: 缺少淨利")
    void testMissingNetIncome() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .revenue(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).doesNotContainKey("net_margin");
    }

    @Test
    @DisplayName("測試: 缺少營收")
    void testMissingRevenue() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .netIncome(BigDecimal.valueOf(150000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).doesNotContainKey("net_margin");
    }

    @Test
    @DisplayName("測試: 營收為零")
    void testZeroRevenue() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .netIncome(BigDecimal.valueOf(150000))
                .revenue(BigDecimal.ZERO)
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).doesNotContainKey("net_margin");
    }

    @Test
    @DisplayName("測試: 虧損公司淨利率為負")
    void testNegativeNetMargin() {
        // Given - 淨利 -100000, 營收 1000000, 淨利率 = -10%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .netIncome(BigDecimal.valueOf(-100000))
                .revenue(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).containsKey("net_margin");
        BigDecimal netMargin = result.getProfitabilityIndicators().get("net_margin");
        assertThat(netMargin).isEqualByComparingTo(BigDecimal.valueOf(-10.00));
    }

    @Test
    @DisplayName("測試: 淨利率異常高警告")
    void testHighNetMarginWarning() {
        // Given - 淨利率 > 60%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .netIncome(BigDecimal.valueOf(700000))
                .revenue(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).containsKey("net_margin");
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("淨利率異常高");
    }

    @Test
    @DisplayName("測試: 元數據正確")
    void testMetadata() {
        // When
        var metadata = calculator.getMetadata();

        // Then
        assertThat(metadata.getName()).isEqualTo("net_margin");
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
