package com.chris.fin_shark.m09.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 籌碼計算診斷訊息容器
 * <p>
 * 記錄計算過程中的警告與錯誤訊息。
 * 結構與 M07 Diagnostics 一致，確保風格統一。
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

    /** 警告訊息 */
    @Builder.Default
    private List<DiagnosticMessage> warnings = new ArrayList<>();

    /** 錯誤訊息 */
    @Builder.Default
    private List<DiagnosticMessage> errors = new ArrayList<>();

    /** 計算耗時（毫秒） */
    private Long calculationTimeMs;

    /**
     * 加入警告
     *
     * @param calculator 計算器名稱
     * @param message    警告訊息
     */
    public void addWarning(String calculator, String message) {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        warnings.add(new DiagnosticMessage(calculator, message, "WARNING"));
    }

    /**
     * 加入錯誤
     *
     * @param calculator 計算器名稱
     * @param message    錯誤訊息
     */
    public void addError(String calculator, String message) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(new DiagnosticMessage(calculator, message, "ERROR"));
    }

    /**
     * 是否有警告
     */
    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    /**
     * 是否有錯誤
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    /**
     * 合併另一個診斷結果
     *
     * @param other 另一個診斷結果
     */
    public void merge(Diagnostics other) {
        if (other == null) {
            return;
        }
        if (other.warnings != null) {
            if (this.warnings == null) {
                this.warnings = new ArrayList<>();
            }
            this.warnings.addAll(other.warnings);
        }
        if (other.errors != null) {
            if (this.errors == null) {
                this.errors = new ArrayList<>();
            }
            this.errors.addAll(other.errors);
        }
    }

    /**
     * 診斷訊息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DiagnosticMessage {
        /** 計算器名稱 */
        private String calculator;
        /** 訊息內容 */
        private String message;
        /** 等級（WARNING / ERROR） */
        private String level;
    }
}
