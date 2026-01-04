package com.chris.fin_shark.m07.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 價格序列
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceSeries {

    /** 股票代碼 */
    private String stockId;

    /** 日期列表 */
    @Builder.Default
    private List<LocalDate> dates = new ArrayList<>();

    /** 開盤價列表 */
    @Builder.Default
    private List<BigDecimal> open = new ArrayList<>();

    /** 最高價列表 */
    @Builder.Default
    private List<BigDecimal> high = new ArrayList<>();

    /** 最低價列表 */
    @Builder.Default
    private List<BigDecimal> low = new ArrayList<>();

    /** 收盤價列表 */
    @Builder.Default
    private List<BigDecimal> close = new ArrayList<>();

    /** 成交量列表 */
    @Builder.Default
    private List<Long> volume = new ArrayList<>();

    /**
     * 取得資料筆數
     */
    public int size() {
        return close != null ? close.size() : 0;
    }

    /**
     * 取得收盤價陣列（用於計算）
     */
    public double[] getCloseArray() {
        if (close == null || close.isEmpty()) {
            return new double[0];
        }
        return close.stream()
                .mapToDouble(BigDecimal::doubleValue)
                .toArray();
    }

    /**
     * 取得開盤價陣列
     */
    public double[] getOpenArray() {
        if (open == null || open.isEmpty()) {
            return new double[0];
        }
        return open.stream()
                .mapToDouble(BigDecimal::doubleValue)
                .toArray();
    }

    /**
     * 取得最高價陣列
     */
    public double[] getHighArray() {
        if (high == null || high.isEmpty()) {
            return new double[0];
        }
        return high.stream()
                .mapToDouble(BigDecimal::doubleValue)
                .toArray();
    }

    /**
     * 取得最低價陣列
     */
    public double[] getLowArray() {
        if (low == null || low.isEmpty()) {
            return new double[0];
        }
        return low.stream()
                .mapToDouble(BigDecimal::doubleValue)
                .toArray();
    }

    /**
     * 取得成交量陣列
     */
    public long[] getVolumeArray() {
        if (volume == null || volume.isEmpty()) {
            return new long[0];
        }
        return volume.stream()
                .mapToLong(Long::longValue)
                .toArray();
    }

    /**
     * 建立測試用的價格序列（只有收盤價）
     */
    public static PriceSeries createTest(String stockId, double[] closePrices) {
        List<BigDecimal> closeList = new ArrayList<>();
        List<LocalDate> dateList = new ArrayList<>();

        for (int i = 0; i < closePrices.length; i++) {
            closeList.add(BigDecimal.valueOf(closePrices[i]));
            dateList.add(LocalDate.now().minusDays(closePrices.length - i - 1));
        }

        return PriceSeries.builder()
                .stockId(stockId)
                .dates(dateList)
                .close(closeList)
                .open(closeList)   // 測試用，開盤價 = 收盤價
                .high(closeList)   // 測試用，最高價 = 收盤價
                .low(closeList)    // 測試用，最低價 = 收盤價
                .volume(new ArrayList<>())
                .build();
    }
}
