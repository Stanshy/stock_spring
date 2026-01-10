package com.chris.fin_shark.m08.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 手動觸發財務指標計算請求
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculateFundamentalsRequest {

    /** 股票代碼列表（空則全部） */
    private List<String> stockIds;

    /** 年度（空則最新） */
    private Integer year;

    /** 季度（空則最新） */
    private Integer quarter;

    /** 是否強制重新計算 */
    @Builder.Default
    private Boolean force = false;
}
