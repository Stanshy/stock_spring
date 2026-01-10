package com.chris.fin_shark.m08.enums;

import lombok.Getter;

/**
 * 綜合評級
 * <p>
 * 對應資料庫 CHECK 約束: composite_grade IN ('A+', 'A', 'B+', 'B', 'C+', 'C', 'D', 'F')
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Getter
public enum CompositeGrade {

    /**
     * A+ 級（優秀）
     */
    A_PLUS("A+", "優秀", 90, 100),

    /**
     * A 級（良好）
     */
    A("A", "良好", 80, 89),

    /**
     * B+ 級（中上）
     */
    B_PLUS("B+", "中上", 75, 79),

    /**
     * B 級（中等）
     */
    B("B", "中等", 70, 74),

    /**
     * C+ 級（中下）
     */
    C_PLUS("C+", "中下", 60, 69),

    /**
     * C 級（及格）
     */
    C("C", "及格", 50, 59),

    /**
     * D 級（不及格）
     */
    D("D", "不及格", 40, 49),

    /**
     * F 級（極差）
     */
    F("F", "極差", 0, 39);

    /**
     * 資料庫儲存值
     */
    private final String code;

    /**
     * 顯示名稱
     */
    private final String displayName;

    /**
     * 分數下限
     */
    private final int minScore;

    /**
     * 分數上限
     */
    private final int maxScore;

    CompositeGrade(String code, String displayName, int minScore, int maxScore) {
        this.code = code;
        this.displayName = displayName;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    /**
     * 根據 code 取得 Enum
     */
    public static CompositeGrade fromCode(String code) {
        for (CompositeGrade grade : values()) {
            if (grade.code.equals(code)) {
                return grade;
            }
        }
        throw new IllegalArgumentException("Unknown CompositeGrade code: " + code);
    }

    /**
     * 根據分數取得評級
     *
     * @param score 綜合評分（0-100）
     * @return 對應的評級
     */
    public static CompositeGrade fromScore(double score) {
        for (CompositeGrade grade : values()) {
            if (score >= grade.minScore && score <= grade.maxScore) {
                return grade;
            }
        }
        throw new IllegalArgumentException("Score out of range: " + score);
    }
}
