package com.chris.fin_shark.m07.service;

import com.chris.fin_shark.m07.repository.IndicatorCalculationJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class IndicatorCalculationAsyncExecutor {

    private final IndicatorCalculationJobRepository jobRepository;
    private final IndicatorCalculationService calculationService;

    @Async("asyncExecutor")
    public void executeByJobId(Long jobId) {
        log.info("🔔 非同步執行 Job: {}", jobId);

        jobRepository.findById(jobId).ifPresentOrElse(job -> {
            calculationService.calculateIndicators(
                    job.getCalculationDate(),
                    job.getStockList() != null ? List.of(job.getStockList()) : null,
                    job.getIndicatorPriority(),
                    Boolean.TRUE  // 或 job 裡面再多一個欄位控制
            );
        }, () -> {
            log.warn("找不到 Job: {}", jobId);
        });
    }

    //開發用
    @Async("asyncExecutor")
    public void backfillAsync(String stockId,
                              LocalDate startDate,
                              LocalDate endDate,
                              String priority,
                              boolean force) {
        calculationService.backfillIndicatorsForRange(
                stockId, startDate, endDate, priority, force
        );
    }

}