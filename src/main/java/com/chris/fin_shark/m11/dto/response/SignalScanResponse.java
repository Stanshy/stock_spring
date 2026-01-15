package com.chris.fin_shark.m11.dto.response;

import com.chris.fin_shark.m11.dto.StrategySignalDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 全市場信號掃描回應 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignalScanResponse {

    @JsonProperty("trade_date")
    private LocalDate tradeDate;

    @JsonProperty("scan_time_ms")
    private Long scanTimeMs;

    @JsonProperty("strategies_scanned")
    private Integer strategiesScanned;

    @JsonProperty("total_signals")
    private Integer totalSignals;

    @JsonProperty("signal_summary")
    private SignalSummaryDTO signalSummary;

    private List<StrategySignalDTO> signals;

    @JsonProperty("stock_signal_count")
    private Map<String, Integer> stockSignalCount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignalSummaryDTO {
        private Integer buy;
        private Integer sell;
        private Integer hold;
    }
}
