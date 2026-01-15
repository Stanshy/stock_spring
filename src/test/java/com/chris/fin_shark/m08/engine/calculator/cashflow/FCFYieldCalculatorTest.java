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
 * 自由現金流殖利率 (FCF Yield) 計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("FCF Yield 計算器測試")
class FCFYieldCalculatorTest {

    private FCFYieldCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new FCFYieldCalculator();
    }

    @Test
    @DisplayName("測試: 正常計算 FCF Yield")
    void testNormalCalculation() {
        // Given - FCF = 200000 - 50000 = 150000, 市值 1000000
        // FCF Yield = 150000 / 1000000 * 100 = 15%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingCashFlow(BigDecimal.valueOf(200000))
                .capitalExpenditure(BigDecimal.valueOf(50000))
                .marketCap(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getCashFlowIndicators()).containsKey("fcf_yield");
        BigDecimal fcfYield = result.getCashFlowIndicators().get("fcf_yield");
        assertThat(fcfYield).isEqualByComparingTo(BigDecimal.valueOf(15.00));
    }

    @Test
    @DisplayName("測試: 高 FCF Yield")
    void testHighFcfYield() {
        // Given - FCF Yield > 10%
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingCashFlow(BigDecimal.valueOf(150000))
                .capitalExpenditure(BigDecimal.valueOf(30000))
                .marketCap(BigDecimal.valueOf(800000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getCashFlowIndicators()).containsKey("fcf_yield");
        BigDecimal fcfYield = result.getCashFlowIndicators().get("fcf_yield");
        assertThat(fcfYield.compareTo(BigDecimal.valueOf(10))).isGreaterThan(0);
    }

    @Test
    @DisplayName("測試: 缺少市值")
    void testMissingMarketCap() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingCashFlow(BigDecimal.valueOf(200000))
                .capitalExpenditure(BigDecimal.valueOf(50000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getCashFlowIndicators()).doesNotContainKey("fcf_yield");
    }

    @Test
    @DisplayName("測試: 缺少營運現金流")
    void testMissingOperatingCashFlow() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .capitalExpenditure(BigDecimal.valueOf(50000))
                .marketCap(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getCashFlowIndicators()).doesNotContainKey("fcf_yield");
    }

    @Test
    @DisplayName("測試: 缺少資本支出")
    void testMissingCapitalExpenditure() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingCashFlow(BigDecimal.valueOf(200000))
                .marketCap(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getCashFlowIndicators()).doesNotContainKey("fcf_yield");
    }

    @Test
    @DisplayName("測試: 市值為零")
    void testZeroMarketCap() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingCashFlow(BigDecimal.valueOf(200000))
                .capitalExpenditure(BigDecimal.valueOf(50000))
                .marketCap(BigDecimal.ZERO)
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getCashFlowIndicators()).doesNotContainKey("fcf_yield");
    }

    @Test
    @DisplayName("測試: FCF Yield 為負警告")
    void testNegativeFcfYieldWarning() {
        // Given - FCF 為負
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingCashFlow(BigDecimal.valueOf(100000))
                .capitalExpenditure(BigDecimal.valueOf(150000))
                .marketCap(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getCashFlowIndicators()).containsKey("fcf_yield");
        BigDecimal fcfYield = result.getCashFlowIndicators().get("fcf_yield");
        assertThat(fcfYield.compareTo(BigDecimal.ZERO)).isLessThan(0);
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("FCF Yield 為負");
    }

    @Test
    @DisplayName("測試: 元數據正確")
    void testMetadata() {
        // When
        var metadata = calculator.getMetadata();

        // Then
        assertThat(metadata.getName()).isEqualTo("fcf_yield");
        assertThat(metadata.getCategory()).isEqualTo("CASH_FLOW");
        assertThat(metadata.getPriority()).isEqualTo("P0");
        assertThat(metadata.getUnit()).isEqualTo("%");
    }

    private CalculationResult createResult() {
        return CalculationResult.builder()
                .diagnostics(new Diagnostics())
                .build();
    }
}
