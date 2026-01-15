package com.chris.fin_shark.m09.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 籌碼資料序列
 * <p>
 * 籌碼計算引擎的輸入資料結構，包含三大法人買賣超與融資融券序列。
 * 與 M07 PriceSeries 對應，但資料來源為 institutional_trading + margin_trading。
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChipSeries {

    /** 股票代碼 */
    private String stockId;

    /** 日期列表（由舊到新排序） */
    @Builder.Default
    private List<LocalDate> dates = new ArrayList<>();

    // ========== 三大法人資料 ==========

    /** 外資買賣超（股） */
    @Builder.Default
    private List<Long> foreignNet = new ArrayList<>();

    /** 投信買賣超（股） */
    @Builder.Default
    private List<Long> trustNet = new ArrayList<>();

    /** 自營商買賣超（股） */
    @Builder.Default
    private List<Long> dealerNet = new ArrayList<>();

    /** 三大法人合計買賣超（股） */
    @Builder.Default
    private List<Long> totalNet = new ArrayList<>();

    // ========== 融資融券資料 ==========

    /** 融資餘額（股） */
    @Builder.Default
    private List<Long> marginBalance = new ArrayList<>();

    /** 融資限額（股） */
    @Builder.Default
    private List<Long> marginQuota = new ArrayList<>();

    /** 融資使用率（%） */
    @Builder.Default
    private List<BigDecimal> marginUsageRate = new ArrayList<>();

    /** 融券餘額（股） */
    @Builder.Default
    private List<Long> shortBalance = new ArrayList<>();

    /** 融券限額（股） */
    @Builder.Default
    private List<Long> shortQuota = new ArrayList<>();

    /** 融券使用率（%） */
    @Builder.Default
    private List<BigDecimal> shortUsageRate = new ArrayList<>();

    // ========== 價格資料（選填，用於成本估算） ==========

    /** 收盤價 */
    @Builder.Default
    private List<BigDecimal> closePrice = new ArrayList<>();

    /** 成交量（股） */
    @Builder.Default
    private List<Long> volume = new ArrayList<>();

    /**
     * 取得資料筆數
     */
    public int size() {
        return dates != null ? dates.size() : 0;
    }

    /**
     * 是否為空
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    // ========== 陣列轉換（用於計算） ==========

    /**
     * 取得外資買賣超陣列
     */
    public long[] getForeignNetArray() {
        return toLongArray(foreignNet);
    }

    /**
     * 取得投信買賣超陣列
     */
    public long[] getTrustNetArray() {
        return toLongArray(trustNet);
    }

    /**
     * 取得自營商買賣超陣列
     */
    public long[] getDealerNetArray() {
        return toLongArray(dealerNet);
    }

    /**
     * 取得三大法人合計買賣超陣列
     */
    public long[] getTotalNetArray() {
        return toLongArray(totalNet);
    }

    /**
     * 取得融資餘額陣列
     */
    public long[] getMarginBalanceArray() {
        return toLongArray(marginBalance);
    }

    /**
     * 取得融券餘額陣列
     */
    public long[] getShortBalanceArray() {
        return toLongArray(shortBalance);
    }

    /**
     * 取得融資使用率陣列
     */
    public double[] getMarginUsageRateArray() {
        return toDoubleArray(marginUsageRate);
    }

    /**
     * 取得收盤價陣列
     */
    public double[] getClosePriceArray() {
        return toDoubleArray(closePrice);
    }

    /**
     * 計算融資增減陣列
     */
    public long[] getMarginChangeArray() {
        if (marginBalance == null || marginBalance.size() < 2) {
            return new long[0];
        }
        long[] changes = new long[marginBalance.size()];
        changes[0] = 0; // 第一天無法計算增減
        for (int i = 1; i < marginBalance.size(); i++) {
            Long curr = marginBalance.get(i);
            Long prev = marginBalance.get(i - 1);
            changes[i] = (curr != null && prev != null) ? curr - prev : 0;
        }
        return changes;
    }

    /**
     * 計算融券增減陣列
     */
    public long[] getShortChangeArray() {
        if (shortBalance == null || shortBalance.size() < 2) {
            return new long[0];
        }
        long[] changes = new long[shortBalance.size()];
        changes[0] = 0;
        for (int i = 1; i < shortBalance.size(); i++) {
            Long curr = shortBalance.get(i);
            Long prev = shortBalance.get(i - 1);
            changes[i] = (curr != null && prev != null) ? curr - prev : 0;
        }
        return changes;
    }

    // ========== 私有輔助方法 ==========

    private long[] toLongArray(List<Long> list) {
        if (list == null || list.isEmpty()) {
            return new long[0];
        }
        return list.stream()
                .mapToLong(v -> v != null ? v : 0L)
                .toArray();
    }

    private double[] toDoubleArray(List<BigDecimal> list) {
        if (list == null || list.isEmpty()) {
            return new double[0];
        }
        return list.stream()
                .mapToDouble(v -> v != null ? v.doubleValue() : 0.0)
                .toArray();
    }

    // ========== 測試用工廠方法 ==========

    /**
     * 建立測試用的籌碼序列（只有法人買賣超）
     */
    public static ChipSeries createTestInstitutional(String stockId,
                                                      long[] foreignNetData,
                                                      long[] trustNetData,
                                                      long[] dealerNetData) {
        List<LocalDate> dateList = new ArrayList<>();
        List<Long> foreignList = new ArrayList<>();
        List<Long> trustList = new ArrayList<>();
        List<Long> dealerList = new ArrayList<>();
        List<Long> totalList = new ArrayList<>();

        int size = foreignNetData.length;
        for (int i = 0; i < size; i++) {
            dateList.add(LocalDate.now().minusDays(size - i - 1));
            foreignList.add(foreignNetData[i]);
            trustList.add(trustNetData[i]);
            dealerList.add(dealerNetData[i]);
            totalList.add(foreignNetData[i] + trustNetData[i] + dealerNetData[i]);
        }

        return ChipSeries.builder()
                .stockId(stockId)
                .dates(dateList)
                .foreignNet(foreignList)
                .trustNet(trustList)
                .dealerNet(dealerList)
                .totalNet(totalList)
                .build();
    }
}
