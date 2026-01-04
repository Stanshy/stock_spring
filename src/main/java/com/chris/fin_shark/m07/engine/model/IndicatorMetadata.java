package com.chris.fin_shark.m07.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 指標元資料
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorMetadata {

    /** 指標名稱（英文） */
    private String name;

    /** 指標類別（TREND, MOMENTUM, VOLATILITY, VOLUME） */
    private String category;

    /** 指標名稱（中文） */
    private String nameZh;

    /** 描述 */
    private String description;

    /** 最少需要的資料點數 */
    private Integer minDataPoints;

    /** 預設參數 */
    private Map<String, Object> defaultParams;

    /** 優先級（P0, P1, P2） */
    private String priority;
}
