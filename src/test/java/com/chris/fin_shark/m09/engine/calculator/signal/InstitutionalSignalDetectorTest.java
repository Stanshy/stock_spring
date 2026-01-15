package com.chris.fin_shark.m09.engine.calculator.signal;

import com.chris.fin_shark.m09.engine.model.ChipSeries;
import com.chris.fin_shark.m09.engine.model.ChipSignal;
import com.chris.fin_shark.m09.enums.ChipCategory;
import com.chris.fin_shark.m09.enums.SignalSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 法人籌碼訊號偵測器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("法人籌碼訊號偵測器測試")
class InstitutionalSignalDetectorTest {

    private InstitutionalSignalDetector detector;

    @BeforeEach
    void setUp() {
        detector = new InstitutionalSignalDetector();
        System.out.println("\n========================================");
        System.out.println("  初始化 法人籌碼訊號偵測器測試");
        System.out.println("========================================\n");
    }

    @Test
    @DisplayName("測試: 基本資訊驗證")
    void testMetadata() {
        System.out.println("  測試: 基本資訊驗證");

        assertThat(detector.getName()).isEqualTo("INSTITUTIONAL_SIGNAL");
        assertThat(detector.getCategory()).isEqualTo(ChipCategory.SIGNAL);
        assertThat(detector.getMetadata().getNameZh()).isEqualTo("法人籌碼訊號偵測");

        System.out.println("  測試通過: 基本資訊正確");
    }

    @Test
    @DisplayName("測試: CHIP_SIG_001 外資大買訊號")
    void testForeignLargeBuy_Signal() {
        System.out.println("  測試: CHIP_SIG_001 外資大買訊號");

        // Given - 20 天穩定小量，最後一天大買（超過 2 個標準差）
        long[] foreignNetData = new long[20];
        // 前 19 天小量波動
        for (int i = 0; i < 19; i++) {
            foreignNetData[i] = (i % 2 == 0) ? 100000 : -50000;
        }
        // 最後一天大買（超過平均 + 2*標準差）
        foreignNetData[19] = 5000000;

        long[] trustNetData = new long[20];
        long[] dealerNetData = new long[20];

        System.out.println("  輸入資料:");
        System.out.println("    - 資料天數: " + foreignNetData.length);
        System.out.println("    - 前 19 天平均外資: 小量波動");
        System.out.println("    - 最後一天外資: " + foreignNetData[19] + " (異常大買)");

        ChipSeries series = ChipSeries.createTestInstitutional("2330", foreignNetData, trustNetData, dealerNetData);

        // When
        List<ChipSignal> signals = detector.detect(series, Map.of());

        // Then
        System.out.println("\n  偵測到訊號數: " + signals.size());
        signals.forEach(s -> System.out.println("    - " + s.getSignalCode() + ": " + s.getSignalName()));

        // 應該偵測到 CHIP_SIG_001 外資大買
        assertThat(signals).anyMatch(s -> s.getSignalCode().equals("CHIP_SIG_001"));

        ChipSignal foreignLargeBuySignal = signals.stream()
                .filter(s -> s.getSignalCode().equals("CHIP_SIG_001"))
                .findFirst()
                .orElse(null);

        assertThat(foreignLargeBuySignal).isNotNull();
        assertThat(foreignLargeBuySignal.getSignalName()).isEqualTo("外資大買");
        assertThat(foreignLargeBuySignal.getSeverity()).isEqualTo(SignalSeverity.HIGH);

        System.out.println("\n  測試通過: 外資大買訊號偵測正確");
    }

    @Test
    @DisplayName("測試: CHIP_SIG_003 外資連續買超訊號")
    void testForeignContinuousBuy_Signal() {
        System.out.println("  測試: CHIP_SIG_003 外資連續買超訊號");

        // Given - 20 天，最後 6 天連續買超（>= 5 天閾值）
        long[] foreignNetData = new long[20];
        // 前 14 天混合
        for (int i = 0; i < 14; i++) {
            foreignNetData[i] = (i % 3 == 0) ? 100000 : -50000;
        }
        // 最後 6 天連續買超
        foreignNetData[14] = 200000;
        foreignNetData[15] = 300000;
        foreignNetData[16] = 150000;
        foreignNetData[17] = 400000;
        foreignNetData[18] = 250000;
        foreignNetData[19] = 500000;

        long[] trustNetData = new long[20];
        long[] dealerNetData = new long[20];

        System.out.println("  輸入資料:");
        System.out.println("    - 最後 6 天: 連續買超");

        ChipSeries series = ChipSeries.createTestInstitutional("2330", foreignNetData, trustNetData, dealerNetData);

        // When
        List<ChipSignal> signals = detector.detect(series, Map.of());

        // Then
        System.out.println("\n  偵測到訊號數: " + signals.size());
        signals.forEach(s -> System.out.println("    - " + s.getSignalCode() + ": " + s.getSignalName()));

        // 應該偵測到 CHIP_SIG_003 外資連續買超
        assertThat(signals).anyMatch(s -> s.getSignalCode().equals("CHIP_SIG_003"));

        ChipSignal continuousBuySignal = signals.stream()
                .filter(s -> s.getSignalCode().equals("CHIP_SIG_003"))
                .findFirst()
                .orElse(null);

        assertThat(continuousBuySignal).isNotNull();
        assertThat(continuousBuySignal.getSignalName()).isEqualTo("外資連續買超");
        assertThat(continuousBuySignal.getSeverity()).isEqualTo(SignalSeverity.MEDIUM);

        System.out.println("\n  測試通過: 外資連續買超訊號偵測正確");
    }

    @Test
    @DisplayName("測試: CHIP_SIG_007 三大法人同買訊號")
    void testInstitutionalAllBuy_Signal() {
        System.out.println("  測試: CHIP_SIG_007 三大法人同買訊號");

        // Given - 最後一天三大法人都買超
        long[] foreignNetData = new long[20];
        long[] trustNetData = new long[20];
        long[] dealerNetData = new long[20];

        // 設定一些歷史資料
        for (int i = 0; i < 20; i++) {
            foreignNetData[i] = 100000;
            trustNetData[i] = 50000;
            dealerNetData[i] = 30000;
        }

        // 最後一天三大法人都買超
        foreignNetData[19] = 1000000;  // 外資買超
        trustNetData[19] = 500000;     // 投信買超
        dealerNetData[19] = 200000;    // 自營買超

        System.out.println("  輸入資料:");
        System.out.println("    - 外資最後一天: " + foreignNetData[19] + " (買超)");
        System.out.println("    - 投信最後一天: " + trustNetData[19] + " (買超)");
        System.out.println("    - 自營最後一天: " + dealerNetData[19] + " (買超)");

        ChipSeries series = ChipSeries.createTestInstitutional("2330", foreignNetData, trustNetData, dealerNetData);

        // When
        List<ChipSignal> signals = detector.detect(series, Map.of());

        // Then
        System.out.println("\n  偵測到訊號數: " + signals.size());
        signals.forEach(s -> System.out.println("    - " + s.getSignalCode() + ": " + s.getSignalName()));

        // 應該偵測到 CHIP_SIG_007 三大法人同買
        assertThat(signals).anyMatch(s -> s.getSignalCode().equals("CHIP_SIG_007"));

        ChipSignal allBuySignal = signals.stream()
                .filter(s -> s.getSignalCode().equals("CHIP_SIG_007"))
                .findFirst()
                .orElse(null);

        assertThat(allBuySignal).isNotNull();
        assertThat(allBuySignal.getSignalName()).isEqualTo("三大法人同買");
        assertThat(allBuySignal.getSeverity()).isEqualTo(SignalSeverity.MEDIUM);

        System.out.println("\n  測試通過: 三大法人同買訊號偵測正確");
    }

    @Test
    @DisplayName("測試: CHIP_SIG_008 三大法人同賣訊號")
    void testInstitutionalAllSell_Signal() {
        System.out.println("  測試: CHIP_SIG_008 三大法人同賣訊號");

        // Given - 最後一天三大法人都賣超
        long[] foreignNetData = new long[20];
        long[] trustNetData = new long[20];
        long[] dealerNetData = new long[20];

        // 設定一些歷史資料
        for (int i = 0; i < 20; i++) {
            foreignNetData[i] = -100000;
            trustNetData[i] = -50000;
            dealerNetData[i] = -30000;
        }

        // 最後一天三大法人都賣超
        foreignNetData[19] = -1000000;  // 外資賣超
        trustNetData[19] = -500000;     // 投信賣超
        dealerNetData[19] = -200000;    // 自營賣超

        System.out.println("  輸入資料:");
        System.out.println("    - 外資最後一天: " + foreignNetData[19] + " (賣超)");
        System.out.println("    - 投信最後一天: " + trustNetData[19] + " (賣超)");
        System.out.println("    - 自營最後一天: " + dealerNetData[19] + " (賣超)");

        ChipSeries series = ChipSeries.createTestInstitutional("2330", foreignNetData, trustNetData, dealerNetData);

        // When
        List<ChipSignal> signals = detector.detect(series, Map.of());

        // Then
        System.out.println("\n  偵測到訊號數: " + signals.size());
        signals.forEach(s -> System.out.println("    - " + s.getSignalCode() + ": " + s.getSignalName()));

        // 應該偵測到 CHIP_SIG_008 三大法人同賣
        assertThat(signals).anyMatch(s -> s.getSignalCode().equals("CHIP_SIG_008"));

        ChipSignal allSellSignal = signals.stream()
                .filter(s -> s.getSignalCode().equals("CHIP_SIG_008"))
                .findFirst()
                .orElse(null);

        assertThat(allSellSignal).isNotNull();
        assertThat(allSellSignal.getSignalName()).isEqualTo("三大法人同賣");
        assertThat(allSellSignal.getSeverity()).isEqualTo(SignalSeverity.HIGH);

        System.out.println("\n  測試通過: 三大法人同賣訊號偵測正確");
    }

    @Test
    @DisplayName("測試: 資料不足時不產生訊號")
    void testInsufficientData_NoSignal() {
        System.out.println("  測試: 資料不足時不產生訊號");

        // Given - 只有 10 天資料（需要 20 天）
        long[] foreignNetData = new long[10];
        long[] trustNetData = new long[10];
        long[] dealerNetData = new long[10];

        for (int i = 0; i < 10; i++) {
            foreignNetData[i] = 100000;
            trustNetData[i] = 50000;
            dealerNetData[i] = 30000;
        }

        System.out.println("  輸入資料:");
        System.out.println("    - 資料天數: " + foreignNetData.length + " (需要 20 天)");

        ChipSeries series = ChipSeries.createTestInstitutional("2330", foreignNetData, trustNetData, dealerNetData);

        // When
        List<ChipSignal> signals = detector.detect(series, Map.of());

        // Then
        System.out.println("\n  偵測到訊號數: " + signals.size());

        assertThat(signals).isEmpty();

        System.out.println("\n  測試通過: 資料不足時正確不產生訊號");
    }

    @Test
    @DisplayName("測試: calculate 方法回傳格式")
    void testCalculateMethod() {
        System.out.println("  測試: calculate 方法回傳格式");

        // Given
        long[] foreignNetData = new long[20];
        long[] trustNetData = new long[20];
        long[] dealerNetData = new long[20];

        for (int i = 0; i < 20; i++) {
            foreignNetData[i] = 100000;
            trustNetData[i] = 50000;
            dealerNetData[i] = 30000;
        }

        ChipSeries series = ChipSeries.createTestInstitutional("2330", foreignNetData, trustNetData, dealerNetData);

        // When
        Map<String, Object> result = detector.calculate(series, Map.of());

        // Then
        System.out.println("  計算結果:");
        System.out.println("    - signals: " + result.get("signals"));
        System.out.println("    - signal_count: " + result.get("signal_count"));

        assertThat(result).containsKey("signals");
        assertThat(result).containsKey("signal_count");
        assertThat(result.get("signals")).isInstanceOf(List.class);

        System.out.println("\n  測試通過: calculate 方法回傳格式正確");
    }
}
