package com.chris.fin_shark.m09.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 籌碼異常訊號 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChipSignalDTO {

    @JsonProperty("signal_id")
    private Long signalId;

    @JsonProperty("stock_id")
    private String stockId;

    @JsonProperty("trade_date")
    private LocalDate tradeDate;

    @JsonProperty("signal_code")
    private String signalCode;

    @JsonProperty("signal_name")
    private String signalName;

    @JsonProperty("signal_type")
    private String signalType;

    @JsonProperty("severity")
    private String severity;

    @JsonProperty("signal_value")
    private BigDecimal signalValue;

    @JsonProperty("threshold_value")
    private BigDecimal thresholdValue;

    @JsonProperty("deviation")
    private BigDecimal deviation;

    @JsonProperty("description")
    private String description;

    @JsonProperty("recommendation")
    private String recommendation;

    @JsonProperty("is_active")
    private Boolean isActive;

    @JsonProperty("acknowledged_at")
    private LocalDateTime acknowledgedAt;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
