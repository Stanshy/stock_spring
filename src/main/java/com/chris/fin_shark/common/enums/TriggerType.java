package com.chris.fin_shark.common.enums;

import lombok.Getter;

/**
 * Job 觸發類型列舉
 *
 * 遵守總綱 4.5.2 Job 執行記錄表規範
 *
 * @author chris
 * @since 2025-12-24
 */
@Getter
public enum TriggerType {

    /**
     * 排程觸發
     * 由系統定時排程自動觸發（Cron）
     */
    SCHEDULED("SCHEDULED", "排程觸發"),

    /**
     * 手動觸發
     * 由使用者或管理員手動觸發
     */
    MANUAL("MANUAL", "手動觸發"),



    /**
     * 事件觸發
     * 由系統事件觸發（如資料更新完成）
     */
    EVENT("EVENT", "事件觸發"),

    /**
     * 重試觸發
     * 由系統自動重試失敗的 Job
     */
    RETRY("RETRY", "重試觸發");

    /**
     * 觸發類型代碼
     */
    private final String code;

    /**
     * 觸發類型描述
     */
    private final String description;

    /**
     * 建構子
     */
    TriggerType(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
