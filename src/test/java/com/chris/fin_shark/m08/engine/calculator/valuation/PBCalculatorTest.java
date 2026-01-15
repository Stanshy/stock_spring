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
 * 股價淨值比 (P/B Ratio) 計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("PB 計算器測試")
class PBCalculatorTest {

    private PBCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PBCalculator();
    }

    @Test
    @DisplayName("測試: 正常計算 P/B")
    void testNormalCalculation() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .stockPrice(BigDecimal.valueOf(500))
                .bookValuePerShare(BigDecimal.valueOf(200))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).containsKey("pb_ratio");
        BigDecimal pbRatio = result.getValuationIndicators().get("pb_ratio");
        assertThat(pbRatio).isEqualByComparingTo(BigDecimal.valueOf(2.50));
    }

    @Test
    @DisplayName("測試: 缺少股價")
    void testMissingStockPrice() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .bookValuePerShare(BigDecimal.valueOf(200))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).doesNotContainKey("pb_ratio");
    }

    @Test
    @DisplayName("測試: 缺少每股淨值")
    void testMissingBookValue() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .stockPrice(BigDecimal.valueOf(500))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).doesNotContainKey("pb_ratio");
    }

    @Test
    @DisplayName("測試: 每股淨值為零")
    void testZeroBookValue() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .stockPrice(BigDecimal.valueOf(500))
                .bookValuePerShare(BigDecimal.ZERO)
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).doesNotContainKey("pb_ratio");
    }

    @Test
    @DisplayName("測試: 每股淨值為負")
    void testNegativeBookValue() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .stockPrice(BigDecimal.valueOf(500))
                .bookValuePerShare(BigDecimal.valueOf(-50))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).doesNotContainKey("pb_ratio");
    }

    @Test
    @DisplayName("測試: P/B 異常高警告")
    void testHighPbWarning() {
        // Given - 股價 500, 淨值 30, P/B = 16.67
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .stockPrice(BigDecimal.valueOf(500))
                .bookValuePerShare(BigDecimal.valueOf(30))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).containsKey("pb_ratio");
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("P/B 異常高");
    }

    @Test
    @DisplayName("測試: 元數據正確")
    void testMetadata() {
        // When
        var metadata = calculator.getMetadata();

        // Then
        assertThat(metadata.getName()).isEqualTo("pb_ratio");
        assertThat(metadata.getCategory()).isEqualTo("VALUATION");
        assertThat(metadata.getPriority()).isEqualTo("P0");
    }

    private CalculationResult createResult() {
        return CalculationResult.builder()
                .diagnostics(new Diagnostics())
                .build();
    }
}
