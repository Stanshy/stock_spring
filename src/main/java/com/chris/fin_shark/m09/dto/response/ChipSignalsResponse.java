package com.chris.fin_shark.m09.dto.response;

import com.chris.fin_shark.m09.dto.ChipSignalDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 籌碼異常訊號回應 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChipSignalsResponse {

    @JsonProperty("trade_date")
    private LocalDate tradeDate;

    @JsonProperty("total_count")
    private Integer totalCount;

    /**
     * 各嚴重度訊號數量
     */
    @JsonProperty("severity_counts")
    private Map<String, Integer> severityCounts;

    @JsonProperty("signals")
    private List<ChipSignalDTO> signals;
}
