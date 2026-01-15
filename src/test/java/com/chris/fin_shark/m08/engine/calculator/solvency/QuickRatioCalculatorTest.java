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
 * 速動比率計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("速動比率計算器測試")
class QuickRatioCalculatorTest {

    private QuickRatioCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new QuickRatioCalculator();
    }

    @Test
    @DisplayName("測試: 正常計算速動比率")
    void testNormalCalculation() {
        // Given - 流動資產 500000, 流動負債 300000
        // 估算存貨 = 500000 * 0.3 = 150000
        // 速動資產 = 500000 - 150000 = 350000
        // 速動比率 = 350000 / 300000 = 1.17
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .currentAssets(BigDecimal.valueOf(500000))
                .currentLiabilities(BigDecimal.valueOf(300000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getSolvencyIndicators()).containsKey("quick_ratio");
        BigDecimal quickRatio = result.getSolvencyIndicators().get("quick_ratio");
        assertThat(quickRatio).isEqualByComparingTo(BigDecimal.valueOf(1.17));
    }

    @Test
    @DisplayName("測試: 良好速動比率")
    void testGoodQuickRatio() {
        // Given - 速動比率 > 1
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .currentAssets(BigDecimal.valueOf(600000))
                .currentLiabilities(BigDecimal.valueOf(300000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getSolvencyIndicators()).containsKey("quick_ratio");
        BigDecimal quickRatio = result.getSolvencyIndicators().get("quick_ratio");
        assertThat(quickRatio.compareTo(BigDecimal.ONE)).isGreaterThanOrEqualTo(0);
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
        assertThat(result.getSolvencyIndicators()).doesNotContainKey("quick_ratio");
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
        assertThat(result.getSolvencyIndicators()).doesNotContainKey("quick_ratio");
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
        assertThat(result.getSolvencyIndicators()).doesNotContainKey("quick_ratio");
    }

    @Test
    @DisplayName("測試: 速動比率偏低警告")
    void testLowQuickRatioWarning() {
        // Given - 速動比率 < 0.8
        // 流動資產 300000, 估算存貨 90000, 速動資產 210000
        // 速動比率 = 210000 / 300000 = 0.70
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .currentAssets(BigDecimal.valueOf(300000))
                .currentLiabilities(BigDecimal.valueOf(300000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getSolvencyIndicators()).containsKey("quick_ratio");
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("速動比率偏低");
    }

    @Test
    @DisplayName("測試: 元數據正確")
    void testMetadata() {
        // When
        var metadata = calculator.getMetadata();

        // Then
        assertThat(metadata.getName()).isEqualTo("quick_ratio");
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
