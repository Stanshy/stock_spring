package com.chris.fin_shark.m08.engine.calculator.cashflow;

import com.chris.fin_shark.m08.engine.model.CalculationResult;
import com.chris.fin_shark.m08.engine.model.Diagnostics;
import com.chris.fin_shark.m08.engine.model.FinancialData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 營運現金流比率計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("OCF Ratio 計算器測試")
class OCFRatioCalculatorTest {

    private OCFRatioCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new OCFRatioCalculator();
    }

    @Test
    @DisplayName("測試: 正常計算營運現金流比率")
    void testNormalCalculation() {
        // Given - 營運現金流 300000, 流動負債 200000, OCF Ratio = 1.5
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingCashFlow(BigDecimal.valueOf(300000))
                .currentLiabilities(BigDecimal.valueOf(200000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getCashFlowIndicators()).containsKey("ocf_ratio");
        BigDecimal ocfRatio = result.getCashFlowIndicators().get("ocf_ratio");
        assertThat(ocfRatio).isEqualByComparingTo(BigDecimal.valueOf(1.50));
    }

    @Test
    @DisplayName("測試: 高營運現金流比率")
    void testHighOcfRatio() {
        // Given - OCF Ratio > 1
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingCashFlow(BigDecimal.valueOf(500000))
                .currentLiabilities(BigDecimal.valueOf(200000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getCashFlowIndicators()).containsKey("ocf_ratio");
        BigDecimal ocfRatio = result.getCashFlowIndicators().get("ocf_ratio");
        assertThat(ocfRatio).isEqualByComparingTo(BigDecimal.valueOf(2.50));
        assertThat(result.getDiagnostics().getWarnings()).isEmpty();
    }

    @Test
    @DisplayName("測試: 缺少營運現金流")
    void testMissingOperatingCashFlow() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .currentLiabilities(BigDecimal.valueOf(200000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getCashFlowIndicators()).doesNotContainKey("ocf_ratio");
    }

    @Test
    @DisplayName("測試: 缺少流動負債")
    void testMissingCurrentLiabilities() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingCashFlow(BigDecimal.valueOf(300000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getCashFlowIndicators()).doesNotContainKey("ocf_ratio");
    }

    @Test
    @DisplayName("測試: 流動負債為零")
    void testZeroCurrentLiabilities() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingCashFlow(BigDecimal.valueOf(300000))
                .currentLiabilities(BigDecimal.ZERO)
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getCashFlowIndicators()).doesNotContainKey("ocf_ratio");
    }

    @Test
    @DisplayName("測試: 流動負債為負（異常）")
    void testNegativeCurrentLiabilities() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingCashFlow(BigDecimal.valueOf(300000))
                .currentLiabilities(BigDecimal.valueOf(-100000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getCashFlowIndicators()).doesNotContainKey("ocf_ratio");
    }

    @Test
    @DisplayName("測試: OCF Ratio 小於 1（記錄）")
    void testOcfRatioBelowOne() {
        // Given - OCF Ratio < 1
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingCashFlow(BigDecimal.valueOf(150000))
                .currentLiabilities(BigDecimal.valueOf(200000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getCashFlowIndicators()).containsKey("ocf_ratio");
        BigDecimal ocfRatio = result.getCashFlowIndicators().get("ocf_ratio");
        assertThat(ocfRatio).isEqualByComparingTo(BigDecimal.valueOf(0.75));
    }

    @Test
    @DisplayName("測試: OCF Ratio 為負警告（現金流出）")
    void testNegativeOcfRatioWarning() {
        // Given - 營運現金流為負
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingCashFlow(BigDecimal.valueOf(-100000))
                .currentLiabilities(BigDecimal.valueOf(200000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getCashFlowIndicators()).containsKey("ocf_ratio");
        BigDecimal ocfRatio = result.getCashFlowIndicators().get("ocf_ratio");
        assertThat(ocfRatio).isEqualByComparingTo(BigDecimal.valueOf(-0.50));
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("營運現金流為負");
    }

    @Test
    @DisplayName("測試: 元數據正確")
    void testMetadata() {
        // When
        var metadata = calculator.getMetadata();

        // Then
        assertThat(metadata.getName()).isEqualTo("ocf_ratio");
        assertThat(metadata.getCategory()).isEqualTo("CASH_FLOW");
        assertThat(metadata.getPriority()).isEqualTo("P0");
        assertThat(metadata.getUnit()).isEqualTo("倍");
    }

    private CalculationResult createResult() {
        return CalculationResult.builder()
                .diagnostics(new Diagnostics())
                .build();
    }
}
