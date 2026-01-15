package com.chris.fin_shark.m11.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 策略查詢請求 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyQueryRequest {

    /**
     * 策略狀態篩選
     */
    private String status;

    /**
     * 策略類型篩選
     */
    private String type;

    /**
     * 關鍵字搜尋
     */
    private String keyword;

    /**
     * 頁碼（從 0 開始）
     */
    @Builder.Default
    private Integer page = 0;

    /**
     * 每頁筆數
     */
    @Builder.Default
    private Integer size = 20;

    /**
     * 排序欄位
     */
    @Builder.Default
    private String sortField = "created_at";

    /**
     * 排序方向
     */
    @Builder.Default
    private String sortDirection = "desc";
}
