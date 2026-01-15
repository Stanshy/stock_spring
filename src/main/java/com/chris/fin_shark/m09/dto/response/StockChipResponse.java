package com.chris.fin_shark.m09.dto.response;

import com.chris.fin_shark.m09.dto.ChipAnalysisResultDTO;
import com.chris.fin_shark.m09.dto.ChipSignalDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 個股籌碼分析回應 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockChipResponse {

    @JsonProperty("stock_id")
    private String stockId;

    @JsonProperty("stock_name")
    private String stockName;

    /**
     * 最新籌碼分析結果
     */
    @JsonProperty("latest")
    private ChipAnalysisResultDTO latest;

    /**
     * 歷史籌碼分析結果
     */
    @JsonProperty("history")
    private List<ChipAnalysisResultDTO> history;

    /**
     * 相關異常訊號
     */
    @JsonProperty("signals")
    private List<ChipSignalDTO> signals;

    /**
     * 訊號數量
     */
    @JsonProperty("signal_count")
    private Integer signalCount;
}
