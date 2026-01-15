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
 * 自由現金流 (FCF) 計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("FCF 計算器測試")
class FCFCalculatorTest {

    private FCFCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new FCFCalculator();
    }

    @Test
    @DisplayName("測試: 正常計算 FCF")
    void testNormalCalculation() {
        // Given - 營運現金流 200000, 資本支出 50000, FCF = 150000
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingCashFlow(BigDecimal.valueOf(200000))
                .capitalExpenditure(BigDecimal.valueOf(50000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getCashFlowIndicators()).containsKey("free_cash_flow");
        BigDecimal fcf = result.getCashFlowIndicators().get("free_cash_flow");
        assertThat(fcf).isEqualByComparingTo(BigDecimal.valueOf(150000));
    }

    @Test
    @DisplayName("測試: 高資本支出公司")
    void testHighCapexCompany() {
        // Given - 營運現金流 200000, 資本支出 180000, FCF = 20000
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingCashFlow(BigDecimal.valueOf(200000))
                .capitalExpenditure(BigDecimal.valueOf(180000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getCashFlowIndicators()).containsKey("free_cash_flow");
        BigDecimal fcf = result.getCashFlowIndicators().get("free_cash_flow");
        assertThat(fcf).isEqualByComparingTo(BigDecimal.valueOf(20000));
    }

    @Test
    @DisplayName("測試: 缺少營運現金流")
    void testMissingOperatingCashFlow() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .capitalExpenditure(BigDecimal.valueOf(50000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getCashFlowIndicators()).doesNotContainKey("free_cash_flow");
    }

    @Test
    @DisplayName("測試: 缺少資本支出")
    void testMissingCapitalExpenditure() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingCashFlow(BigDecimal.valueOf(200000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getCashFlowIndicators()).doesNotContainKey("free_cash_flow");
    }

    @Test
    @DisplayName("測試: FCF 為負警告（燒錢）")
    void testNegativeFcfWarning() {
        // Given - 營運現金流 100000, 資本支出 150000, FCF = -50000
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingCashFlow(BigDecimal.valueOf(100000))
                .capitalExpenditure(BigDecimal.valueOf(150000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getCashFlowIndicators()).containsKey("free_cash_flow");
        BigDecimal fcf = result.getCashFlowIndicators().get("free_cash_flow");
        assertThat(fcf).isEqualByComparingTo(BigDecimal.valueOf(-50000));
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("自由現金流為負");
    }

    @Test
    @DisplayName("測試: 營運現金流為負")
    void testNegativeOperatingCashFlow() {
        // Given - 營運現金流 -50000, 資本支出 30000, FCF = -80000
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingCashFlow(BigDecimal.valueOf(-50000))
                .capitalExpenditure(BigDecimal.valueOf(30000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getCashFlowIndicators()).containsKey("free_cash_flow");
        BigDecimal fcf = result.getCashFlowIndicators().get("free_cash_flow");
        assertThat(fcf).isEqualByComparingTo(BigDecimal.valueOf(-80000));
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
    }

    @Test
    @DisplayName("測試: 元數據正確")
    void testMetadata() {
        // When
        var metadata = calculator.getMetadata();

        // Then
        assertThat(metadata.getName()).isEqualTo("free_cash_flow");
        assertThat(metadata.getCategory()).isEqualTo("CASH_FLOW");
        assertThat(metadata.getPriority()).isEqualTo("P0");
        assertThat(metadata.getUnit()).isEqualTo("千元");
    }

    private CalculationResult createResult() {
        return CalculationResult.builder()
                .diagnostics(new Diagnostics())
                .build();
    }
}
