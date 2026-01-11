package com.chris.fin_shark.m06.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 缺漏資料 Value Object
 * <p>
 * 用於資料品質檢核結果的缺漏資料呈現
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissingDataVO {

    /** 股票代碼 */
    private String stockId;

    /** 股票名稱 */
    private String stockName;

    /** 缺漏日期 */
    private LocalDate missingDate;

    /** 資料表名稱 */
    private String tableName;
}
