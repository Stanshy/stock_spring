package com.chris.fin_shark.m08.domain;

import com.chris.fin_shark.m08.enums.AltmanStatus;
import com.chris.fin_shark.m08.enums.BeneishStatus;
import com.chris.fin_shark.m08.enums.CompositeGrade;
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
 * 財務綜合評分實體
 * <p>
 * 對應資料表: financial_scores
 * 使用自增主鍵 + UNIQUE 約束
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Entity
@Table(name = "financial_scores",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_financial_scores",
                        columnNames = {"stock_id", "year", "quarter"}
                )
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialScore {

    // ========== 主鍵 ==========

    /** 評分 ID（自增主鍵） */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "score_id")
    private Long scoreId;

    // ========== 業務主鍵（UNIQUE 約束） ==========

    /** 股票代碼 */
    @Column(name = "stock_id", length = 10, nullable = false)
    private String stockId;

    /** 年度 */
    @Column(name = "year", nullable = false)
    private Integer year;

    /** 季度 */
    @Column(name = "quarter", nullable = false)
    private Integer quarter;

    // ========== 評分資訊 ==========

    /** 計算日期 */
    @Column(name = "calculation_date", nullable = false)
    private LocalDate calculationDate;

    // ========== Piotroski F-Score (0-9分) ==========

    /** Piotroski F-Score 總分 (0-9) */
    @Column(name = "piotroski_f_score")
    private Integer piotroskiFScore;

    /** Piotroski 詳細分數 (JSONB) */
    @Column(name = "piotroski_details", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> piotroskiDetails;

    // ========== Altman Z-Score ==========

    /** Altman Z-Score */
    @Column(name = "altman_z_score", precision = 10, scale = 2)
    private BigDecimal altmanZScore;

    /** Altman 狀態（SAFE/GREY/DISTRESS） */

    @Enumerated(EnumType.STRING)
    @Column(name = "altman_status", length = 20)
    private AltmanStatus altmanStatus;

    /** Altman 詳細資訊 (JSONB) */
    @Column(name = "altman_details", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> altmanDetails;

    // ========== Beneish M-Score ==========

    /** Beneish M-Score（盈餘操縱偵測） */
    @Column(name = "beneish_m_score", precision = 10, scale = 2)
    private BigDecimal beneishMScore;

    /** Beneish 狀態（CLEAN/WARNING/MANIPULATOR） */
    // 🔴 改動：改用 Enum
    @Enumerated(EnumType.STRING)
    @Column(name = "beneish_status", length = 20)
    private BeneishStatus beneishStatus;

    /** Beneish 詳細資訊 (JSONB) */
    @Column(name = "beneish_details", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> beneishDetails;

    // ========== Graham Score (0-10分) ==========

    /** Graham 評分 (0-10) */
    @Column(name = "graham_score")
    private Integer grahamScore;

    /** Graham 詳細資訊 (JSONB) */
    @Column(name = "graham_details", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> grahamDetails;

    // ========== 綜合評分 (0-100分) ==========

    /** 綜合評分 (0-100) */
    @Column(name = "composite_score", precision = 5, scale = 2)
    private BigDecimal compositeScore;

    /** 綜合評級（A+, A, B+, B, C+, C, D, F） */

    @Enumerated(EnumType.STRING)
    @Column(name = "composite_grade", length = 5)
    private CompositeGrade compositeGrade;

    // ========== 時間戳 ==========

    /** 建立時間 */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 更新時間 */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


}
