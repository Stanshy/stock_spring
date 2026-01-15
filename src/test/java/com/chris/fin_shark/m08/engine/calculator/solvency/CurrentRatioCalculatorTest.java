package com.chris.fin_shark.m08.engine.calculator.solvency;

import com.chris.fin_shark.m08.engine.model.CalculationResult;
import com.chris.fin_shark.m08.engine.model.Diagnostics;
import com.chris.fin_shark.m08.engine.model.FinancialData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 流動比率計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("流動比率計算器測試")
class CurrentRatioCalculatorTest {

    private CurrentRatioCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new CurrentRatioCalculator();
    }

    @Test
    @DisplayName("測試: 正常計算流動比率")
    void testNormalCalculation() {
        // Given - 流動資產 500000, 流動負債 300000, 流動比率 = 1.67
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .currentAssets(BigDecimal.valueOf(500000))
                .currentLiabilities(BigDecimal.valueOf(300000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getSolvencyIndicators()).containsKey("current_ratio");
        BigDecimal currentRatio = result.getSolvencyIndicators().get("current_ratio");
        assertThat(currentRatio).isEqualByComparingTo(BigDecimal.valueOf(1.67));
    }

    @Test
    @DisplayName("測試: 良好流動性")
    void testGoodLiquidity() {
        // Given - 流動比率 = 2.0
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .currentAssets(BigDecimal.valueOf(600000))
                .currentLiabilities(BigDecimal.valueOf(300000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getSolvencyIndicators()).containsKey("current_ratio");
        BigDecimal currentRatio = result.getSolvencyIndicators().get("current_ratio");
        assertThat(currentRatio).isEqualByComparingTo(BigDecimal.valueOf(2.00));
        assertThat(result.getDiagnostics().getWarnings()).isEmpty();
    }

    @Test
    @DisplayName("測試: 缺少流動資產")
    void testMissingCurrentAssets() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .currentLiabilities(BigDecimal.valueOf(300000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getSolvencyIndicators()).doesNotContainKey("current_ratio");
    }

    @Test
    @DisplayName("測試: 缺少流動負債")
    void testMissingCurrentLiabilities() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .currentAssets(BigDecimal.valueOf(500000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getSolvencyIndicators()).doesNotContainKey("current_ratio");
    }

    @Test
    @DisplayName("測試: 流動負債為零")
    void testZeroCurrentLiabilities() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .currentAssets(BigDecimal.valueOf(500000))
                .currentLiabilities(BigDecimal.ZERO)
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getSolvencyIndicators()).doesNotContainKey("current_ratio");
    }

    @Test
    @DisplayName("測試: 流動比率低於1警告")
    void testLowCurrentRatioWarning() {
        // Given - 流動比率 < 1
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .currentAssets(BigDecimal.valueOf(250000))
                .currentLiabilities(BigDecimal.valueOf(300000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getSolvencyIndicators()).containsKey("current_ratio");
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("流動比率低於1");
    }

    @Test
    @DisplayName("測試: 流動比率過低警告")
    void testVeryLowCurrentRatioWarning() {
        // Given - 流動比率 < 0.8
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .currentAssets(BigDecimal.valueOf(200000))
                .currentLiabilities(BigDecimal.valueOf(300000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getSolvencyIndicators()).containsKey("current_ratio");
        assertThat(result.getDiagnostics().getWarnings().size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("測試: 流動比率過高警告")
    void testHighCurrentRatioWarning() {
        // Given - 流動比率 > 5
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .currentAssets(BigDecimal.valueOf(1800000))
                .currentLiabilities(BigDecimal.valueOf(300000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getSolvencyIndicators()).containsKey("current_ratio");
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("流動比率過高");
    }

    @Test
    @DisplayName("測試: 元數據正確")
    void testMetadata() {
        // When
        var metadata = calculator.getMetadata();

        // Then
        assertThat(metadata.getName()).isEqualTo("current_ratio");
        assertThat(metadata.getCategory()).isEqualTo("SOLVENCY");
        assertThat(metadata.getPriority()).isEqualTo("P0");
        assertThat(metadata.getUnit()).isEqualTo("倍");
    }

    private CalculationResult createResult() {
        return CalculationResult.builder()
                .diagnostics(new Diagnostics())
                .build();
    }
}
