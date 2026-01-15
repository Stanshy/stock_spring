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
 * 營業利益率計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("營業利益率計算器測試")
class OperatingMarginCalculatorTest {

    private OperatingMarginCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new OperatingMarginCalculator();
    }

    @Test
    @DisplayName("測試: 正常計算營業利益率")
    void testNormalCalculation() {
        // Given - 營業利益 200000, 營收 1000000, 營業利益率 = 20%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingIncome(BigDecimal.valueOf(200000))
                .revenue(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).containsKey("operating_margin");
        BigDecimal operatingMargin = result.getProfitabilityIndicators().get("operating_margin");
        assertThat(operatingMargin).isEqualByComparingTo(BigDecimal.valueOf(20.00));
    }

    @Test
    @DisplayName("測試: 缺少營業利益")
    void testMissingOperatingIncome() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .revenue(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).doesNotContainKey("operating_margin");
    }

    @Test
    @DisplayName("測試: 缺少營收")
    void testMissingRevenue() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingIncome(BigDecimal.valueOf(200000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).doesNotContainKey("operating_margin");
    }

    @Test
    @DisplayName("測試: 營收為零")
    void testZeroRevenue() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingIncome(BigDecimal.valueOf(200000))
                .revenue(BigDecimal.ZERO)
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).doesNotContainKey("operating_margin");
    }

    @Test
    @DisplayName("測試: 營業虧損")
    void testNegativeOperatingMargin() {
        // Given - 營業利益 -100000, 營收 1000000, 營業利益率 = -10%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingIncome(BigDecimal.valueOf(-100000))
                .revenue(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).containsKey("operating_margin");
        BigDecimal operatingMargin = result.getProfitabilityIndicators().get("operating_margin");
        assertThat(operatingMargin).isEqualByComparingTo(BigDecimal.valueOf(-10.00));
    }

    @Test
    @DisplayName("測試: 營業利益率異常低警告")
    void testLowOperatingMarginWarning() {
        // Given - 營業利益率 < -30%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingIncome(BigDecimal.valueOf(-400000))
                .revenue(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).containsKey("operating_margin");
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("營業利益率異常低");
    }

    @Test
    @DisplayName("測試: 元數據正確")
    void testMetadata() {
        // When
        var metadata = calculator.getMetadata();

        // Then
        assertThat(metadata.getName()).isEqualTo("operating_margin");
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
