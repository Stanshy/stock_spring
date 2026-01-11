package com.chris.fin_shark.m06.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 品質檢核執行結果 Value Object
 * <p>
 * 用於品質檢核執行後的結果呈現
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityCheckExecutionVO {

    /** 股票代碼 */
    private String stockId;

    /** 交易日期 */
    private LocalDate tradeDate;

    /** 資料表名稱 */
    private String tableName;

    /** 檢核類型 */
    private String checkType;

    /** 問題描述 */
    private String issueDetail;

    /** 實際值 */
    private String actualValue;

    /** 預期值 */
    private String expectedValue;
}
