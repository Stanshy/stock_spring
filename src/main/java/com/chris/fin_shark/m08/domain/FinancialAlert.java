package com.chris.fin_shark.m08.domain;

import com.chris.fin_shark.m08.enums.AlertCategory;
import com.chris.fin_shark.m08.enums.AlertStatus;
import com.chris.fin_shark.m08.enums.Severity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 財務異常警示實體
 * <p>
 * 對應資料表: financial_alerts
 * 記錄財務指標異常、盈餘品質問題等警示
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Entity
@Table(name = "financial_alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialAlert {

    // ========== 主鍵 ==========

    /** 警示 ID（自增主鍵） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Long alertId;

    // ========== 基本資訊 ==========

    /** 股票代碼 */
    @Column(name = "stock_id", length = 10, nullable = false)
    private String stockId;

    /** 年度 */
    @Column(name = "year", nullable = false)
    private Integer year;

    /** 季度 */
    @Column(name = "quarter", nullable = false)
    private Integer quarter;

    // ========== 警示內容 ==========

    /** 警示類型（HIGH_DEBT_RATIO, EARNINGS_QUALITY 等） */
    @Column(name = "alert_type", length = 50, nullable = false)
    private String alertType;

    /** 警示類別（EARNINGS_QUALITY, DEBT_RISK, LIQUIDITY_RISK, PROFITABILITY_DECLINE, OTHER） */
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_category", length = 50, nullable = false)
    private AlertCategory alertCategory;

    /** 嚴重程度（LOW, MEDIUM, HIGH, CRITICAL） */
    @Enumerated(EnumType.STRING)
    @Column(name = "severity", length = 20, nullable = false)
    private Severity severity;

    /** 警示訊息 */
    @Column(name = "alert_message", columnDefinition = "TEXT")
    private String alertMessage;

    /** 警示詳細資訊 (JSONB) */
    @Column(name = "alert_detail", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> alertDetail;

    // ========== 觸發指標 ==========

    /** 觸發警示的指標名稱 */
    @Column(name = "trigger_indicator", length = 50)
    private String triggerIndicator;

    /** 觸發值 */
    @Column(name = "trigger_value", precision = 15, scale = 2)
    private BigDecimal triggerValue;

    /** 門檻值 */
    @Column(name = "threshold_value", precision = 15, scale = 2)
    private BigDecimal thresholdValue;

    // ========== 警示狀態 ==========

    /** 警示狀態（ACTIVE, RESOLVED, IGNORED） */
    @Enumerated(EnumType.STRING)
    @Column(name = "alert_status", length = 20, nullable = false)
    private AlertStatus alertStatus;

    /** 是否已通知 */
    @Column(name = "is_notified")
    private Boolean isNotified;

    /** 解決日期 */
    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    // ========== 時間戳 ==========

    /** 建立時間 */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 更新時間 */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;



}
