package com.chris.fin_shark.m06.job;

import com.chris.fin_shark.common.enums.TriggerType;
import com.chris.fin_shark.m06.service.FinancialStatementSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 財務報表同步排程 Job
 * <p>
 * 功能編號: F-M06-008
 * 每月定時執行財報資料同步
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FinancialStatementSyncJob {

    private final FinancialStatementSyncService financialStatementSyncService;

    /**
     * 定時執行財報同步
     * <p>
     * 執行時間：每月 15 日 09:00
     * Cron 表達式：秒 分 時 日 月 週
     * </p>
     */
    @Scheduled(cron = "0 0 9 15 * ?")
    public void syncMonthlyFinancialStatements() {
        log.info("排程觸發：財報同步 Job");

        // 計算應同步的年度季度
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        short quarter = (short) ((today.getMonthValue() - 1) / 3);

        // 如果是 Q1，則同步上一年度 Q4
        if (quarter == 0) {
            year--;
            quarter = 4;
        }

        financialStatementSyncService.syncFinancialStatementsForPeriod(year, quarter, TriggerType.SCHEDULED);
    }

    /**
     * 手動觸發財報同步
     * <p>
     * 提供給 Controller 呼叫，用於手動補齊資料
     * </p>
     *
     * @param year    年度
     * @param quarter 季度
     */
    public void syncFinancialStatementsManually(int year, short quarter) {
        log.info("手動觸發：財報同步 Job, year={}, quarter={}", year, quarter);

        financialStatementSyncService.syncFinancialStatementsForPeriod(year, quarter, TriggerType.MANUAL);
    }
}
