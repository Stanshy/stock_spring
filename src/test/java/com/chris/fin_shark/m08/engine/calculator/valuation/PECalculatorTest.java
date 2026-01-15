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
 * 本益比 (P/E Ratio) 計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("PE 計算器測試")
class PECalculatorTest {

    private PECalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PECalculator();
    }

    @Test
    @DisplayName("測試: 正常計算 P/E")
    void testNormalCalculation() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .stockPrice(BigDecimal.valueOf(500))
                .eps(BigDecimal.valueOf(25))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).containsKey("pe_ratio");
        BigDecimal peRatio = result.getValuationIndicators().get("pe_ratio");
        assertThat(peRatio).isEqualByComparingTo(BigDecimal.valueOf(20.00));
    }

    @Test
    @DisplayName("測試: 缺少股價")
    void testMissingStockPrice() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .eps(BigDecimal.valueOf(25))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).doesNotContainKey("pe_ratio");
    }

    @Test
    @DisplayName("測試: 缺少 EPS")
    void testMissingEps() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .stockPrice(BigDecimal.valueOf(500))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).doesNotContainKey("pe_ratio");
    }

    @Test
    @DisplayName("測試: EPS 為零")
    void testZeroEps() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .stockPrice(BigDecimal.valueOf(500))
                .eps(BigDecimal.ZERO)
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).doesNotContainKey("pe_ratio");
    }

    @Test
    @DisplayName("測試: EPS 為負（虧損公司）")
    void testNegativeEps() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .stockPrice(BigDecimal.valueOf(500))
                .eps(BigDecimal.valueOf(-5))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).doesNotContainKey("pe_ratio");
    }

    @Test
    @DisplayName("測試: P/E 異常高警告")
    void testHighPeWarning() {
        // Given - 股價 1000, EPS 5, P/E = 200
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .stockPrice(BigDecimal.valueOf(1000))
                .eps(BigDecimal.valueOf(5))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).containsKey("pe_ratio");
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("P/E 異常高");
    }

    @Test
    @DisplayName("測試: 元數據正確")
    void testMetadata() {
        // When
        var metadata = calculator.getMetadata();

        // Then
        assertThat(metadata.getName()).isEqualTo("pe_ratio");
        assertThat(metadata.getCategory()).isEqualTo("VALUATION");
        assertThat(metadata.getPriority()).isEqualTo("P0");
    }

    private CalculationResult createResult() {
        return CalculationResult.builder()
                .diagnostics(new Diagnostics())
                .build();
    }
}
