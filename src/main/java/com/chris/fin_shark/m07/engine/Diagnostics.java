package com.chris.fin_shark.m07.engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 診斷訊息
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Diagnostics {

    @Builder.Default
    private List<DiagnosticMessage> warnings = new ArrayList<>();

    @Builder.Default
    private List<DiagnosticMessage> errors = new ArrayList<>();

    public void addWarning(String indicator, String message) {
        if (warnings == null) {
            warnings = new ArrayList<>();
        }
        warnings.add(new DiagnosticMessage("WARNING", indicator, message));
    }

    public void addError(String indicator, String message) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(new DiagnosticMessage("ERROR", indicator, message));
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DiagnosticMessage {
        private String level;
        private String indicator;
        private String message;
    }
}
