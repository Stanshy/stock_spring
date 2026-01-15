package com.chris.fin_shark.m08.engine.calculator.structure;

import com.chris.fin_shark.m08.engine.model.CalculationResult;
import com.chris.fin_shark.m08.engine.model.Diagnostics;
import com.chris.fin_shark.m08.engine.model.FinancialData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 權益比率計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("權益比率計算器測試")
class EquityRatioCalculatorTest {

    private EquityRatioCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new EquityRatioCalculator();
    }

    @Test
    @DisplayName("測試: 正常計算權益比率")
    void testNormalCalculation() {
        // Given - 權益 600000, 資產 1000000, 權益比率 = 60%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalEquity(BigDecimal.valueOf(600000))
                .totalAssets(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getFinancialStructureIndicators()).containsKey("equity_ratio");
        BigDecimal equityRatio = result.getFinancialStructureIndicators().get("equity_ratio");
        assertThat(equityRatio).isEqualByComparingTo(BigDecimal.valueOf(60.00));
    }

    @Test
    @DisplayName("測試: 高權益比率")
    void testHighEquityRatio() {
        // Given - 權益 800000, 資產 1000000, 權益比率 = 80%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalEquity(BigDecimal.valueOf(800000))
                .totalAssets(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getFinancialStructureIndicators()).containsKey("equity_ratio");
        BigDecimal equityRatio = result.getFinancialStructureIndicators().get("equity_ratio");
        assertThat(equityRatio).isEqualByComparingTo(BigDecimal.valueOf(80.00));
        assertThat(result.getDiagnostics().getWarnings()).isEmpty();
    }

    @Test
    @DisplayName("測試: 缺少權益")
    void testMissingEquity() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalAssets(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getFinancialStructureIndicators()).doesNotContainKey("equity_ratio");
    }

    @Test
    @DisplayName("測試: 缺少資產")
    void testMissingAssets() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalEquity(BigDecimal.valueOf(600000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getFinancialStructureIndicators()).doesNotContainKey("equity_ratio");
    }

    @Test
    @DisplayName("測試: 資產為零")
    void testZeroAssets() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalEquity(BigDecimal.valueOf(600000))
                .totalAssets(BigDecimal.ZERO)
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getFinancialStructureIndicators()).doesNotContainKey("equity_ratio");
    }

    @Test
    @DisplayName("測試: 權益比率偏低警告")
    void testLowEquityRatioWarning() {
        // Given - 權益比率 < 30%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalEquity(BigDecimal.valueOf(200000))
                .totalAssets(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getFinancialStructureIndicators()).containsKey("equity_ratio");
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("權益比率偏低");
    }

    @Test
    @DisplayName("測試: 權益比率為負（資不抵債）")
    void testNegativeEquityRatioWarning() {
        // Given - 權益為負
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalEquity(BigDecimal.valueOf(-100000))
                .totalAssets(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getFinancialStructureIndicators()).containsKey("equity_ratio");
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().stream()
                .anyMatch(w -> w.contains("權益比率為負"))).isTrue();
    }

    @Test
    @DisplayName("測試: 元數據正確")
    void testMetadata() {
        // When
        var metadata = calculator.getMetadata();

        // Then
        assertThat(metadata.getName()).isEqualTo("equity_ratio");
        assertThat(metadata.getCategory()).isEqualTo("FINANCIAL_STRUCTURE");
        assertThat(metadata.getPriority()).isEqualTo("P0");
        assertThat(metadata.getUnit()).isEqualTo("%");
    }

    private CalculationResult createResult() {
        return CalculationResult.builder()
                .diagnostics(new Diagnostics())
                .build();
    }
}
