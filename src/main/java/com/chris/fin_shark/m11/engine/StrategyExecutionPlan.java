package com.chris.fin_shark.m11.engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 策略執行計劃
 * <p>
 * 定義策略執行的範圍與選項
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyExecutionPlan {

    /**
     * 策略 ID
     */
    private String strategyId;

    /**
     * 執行日期
     */
    @Builder.Default
    private LocalDate executionDate = LocalDate.now();

    /**
     * 股票範圍
     */
    @Builder.Default
    private StockUniverse stockUniverse = StockUniverse.twseAll();

    /**
     * 是否包含因子值
     */
    @Builder.Default
    private boolean includeFactorValues = true;

    /**
     * 是否包含診斷資訊
     */
    @Builder.Default
    private boolean includeDiagnostics = true;

    /**
     * 是否儲存結果
     */
    @Builder.Default
    private boolean saveResults = true;

    /**
     * 是否平行執行
     */
    @Builder.Default
    private boolean parallelExecution = true;

    /**
     * 批次大小
     */
    @Builder.Default
    private int batchSize = 100;

    /**
     * 股票範圍定義
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockUniverse {

        /**
         * 範圍類型
         */
        private UniverseType type;

        /**
         * 市場類型
         */
        private String marketType;

        /**
         * 最低成交量
         */
        private Integer minVolume;

        /**
         * 是否排除 ETF
         */
        @Builder.Default
        private boolean excludeEtf = true;

        /**
         * 指定股票清單
         */
        private List<String> stockIds;

        /**
         * 指定產業
         */
        private List<String> industries;

        /**
         * 範圍類型列舉
         */
        public enum UniverseType {
            MARKET,     // 全市場
            STOCKS,     // 指定股票
            INDUSTRY,   // 指定產業
            WATCHLIST   // 自選股
        }

        /**
         * 全市場（上市）
         */
        public static StockUniverse twseAll() {
            return StockUniverse.builder()
                    .type(UniverseType.MARKET)
                    .marketType("TWSE")
                    .minVolume(1000)
                    .excludeEtf(true)
                    .build();
        }

        /**
         * 指定股票
         */
        public static StockUniverse ofStocks(String... stockIds) {
            return StockUniverse.builder()
                    .type(UniverseType.STOCKS)
                    .stockIds(List.of(stockIds))
                    .build();
        }

        /**
         * 指定股票清單
         */
        public static StockUniverse ofStocks(List<String> stockIds) {
            return StockUniverse.builder()
                    .type(UniverseType.STOCKS)
                    .stockIds(stockIds)
                    .build();
        }
    }

    // ==================== 靜態工廠方法 ====================

    /**
     * 全市場執行計劃
     */
    public static StrategyExecutionPlan fullMarket(String strategyId) {
        return StrategyExecutionPlan.builder()
                .strategyId(strategyId)
                .executionDate(LocalDate.now())
                .stockUniverse(StockUniverse.twseAll())
                .includeFactorValues(true)
                .includeDiagnostics(true)
                .saveResults(true)
                .parallelExecution(true)
                .batchSize(100)
                .build();
    }

    /**
     * 單一股票測試計劃
     */
    public static StrategyExecutionPlan singleStock(String strategyId, String stockId) {
        return StrategyExecutionPlan.builder()
                .strategyId(strategyId)
                .executionDate(LocalDate.now())
                .stockUniverse(StockUniverse.ofStocks(stockId))
                .includeFactorValues(true)
                .includeDiagnostics(true)
                .saveResults(false)
                .parallelExecution(false)
                .build();
    }
}
