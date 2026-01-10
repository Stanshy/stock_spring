package com.chris.fin_shark.m08.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 查詢指標歷史趨勢請求
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendQueryRequest {

    /** 股票代碼 */
    @NotBlank(message = "股票代碼不可為空")
    private String stockId;

    /** 指標名稱 */
    @NotBlank(message = "指標名稱不可為空")
    private String indicator;

    /** 起始年度 */
    @NotNull(message = "起始年度不可為空")
    private Integer startYear;

    /** 起始季度 */
    @NotNull(message = "起始季度不可為空")
    @Min(value = 1, message = "季度必須在 1-4 之間")
    @Max(value = 4, message = "季度必須在 1-4 之間")
    private Integer startQuarter;

    /** 結束年度 */
    @NotNull(message = "結束年度不可為空")
    private Integer endYear;

    /** 結束季度 */
    @NotNull(message = "結束季度不可為空")
    @Min(value = 1, message = "季度必須在 1-4 之間")
    @Max(value = 4, message = "季度必須在 1-4 之間")
    private Integer endQuarter;
}
