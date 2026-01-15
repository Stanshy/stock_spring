package com.chris.fin_shark.m11.enums;

import lombok.Getter;

/**
 * 參數優化方法列舉
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum OptimizationMethod {

    /**
     * 網格搜索
     */
    GRID_SEARCH("網格搜索", "窮舉所有參數組合"),

    /**
     * 隨機搜索
     */
    RANDOM_SEARCH("隨機搜索", "隨機抽樣參數組合"),

    /**
     * 貝葉斯優化
     */
    BAYESIAN("貝葉斯優化", "基於貝葉斯推理的優化方法");

    private final String displayName;
    private final String description;

    OptimizationMethod(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
