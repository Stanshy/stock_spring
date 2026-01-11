package com.chris.fin_shark.m06.job;

import com.chris.fin_shark.common.enums.TriggerType;
import com.chris.fin_shark.m06.service.DataQualityExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 資料品質檢核排程 Job
 * <p>
 * 功能編號: F-M06-008
 * 每日定時執行資料品質檢核
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DataQualityCheckJob {

    private final DataQualityExecutionService dataQualityExecutionService;

    /**
     * 定時執行資料品質檢核
     * <p>
     * 執行時間：每日 06:00
     * Cron 表達式：秒 分 時 日 月 週
     * </p>
     */
    @Scheduled(cron = "0 0 6 * * ?")
    public void runDailyQualityCheck() {
        log.info("排程觸發：資料品質檢核 Job");

        dataQualityExecutionService.runScheduledQualityCheck(TriggerType.SCHEDULED);
    }

    /**
     * 手動觸發資料品質檢核
     * <p>
     * 提供給 Controller 呼叫
     * </p>
     */
    public void runQualityCheckManually() {
        log.info("手動觸發：資料品質檢核 Job");

        dataQualityExecutionService.runScheduledQualityCheck(TriggerType.MANUAL);
    }
}
