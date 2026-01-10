package com.chris.fin_shark.common.dto.job;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Job 狀態統計資料傳輸物件
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobStatusDTO {

    /** 執行中的 Job 數量 */
    @JsonProperty("running_jobs")
    private Integer runningJobs;

    /** 今日成功 Job 數量 */
    @JsonProperty("today_success_jobs")
    private Integer todaySuccessJobs;

    /** 今日失敗 Job 數量 */
    @JsonProperty("today_failed_jobs")
    private Integer todayFailedJobs;

    /** 執行中的 Job 列表 */
    @JsonProperty("running_job_list")
    private List<RunningJobInfo> runningJobList;

    /**
     * 執行中的 Job 資訊
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RunningJobInfo {
        /** 執行 ID */
        @JsonProperty("execution_id")
        private Long executionId;

        /** Job 名稱 */
        @JsonProperty("job_name")
        private String jobName;

        /** 開始時間 */
        @JsonProperty("start_time")
        private String startTime;

        /** 已處理筆數 */
        @JsonProperty("processed_items")
        private Integer processedItems;
    }
}
