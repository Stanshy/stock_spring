package com.chris.fin_shark.m10.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * K 線型態結果複合主鍵
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KLinePatternResultId implements Serializable {

    private Long resultId;
    private LocalDate tradeDate;
}
