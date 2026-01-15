package com.chris.fin_shark.m11.enums;

import lombok.Getter;

/**
 * 優化目標函數列舉
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum ObjectiveFunction {

    /**
     * 夏普比率
     */
    SHARPE_RATIO("夏普比率", "風險調整後報酬"),

    /**
     * 總報酬率
     */
    TOTAL_RETURN("總報酬率", "累計報酬率"),

    /**
     * 勝率
     */
    WIN_RATE("勝率", "獲利交易佔比"),

    /**
     * 最大回撤
     */
    MAX_DRAWDOWN("最大回撤", "最大虧損幅度");

    private final String displayName;
    private final String description;

    ObjectiveFunction(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
