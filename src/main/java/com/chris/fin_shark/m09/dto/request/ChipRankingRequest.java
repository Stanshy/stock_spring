package com.chris.fin_shark.m09.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 籌碼排行榜請求 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChipRankingRequest {

    /**
     * 排行榜類型
     * <p>
     * RANK001: 外資買超排行
     * RANK002: 外資賣超排行
     * RANK003: 投信買超排行
     * RANK004: 投信賣超排行
     * RANK005: 外資連續買超排行
     * RANK006: 融資增加排行
     * RANK007: 融資減少排行
     * RANK008: 券資比排行
     * RANK009: 三大法人合計買超
     * </p>
     */
    @NotBlank(message = "rank_type is required")
    @JsonProperty("rank_type")
    private String rankType;

    /**
     * 交易日期
     */
    @JsonProperty("trade_date")
    private LocalDate tradeDate;

    /**
     * 市場類型（TWSE / OTC）
     */
    @JsonProperty("market_type")
    private String marketType;

    /**
     * 筆數限制
     */
    @JsonProperty("limit")
    @Builder.Default
    @Min(1)
    @Max(100)
    private Integer limit = 50;

    /**
     * 最小成交量過濾
     */
    @JsonProperty("min_volume")
    private Long minVolume;
}
