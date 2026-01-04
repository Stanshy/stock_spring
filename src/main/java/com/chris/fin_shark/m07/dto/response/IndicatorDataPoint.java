package com.chris.fin_shark.m07.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * 指標資料點
 * <p>
 * 單一日期的技術指標資料
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorDataPoint {

    /** 計算日期 */
    @JsonProperty("calculation_date")
    private LocalDate calculationDate;

    /** 趨勢指標 */
    @JsonProperty("trend")
    private Map<String, Object> trend;

    /** 動能指標 */
    @JsonProperty("momentum")
    private Map<String, Object> momentum;

    /** 波動性指標 */
    @JsonProperty("volatility")
    private Map<String, Object> volatility;

    /** 成交量指標 */
    @JsonProperty("volume")
    private Map<String, Object> volume;
}

