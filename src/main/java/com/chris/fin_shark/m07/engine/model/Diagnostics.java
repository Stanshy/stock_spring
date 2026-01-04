package com.chris.fin_shark.m07.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 診斷訊息容器
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

    /**
     * 加入警告
     */
    public void addWarning(String indicator, String message) {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        warnings.add(new DiagnosticMessage(indicator, message, "WARNING"));
    }

    /**
     * 加入錯誤
     */
    public void addError(String indicator, String message) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(new DiagnosticMessage(indicator, message, "ERROR"));
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
     * 診斷訊息
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DiagnosticMessage {
        private String indicator;
        private String message;
        private String level;
    }
}
