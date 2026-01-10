package com.chris.fin_shark.m08.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * 財務異常警示 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialAlertDTO {

    /** 警示 ID */
    private Long alertId;

    /** 股票代碼 */
    private String stockId;

    /** 股票名稱 */
    private String stockName;

    /** 年度 */
    private Integer year;

    /** 季度 */
    private Integer quarter;

    /** 偵測日期 */
    private LocalDate detectionDate;

    /** 警示類型 */
    private String alertType;

    /** 警示類別 */
    private String alertCategory;

    /** 嚴重程度 */
    private String severity;

    /** 警示訊息 */
    private String alertMessage;

    /** 警示詳細資訊 */
    private Map<String, Object> alertDetail;

    /** 觸發指標 */
    private String triggerIndicator;

    /** 觸發值 */
    private BigDecimal triggerValue;

    /** 門檻值 */
    private BigDecimal thresholdValue;

    /** 警示狀態 */
    private String alertStatus;
}
