package com.chris.fin_shark.m08.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 診斷資訊
 * <p>
 * 記錄計算過程中的執行時間、錯誤、警告等資訊
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Diagnostics {

    /** 計算耗時（毫秒） */
    private Long calculationTime;

    /** 總計算器數量 */
    private Integer totalCalculators;

    /** 成功執行的計算器數量 */
    private Integer successfulCalculators;

    /** 錯誤列表 */
    @Builder.Default
    private List<String> errors = new ArrayList<>();

    /** 警告列表 */
    @Builder.Default
    private List<String> warnings = new ArrayList<>();

    /** 資料品質問題列表 */
    @Builder.Default
    private List<String> dataQualityIssues = new ArrayList<>();

    /**
     * 新增錯誤
     */
    public void addError(String error) {
        errors.add(error);
    }

    /**
     * 新增警告
     */
    public void addWarning(String warning) {
        warnings.add(warning);
    }

    /**
     * 新增資料品質問題
     */
    public void addDataQualityIssue(String issue) {
        dataQualityIssues.add(issue);
    }

    /**
     * 檢查是否有錯誤
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * 檢查是否有警告
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }
}
