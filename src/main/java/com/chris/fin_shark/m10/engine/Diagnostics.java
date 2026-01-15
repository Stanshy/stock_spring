package com.chris.fin_shark.m10.engine;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 診斷訊息
 * <p>
 * 追蹤型態偵測過程中的警告和錯誤
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
public class Diagnostics {

    /**
     * 診斷訊息項目
     */
    @Data
    @NoArgsConstructor
    public static class Message {
        private String level;       // ERROR, WARNING, INFO
        private String detector;    // 偵測器名稱
        private String message;     // 訊息內容
        private Long timestamp;     // 時間戳

        public Message(String level, String detector, String message) {
            this.level = level;
            this.detector = detector;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
    }

    /**
     * 錯誤訊息列表
     */
    private List<Message> errors = new ArrayList<>();

    /**
     * 警告訊息列表
     */
    private List<Message> warnings = new ArrayList<>();

    /**
     * 資訊訊息列表
     */
    private List<Message> infos = new ArrayList<>();

    /**
     * 計算耗時（毫秒）
     */
    private Long calculationTimeMs;

    /**
     * 掃描的交易日數
     */
    private Integer tradingDaysScanned;

    /**
     * 檢查的型態數量
     */
    private Integer patternsChecked;

    /**
     * 偵測到的型態數量
     */
    private Integer patternsDetected;

    // === 添加訊息方法 ===

    /**
     * 添加錯誤
     */
    public void addError(String detector, String message) {
        errors.add(new Message("ERROR", detector, message));
    }

    /**
     * 添加警告
     */
    public void addWarning(String detector, String message) {
        warnings.add(new Message("WARNING", detector, message));
    }

    /**
     * 添加資訊
     */
    public void addInfo(String detector, String message) {
        infos.add(new Message("INFO", detector, message));
    }

    // === 查詢方法 ===

    /**
     * 是否有錯誤
     */
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * 是否有警告
     */
    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    /**
     * 取得所有訊息（合併）
     */
    public List<Message> getAllMessages() {
        List<Message> all = new ArrayList<>();
        all.addAll(errors);
        all.addAll(warnings);
        all.addAll(infos);
        return all;
    }

    /**
     * 取得錯誤數量
     */
    public int getErrorCount() {
        return errors.size();
    }

    /**
     * 取得警告數量
     */
    public int getWarningCount() {
        return warnings.size();
    }

    // === 合併方法 ===

    /**
     * 合併另一個 Diagnostics
     */
    public void merge(Diagnostics other) {
        if (other != null) {
            this.errors.addAll(other.getErrors());
            this.warnings.addAll(other.getWarnings());
            this.infos.addAll(other.getInfos());
        }
    }

    // === 靜態方法 ===

    /**
     * 建立空的 Diagnostics
     */
    public static Diagnostics empty() {
        return new Diagnostics();
    }

    /**
     * 建立帶有計時資訊的 Diagnostics
     */
    public static Diagnostics withTiming(long calculationTimeMs, int daysScanned, int patternsChecked) {
        Diagnostics d = new Diagnostics();
        d.setCalculationTimeMs(calculationTimeMs);
        d.setTradingDaysScanned(daysScanned);
        d.setPatternsChecked(patternsChecked);
        return d;
    }
}
