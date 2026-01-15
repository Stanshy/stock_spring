package com.chris.fin_shark.m09.dto.response;

import com.chris.fin_shark.m09.dto.ChipRankingDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 籌碼排行榜回應 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChipRankingResponse {

    @JsonProperty("rank_type")
    private String rankType;

    @JsonProperty("rank_name")
    private String rankName;

    @JsonProperty("trade_date")
    private LocalDate tradeDate;

    @JsonProperty("market_type")
    private String marketType;

    @JsonProperty("total_count")
    private Integer totalCount;

    @JsonProperty("rankings")
    private List<ChipRankingDTO> rankings;
}
