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
 * 企業價值倍數 (EV/EBITDA) 計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("EV/EBITDA 計算器測試")
class EVEBITDACalculatorTest {

    private EVEBITDACalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new EVEBITDACalculator();
    }

    @Test
    @DisplayName("測試: 正常計算 EV/EBITDA")
    void testNormalCalculation() {
        // Given
        // EV = 市值 + 負債 - 現金 = 1000000 + 300000 - 100000 = 1200000
        // EV/EBITDA = 1200000 / 100000 = 12.0
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .marketCap(BigDecimal.valueOf(1000000))
                .totalLiabilities(BigDecimal.valueOf(300000))
                .cashAndEquivalents(BigDecimal.valueOf(100000))
                .ebitda(BigDecimal.valueOf(100000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).containsKey("ev_ebitda");
        assertThat(result.getValuationIndicators()).containsKey("ev");
        BigDecimal evEbitda = result.getValuationIndicators().get("ev_ebitda");
        assertThat(evEbitda).isEqualByComparingTo(BigDecimal.valueOf(12.00));
    }

    @Test
    @DisplayName("測試: 無負債無現金")
    void testNoDebtNoCash() {
        // Given - EV = 市值 = 1000000
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .marketCap(BigDecimal.valueOf(1000000))
                .ebitda(BigDecimal.valueOf(100000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).containsKey("ev_ebitda");
        BigDecimal evEbitda = result.getValuationIndicators().get("ev_ebitda");
        assertThat(evEbitda).isEqualByComparingTo(BigDecimal.valueOf(10.00));
    }

    @Test
    @DisplayName("測試: 缺少市值")
    void testMissingMarketCap() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .ebitda(BigDecimal.valueOf(100000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).doesNotContainKey("ev_ebitda");
    }

    @Test
    @DisplayName("測試: 缺少 EBITDA")
    void testMissingEbitda() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .marketCap(BigDecimal.valueOf(1000000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).doesNotContainKey("ev_ebitda");
    }

    @Test
    @DisplayName("測試: EBITDA 為零")
    void testZeroEbitda() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .marketCap(BigDecimal.valueOf(1000000))
                .ebitda(BigDecimal.ZERO)
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).doesNotContainKey("ev_ebitda");
    }

    @Test
    @DisplayName("測試: EBITDA 為負")
    void testNegativeEbitda() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .marketCap(BigDecimal.valueOf(1000000))
                .ebitda(BigDecimal.valueOf(-50000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).doesNotContainKey("ev_ebitda");
    }

    @Test
    @DisplayName("測試: EV/EBITDA 異常高警告")
    void testHighEvEbitdaWarning() {
        // Given - EV/EBITDA > 30
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .marketCap(BigDecimal.valueOf(1000000))
                .ebitda(BigDecimal.valueOf(25000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).containsKey("ev_ebitda");
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("EV/EBITDA 異常高");
    }

    @Test
    @DisplayName("測試: 負 EV 警告（現金多於市值+負債）")
    void testNegativeEvWarning() {
        // Given - EV = 100000 + 50000 - 200000 = -50000
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .marketCap(BigDecimal.valueOf(100000))
                .totalLiabilities(BigDecimal.valueOf(50000))
                .cashAndEquivalents(BigDecimal.valueOf(200000))
                .ebitda(BigDecimal.valueOf(10000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).containsKey("ev_ebitda");
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("EV/EBITDA 為負");
    }

    @Test
    @DisplayName("測試: 元數據正確")
    void testMetadata() {
        // When
        var metadata = calculator.getMetadata();

        // Then
        assertThat(metadata.getName()).isEqualTo("ev_ebitda");
        assertThat(metadata.getCategory()).isEqualTo("VALUATION");
        assertThat(metadata.getPriority()).isEqualTo("P0");
    }

    private CalculationResult createResult() {
        return CalculationResult.builder()
                .diagnostics(new Diagnostics())
                .build();
    }
}
