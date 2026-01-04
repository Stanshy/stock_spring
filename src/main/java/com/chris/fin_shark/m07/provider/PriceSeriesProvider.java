package com.chris.fin_shark.m07.provider;

import com.chris.fin_shark.m06.domain.StockPrice;
import com.chris.fin_shark.m06.repository.StockPriceRepository;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 價格序列提供者
 * <p>
 * 從 M06 StockPrice 轉換為 Engine 需要的 PriceSeries
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PriceSeriesProvider {

    private final StockPriceRepository stockPriceRepository;

    /**
     * 取得單一股票的價格序列
     *
     * @param stockId 股票代碼
     * @param endDate 結束日期
     * @param days    天數
     * @return 價格序列
     */
    public PriceSeries get(String stockId, LocalDate endDate, int days) {
        log.debug("取得價格序列: stockId={}, endDate={}, days={}", stockId, endDate, days);

        LocalDate startDate = endDate.minusDays(days);

        // 從 M06 取得股價資料
        List<StockPrice> prices = stockPriceRepository
                .findByStockIdAndTradeDateBetweenOrderByTradeDateAsc(
                        stockId,
                        startDate,
                        endDate
                );

        if (prices.isEmpty()) {
            log.warn("找不到股價資料: stockId={}, startDate={}, endDate={}",
                    stockId, startDate, endDate);
            return PriceSeries.builder()
                    .stockId(stockId)
                    .build();
        }

        // 轉換為 PriceSeries
        return convertToPriceSeries(stockId, prices);
    }

    /**
     * 批次取得多支股票的價格序列
     *
     * @param stockIds 股票代碼列表
     * @param endDate  結束日期
     * @param days     天數
     * @return 股票代碼 → 價格序列
     */
    public Map<String, PriceSeries> getBatch(List<String> stockIds, LocalDate endDate, int days) {
        log.info("批次取得價格序列: stockCount={}, endDate={}, days={}",
                stockIds.size(), endDate, days);

        Map<String, PriceSeries> result = new HashMap<>();

        for (String stockId : stockIds) {
            try {
                PriceSeries series = get(stockId, endDate, days);
                if (series.size() > 0) {
                    result.put(stockId, series);
                } else {
                    log.warn("跳過無資料的股票: {}", stockId);
                }
            } catch (Exception e) {
                log.error("取得股價失敗: stockId={}, error={}", stockId, e.getMessage());
            }
        }

        log.info("批次取得完成: 成功={}, 失敗={}",
                result.size(),
                stockIds.size() - result.size());

        return result;
    }

    /**
     * 轉換為 PriceSeries
     */
    private PriceSeries convertToPriceSeries(String stockId, List<StockPrice> prices) {
        return PriceSeries.builder()
                .stockId(stockId)
                .dates(prices.stream()
                        .map(StockPrice::getTradeDate)
                        .collect(Collectors.toList()))
                .open(prices.stream()
                        .map(StockPrice::getOpenPrice)
                        .collect(Collectors.toList()))
                .high(prices.stream()
                        .map(StockPrice::getHighPrice)
                        .collect(Collectors.toList()))
                .low(prices.stream()
                        .map(StockPrice::getLowPrice)
                        .collect(Collectors.toList()))
                .close(prices.stream()
                        .map(StockPrice::getClosePrice)
                        .collect(Collectors.toList()))
                .volume(prices.stream()
                        .map(StockPrice::getVolume)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * 檢查是否有足夠的資料
     *
     * @param stockId       股票代碼
     * @param endDate       結束日期
     * @param requiredDays  需要的天數
     * @return 是否足夠
     */
    public boolean hasEnoughData(String stockId, LocalDate endDate, int requiredDays) {
        PriceSeries series = get(stockId, endDate, requiredDays + 10);  // 多取 10 天以防萬一
        return series.size() >= requiredDays;
    }
}
