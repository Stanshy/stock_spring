package com.chris.fin_shark.m09.engine.model;

import com.chris.fin_shark.m09.enums.SignalSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 籌碼異常訊號（計算引擎輸出模型）
 * <p>
 * 表示偵測到的籌碼異常訊號。
 * 這是 engine 層的模型，與 domain Entity 分離。
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChipSignal {

    /** 訊號代碼（如 CHIP_SIG_001） */
    private String signalCode;

    /** 訊號名稱 */
    private String signalName;

    /** 訊號類型（INSTITUTIONAL / MARGIN / CONCENTRATION / COMPOSITE） */
    private String signalType;

    /** 嚴重度 */
    private SignalSeverity severity;

    /** 訊號觸發值 */
    private BigDecimal signalValue;

    /** 門檻值 */
    private BigDecimal thresholdValue;

    /** 偏離程度（標準差倍數） */
    private BigDecimal deviation;

    /** 訊號描述 */
    private String description;

    /** 建議操作 */
    private String recommendation;

    // ========== 工廠方法 ==========

    /**
     * 建立法人類訊號
     */
    public static ChipSignal institutionalSignal(String signalCode,
                                                  String signalName,
                                                  SignalSeverity severity,
                                                  BigDecimal value,
                                                  BigDecimal threshold,
                                                  String description) {
        return ChipSignal.builder()
                .signalCode(signalCode)
                .signalName(signalName)
                .signalType("INSTITUTIONAL")
                .severity(severity)
                .signalValue(value)
                .thresholdValue(threshold)
                .description(description)
                .build();
    }

    /**
     * 建立融資融券類訊號
     */
    public static ChipSignal marginSignal(String signalCode,
                                           String signalName,
                                           SignalSeverity severity,
                                           BigDecimal value,
                                           BigDecimal threshold,
                                           String description) {
        return ChipSignal.builder()
                .signalCode(signalCode)
                .signalName(signalName)
                .signalType("MARGIN")
                .severity(severity)
                .signalValue(value)
                .thresholdValue(threshold)
                .description(description)
                .build();
    }
}
