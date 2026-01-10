package com.chris.fin_shark.m08.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 指標元數據
 * <p>
 * 描述財務指標的基本資訊
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorMetadata {

    /** 指標名稱（英文代碼） */
    private String name;

    /** 指標顯示名稱（中文） */
    private String displayName;

    /** 指標類別 */
    private String category;

    /** 指標描述 */
    private String description;

    /** 單位 */
    private String unit;

    /** 優先級（P0/P1/P2） */
    private String priority;

    /** 是否需要歷史資料 */
    private Boolean requiresHistory;
}
