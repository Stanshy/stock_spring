package com.chris.fin_shark.m11.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 信號查詢請求 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalQueryRequest {

    @JsonProperty("start_date")
    private LocalDate startDate;

    @JsonProperty("end_date")
    private LocalDate endDate;

    @JsonProperty("signal_type")
    private String signalType;

    @JsonProperty("stock_id")
    private String stockId;

    @JsonProperty("min_confidence")
    private BigDecimal minConfidence;

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 50;
}
