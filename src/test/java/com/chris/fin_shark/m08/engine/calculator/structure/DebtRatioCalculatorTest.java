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
 * 負債比率計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("負債比率計算器測試")
class DebtRatioCalculatorTest {

    private DebtRatioCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new DebtRatioCalculator();
    }

    @Test
    @DisplayName("測試: 正常計算負債比率")
    void testNormalCalculation() {
        // Given - 負債 400000, 資產 1000000, 負債比率 = 40%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalLiabilities(BigDecimal.valueOf(400000))
                .totalAssets(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getFinancialStructureIndicators()).containsKey("debt_ratio");
        BigDecimal debtRatio = result.getFinancialStructureIndicators().get("debt_ratio");
        assertThat(debtRatio).isEqualByComparingTo(BigDecimal.valueOf(40.00));
    }

    @Test
    @DisplayName("測試: 低負債公司")
    void testLowDebtCompany() {
        // Given - 負債 100000, 資產 1000000, 負債比率 = 10%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalLiabilities(BigDecimal.valueOf(100000))
                .totalAssets(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getFinancialStructureIndicators()).containsKey("debt_ratio");
        BigDecimal debtRatio = result.getFinancialStructureIndicators().get("debt_ratio");
        assertThat(debtRatio).isEqualByComparingTo(BigDecimal.valueOf(10.00));
        assertThat(result.getDiagnostics().getWarnings()).isEmpty();
    }

    @Test
    @DisplayName("測試: 缺少負債")
    void testMissingLiabilities() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalAssets(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getFinancialStructureIndicators()).doesNotContainKey("debt_ratio");
    }

    @Test
    @DisplayName("測試: 缺少資產")
    void testMissingAssets() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalLiabilities(BigDecimal.valueOf(400000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getFinancialStructureIndicators()).doesNotContainKey("debt_ratio");
    }

    @Test
    @DisplayName("測試: 資產為零")
    void testZeroAssets() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalLiabilities(BigDecimal.valueOf(400000))
                .totalAssets(BigDecimal.ZERO)
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getFinancialStructureIndicators()).doesNotContainKey("debt_ratio");
    }

    @Test
    @DisplayName("測試: 負債比率偏高警告")
    void testHighDebtRatioWarning() {
        // Given - 負債比率 > 70%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalLiabilities(BigDecimal.valueOf(750000))
                .totalAssets(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getFinancialStructureIndicators()).containsKey("debt_ratio");
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("負債比率偏高");
    }

    @Test
    @DisplayName("測試: 負債比率過高警告")
    void testVeryHighDebtRatioWarning() {
        // Given - 負債比率 > 90%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalLiabilities(BigDecimal.valueOf(950000))
                .totalAssets(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getFinancialStructureIndicators()).containsKey("debt_ratio");
        assertThat(result.getDiagnostics().getWarnings().size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("測試: 元數據正確")
    void testMetadata() {
        // When
        var metadata = calculator.getMetadata();

        // Then
        assertThat(metadata.getName()).isEqualTo("debt_ratio");
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
