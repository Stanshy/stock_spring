package com.chris.fin_shark.m08.engine.calculator.valuation;

import com.chris.fin_shark.m08.engine.model.CalculationResult;
import com.chris.fin_shark.m08.engine.model.Diagnostics;
import com.chris.fin_shark.m08.engine.model.FinancialData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 股價營收比 (P/S Ratio) 計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("PS 計算器測試")
class PSCalculatorTest {

    private PSCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PSCalculator();
    }

    @Test
    @DisplayName("測試: 正常計算 P/S")
    void testNormalCalculation() {
        // Given - 市值 1000000千元, 營收 500000千元, P/S = 2.0
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .marketCap(BigDecimal.valueOf(1000000))
                .revenue(BigDecimal.valueOf(500000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).containsKey("ps_ratio");
        BigDecimal psRatio = result.getValuationIndicators().get("ps_ratio");
        assertThat(psRatio).isEqualByComparingTo(BigDecimal.valueOf(2.00));
    }

    @Test
    @DisplayName("測試: 缺少市值")
    void testMissingMarketCap() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .revenue(BigDecimal.valueOf(500000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).doesNotContainKey("ps_ratio");
    }

    @Test
    @DisplayName("測試: 缺少營收")
    void testMissingRevenue() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .marketCap(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).doesNotContainKey("ps_ratio");
    }

    @Test
    @DisplayName("測試: 營收為零")
    void testZeroRevenue() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .marketCap(BigDecimal.valueOf(1000000))
                .revenue(BigDecimal.ZERO)
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).doesNotContainKey("ps_ratio");
    }

    @Test
    @DisplayName("測試: 營收為負")
    void testNegativeRevenue() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .marketCap(BigDecimal.valueOf(1000000))
                .revenue(BigDecimal.valueOf(-100000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).doesNotContainKey("ps_ratio");
    }

    @Test
    @DisplayName("測試: P/S 異常高警告")
    void testHighPsWarning() {
        // Given - 市值 500000, 營收 10000, P/S = 50
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .marketCap(BigDecimal.valueOf(500000))
                .revenue(BigDecimal.valueOf(10000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).containsKey("ps_ratio");
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("P/S 異常高");
    }

    @Test
    @DisplayName("測試: 元數據正確")
    void testMetadata() {
        // When
        var metadata = calculator.getMetadata();

        // Then
        assertThat(metadata.getName()).isEqualTo("ps_ratio");
        assertThat(metadata.getCategory()).isEqualTo("VALUATION");
        assertThat(metadata.getPriority()).isEqualTo("P0");
    }

    private CalculationResult createResult() {
        return CalculationResult.builder()
                .diagnostics(new Diagnostics())
                .build();
    }
}
