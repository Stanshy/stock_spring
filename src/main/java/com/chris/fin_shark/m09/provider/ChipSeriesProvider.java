package com.chris.fin_shark.m09.provider;

import com.chris.fin_shark.m06.domain.InstitutionalTrading;
import com.chris.fin_shark.m06.domain.MarginTrading;
import com.chris.fin_shark.m06.repository.InstitutionalTradingRepository;
import com.chris.fin_shark.m06.repository.MarginTradingRepository;
import com.chris.fin_shark.m09.engine.model.ChipSeries;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 籌碼資料序列提供者
 * <p>
 * 從 M06 的 institutional_trading 與 margin_trading 取得資料，
 * 轉換為 ChipEngine 需要的 ChipSeries 格式。
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChipSeriesProvider {

    private final InstitutionalTradingRepository institutionalRepository;
    private final MarginTradingRepository marginRepository;

    /**
     * 取得單一股票的籌碼序列
     *
     * @param stockId 股票代碼
     * @param endDate 結束日期
     * @param days    天數
     * @return 籌碼序列
     */
    public ChipSeries get(String stockId, LocalDate endDate, int days) {
        log.debug("取得籌碼序列: stockId={}, endDate={}, days={}", stockId, endDate, days);

        LocalDate startDate = endDate.minusDays(days);

        // 從 M06 取得法人買賣超資料
        List<InstitutionalTrading> institutionalData = institutionalRepository
                .findByStockIdAndTradeDateBetweenOrderByTradeDateAsc(stockId, startDate, endDate);

        // 從 M06 取得融資融券資料
        List<MarginTrading> marginData = marginRepository
                .findByStockIdAndTradeDateBetweenOrderByTradeDateAsc(stockId, startDate, endDate);

        if (institutionalData.isEmpty() && marginData.isEmpty()) {
            log.warn("找不到籌碼資料: stockId={}, startDate={}, endDate={}",
                    stockId, startDate, endDate);
            return ChipSeries.builder()
                    .stockId(stockId)
                    .build();
        }

        // 轉換為 ChipSeries
        return convertToChipSeries(stockId, institutionalData, marginData);
    }

    /**
     * 批次取得多支股票的籌碼序列
     *
     * @param stockIds 股票代碼列表
     * @param endDate  結束日期
     * @param days     天數
     * @return 股票代碼 → 籌碼序列
     */
    public Map<String, ChipSeries> getBatch(List<String> stockIds, LocalDate endDate, int days) {
        log.info("批次取得籌碼序列: stockCount={}, endDate={}, days={}",
                stockIds.size(), endDate, days);

        Map<String, ChipSeries> result = new HashMap<>();

        for (String stockId : stockIds) {
            try {
                ChipSeries series = get(stockId, endDate, days);
                if (series.size() > 0) {
                    result.put(stockId, series);
                } else {
                    log.warn("跳過無資料的股票: {}", stockId);
                }
            } catch (Exception e) {
                log.error("取得籌碼資料失敗: stockId={}, error={}", stockId, e.getMessage());
            }
        }

        log.info("批次取得完成: 成功={}, 失敗={}",
                result.size(),
                stockIds.size() - result.size());

        return result;
    }

    /**
     * 轉換為 ChipSeries
     */
    private ChipSeries convertToChipSeries(String stockId,
                                            List<InstitutionalTrading> institutionalData,
                                            List<MarginTrading> marginData) {

        // 以法人資料的日期為主（若法人資料為空則用融資融券資料）
        List<LocalDate> dates;
        if (!institutionalData.isEmpty()) {
            dates = institutionalData.stream()
                    .map(InstitutionalTrading::getTradeDate)
                    .collect(Collectors.toList());
        } else {
            dates = marginData.stream()
                    .map(MarginTrading::getTradeDate)
                    .collect(Collectors.toList());
        }

        // 建立融資融券資料的日期索引
        Map<LocalDate, MarginTrading> marginMap = marginData.stream()
                .collect(Collectors.toMap(MarginTrading::getTradeDate, m -> m, (a, b) -> b));

        // 建立法人資料的日期索引
        Map<LocalDate, InstitutionalTrading> institutionalMap = institutionalData.stream()
                .collect(Collectors.toMap(InstitutionalTrading::getTradeDate, i -> i, (a, b) -> b));

        // 組裝 ChipSeries
        List<Long> foreignNet = new ArrayList<>();
        List<Long> trustNet = new ArrayList<>();
        List<Long> dealerNet = new ArrayList<>();
        List<Long> totalNet = new ArrayList<>();
        List<Long> marginBalance = new ArrayList<>();
        List<Long> marginQuota = new ArrayList<>();
        List<BigDecimal> marginUsageRate = new ArrayList<>();
        List<Long> shortBalance = new ArrayList<>();
        List<Long> shortQuota = new ArrayList<>();
        List<BigDecimal> shortUsageRate = new ArrayList<>();

        for (LocalDate date : dates) {
            // 法人資料
            InstitutionalTrading inst = institutionalMap.get(date);
            if (inst != null) {
                foreignNet.add(nullToZero(inst.getForeignNet()));
                trustNet.add(nullToZero(inst.getTrustNet()));
                dealerNet.add(nullToZero(inst.getDealerNet()));
                totalNet.add(nullToZero(inst.getTotalNet()));
            } else {
                foreignNet.add(0L);
                trustNet.add(0L);
                dealerNet.add(0L);
                totalNet.add(0L);
            }

            // 融資融券資料
            MarginTrading margin = marginMap.get(date);
            if (margin != null) {
                marginBalance.add(nullToZero(margin.getMarginBalance()));
                marginQuota.add(nullToZero(margin.getMarginQuota()));
                marginUsageRate.add(margin.getMarginUsageRate() != null
                        ? margin.getMarginUsageRate()
                        : BigDecimal.ZERO);
                shortBalance.add(nullToZero(margin.getShortBalance()));
                shortQuota.add(nullToZero(margin.getShortQuota()));
                shortUsageRate.add(margin.getShortUsageRate() != null
                        ? margin.getShortUsageRate()
                        : BigDecimal.ZERO);
            } else {
                marginBalance.add(0L);
                marginQuota.add(0L);
                marginUsageRate.add(BigDecimal.ZERO);
                shortBalance.add(0L);
                shortQuota.add(0L);
                shortUsageRate.add(BigDecimal.ZERO);
            }
        }

        return ChipSeries.builder()
                .stockId(stockId)
                .dates(dates)
                .foreignNet(foreignNet)
                .trustNet(trustNet)
                .dealerNet(dealerNet)
                .totalNet(totalNet)
                .marginBalance(marginBalance)
                .marginQuota(marginQuota)
                .marginUsageRate(marginUsageRate)
                .shortBalance(shortBalance)
                .shortQuota(shortQuota)
                .shortUsageRate(shortUsageRate)
                .build();
    }

    /**
     * null 轉為 0
     */
    private Long nullToZero(Long value) {
        return value != null ? value : 0L;
    }

    /**
     * 檢查是否有足夠的資料
     *
     * @param stockId      股票代碼
     * @param endDate      結束日期
     * @param requiredDays 需要的天數
     * @return 是否足夠
     */
    public boolean hasEnoughData(String stockId, LocalDate endDate, int requiredDays) {
        ChipSeries series = get(stockId, endDate, requiredDays + 10);  // 多取 10 天以防萬一
        return series.size() >= requiredDays;
    }
}
