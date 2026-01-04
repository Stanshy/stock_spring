package com.chris.fin_shark.m07.controller;

import lombok.Data;

import java.time.LocalDate;
//開發用
@Data
public class IndicatorBackfillRequest {
    private String stockId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String indicatorPriority; // e.g. P0/P1，沒有就算全部
    private Boolean forceRecalculate; // 開發階段通常給 true
}
