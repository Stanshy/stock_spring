package com.chris.fin_shark.common.dto.job;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Job 狀態統計資料傳輸物件
 * <p>
 * 用於查詢 Job 執行狀態總覽
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobStatusDTO {

    /**
     * 執行中的 Job 數量
     */
    @JsonProperty("running_jobs")
    private Integer runningJobs;

    /**
     * 今日成功 Job 數量
     */
    @JsonProperty("today_success_jobs")
    private Integer todaySuccessJobs;

    /**
     * 今日失敗 Job 數量
     */
    @JsonProperty("today_failed_jobs")
    private Integer todayFailedJobs;

    /**
     * 今日待執行 Job 數量
     */
    @JsonProperty("today_pending_jobs")
    private Integer todayPendingJobs;

    /**
     * 執行中的 Job 列表
     */
    @JsonProperty("running_job_list")
    private List<RunningJobInfo> runningJobList;

    /**
     * 執行中的 Job 資訊（簡化版）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RunningJobInfo {

        /**
         * 執行 ID
         */
        @JsonProperty("execution_id")
        private Long executionId;

        /**
         * Job 名稱
         */
        @JsonProperty("job_name")
        private String jobName;

        /**
         * Job 類型
         */
        @JsonProperty("job_type")
        private String jobType;

        /**
         * 開始時間
         */
        @JsonProperty("start_time")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;

        /**
         * 已處理筆數
         */
        @JsonProperty("processed_items")
        private Integer processedItems;

        /**
         * 總筆數
         */
        @JsonProperty("total_items")
        private Integer totalItems;

        /**
         * 處理進度（百分比）
         */
        @JsonProperty("progress")
        public Double getProgress() {
            if (totalItems == null || totalItems == 0) {
                return 0.0;
            }
            if (processedItems == null) {
                return 0.0;
            }
            return (processedItems * 100.0) / totalItems;
        }
    }
}