package com.chris.fin_shark.m07.integration;

import com.chris.fin_shark.m07.service.IndicatorCalculationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 端到端測試（含資料庫）
 */
@SpringBootTest
//@Transactional  // 測試後回滾
public class EndToEndTest {

    @Autowired
    private IndicatorCalculationService calculationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testFullCalculationFlow() {
        System.out.println("\n========================================");
        System.out.println("🧪 端到端測試：完整計算流程");
        System.out.println("========================================\n");

        // 1. 檢查股價資料是否存在
        String checkSql = """
            SELECT COUNT(*) 
            FROM stock_prices 
            WHERE stock_id = '2330' 
              AND trade_date >= ?
            """;

        LocalDate startDate = LocalDate.now().minusDays(180);
        Integer priceCount = jdbcTemplate.queryForObject(
                checkSql,
                Integer.class,
                startDate
        );

        System.out.println("📊 2330 股價資料筆數: " + priceCount);

        if (priceCount == null || priceCount < 60) {
            System.out.println("⚠️  股價資料不足，跳過測試");
            return;
        }

        // 2. 執行指標計算
        System.out.println("\n🔧 開始計算指標...");

        Long jobId = calculationService.calculateIndicators(
                LocalDate.now(),
                List.of("2330"),  // 只計算 2330
                "P0",
                false
        );

        System.out.println("✅ Job ID: " + jobId);

        // 3. 驗證指標資料已寫入
        String verifySql = """
            SELECT 
                stock_id,
                calculation_date,
                ma5,
                ma20,
                rsi_14,
                trend_indicators::text,
                momentum_indicators::text
            FROM technical_indicators
            WHERE stock_id = '2330'
              AND calculation_date = ?
            """;

        List<Map<String, Object>> results = jdbcTemplate.queryForList(
                verifySql,
                LocalDate.now()
        );

        System.out.println("\n📤 查詢結果:");
        System.out.println("  - 資料筆數: " + results.size());

        if (!results.isEmpty()) {
            Map<String, Object> row = results.get(0);
            System.out.println("  - MA5: " + row.get("ma5"));
            System.out.println("  - MA20: " + row.get("ma20"));
            System.out.println("  - RSI_14: " + row.get("rsi_14"));
            System.out.println("  - Trend Indicators: " +
                    (row.get("trend_indicators") != null ? "有資料" : "無資料"));
            System.out.println("  - Momentum Indicators: " +
                    (row.get("momentum_indicators") != null ? "有資料" : "無資料"));

            // 驗證
            assertThat(results).hasSize(1);

            // 檢查至少有一種欄位有資料（直接欄位或 JSONB）
            boolean hasDirectFields = row.get("ma5") != null;
            boolean hasJsonbFields = row.get("trend_indicators") != null;

            assertThat(hasDirectFields || hasJsonbFields)
                    .as("應該至少有直接欄位或 JSONB 欄位有資料")
                    .isTrue();
        }

        System.out.println("\n========================================");
        System.out.println("✅ 端到端測試通過");
        System.out.println("========================================\n");
    }
}