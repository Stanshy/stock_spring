package com.chris.fin_shark.m09.engine.calculator.signal;

import com.chris.fin_shark.m09.engine.model.ChipSeries;
import com.chris.fin_shark.m09.engine.model.ChipSignal;

import java.util.List;
import java.util.Map;

/**
 * 籌碼訊號偵測器介面
 * <p>
 * 所有訊號偵測器必須實作此介面。
 * 與 ChipCalculator 不同，訊號偵測器返回的是 ChipSignal 列表。
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
public interface ChipSignalDetector {

    /**
     * 偵測器名稱
     */
    String getName();

    /**
     * 偵測訊號
     *
     * @param series 籌碼資料序列
     * @param params 參數
     * @return 偵測到的訊號列表
     */
    List<ChipSignal> detect(ChipSeries series, Map<String, Object> params);
}
