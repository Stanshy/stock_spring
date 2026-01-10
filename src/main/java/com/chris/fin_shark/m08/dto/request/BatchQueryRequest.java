package com.chris.fin_shark.m08.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批次查詢財務指標請求
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchQueryRequest {

    /** 股票代碼列表 */
    @NotEmpty(message = "股票代碼列表不可為空")
    private List<String> stockIds;

    /** 年度 */
    private Integer year;

    /** 季度 */
    private Integer quarter;

    /** 指標列表（選填，不指定則返回全部） */
    private List<String> indicators;
}
