# M13-信號判斷引擎 ERD

> **文件編號**: ERD-M13
> **模組名稱**: 信號判斷引擎 (Signal Judgment Engine)
> **版本**: v1.0
> **最後更新**: 2026-01-14
> **狀態**: Draft

---

## 1. 實體關聯圖

```mermaid
erDiagram
    %% ========================================
    %% M13 信號判斷引擎 ERD
    %% ========================================

    %% 原始信號表
    raw_signals {
        bigint id PK
        varchar raw_signal_id UK "原始信號 ID"
        varchar source_signal_id "上游信號 ID"
        varchar source_module "來源模組 M07-M12"
        varchar signal_type "信號類型"
        varchar signal_code "信號代碼"
        varchar signal_name "信號名稱"
        varchar stock_id FK "股票代碼"
        date signal_date "信號日期"
        varchar signal_direction "方向 BUY/SELL/HOLD"
        decimal source_confidence "來源信心度"
        jsonb signal_metadata "信號元資料"
        varchar dedup_status "去重狀態"
        varchar unified_signal_id FK "統一信號 ID"
        varchar batch_id FK "批次 ID"
        timestamp collected_at "收集時間"
        timestamp created_at "建立時間"
    }

    %% 統一信號表
    unified_signals {
        bigint id PK
        varchar signal_id UK "統一信號 ID"
        varchar stock_id FK "股票代碼"
        varchar stock_name "股票名稱"
        varchar sector_code "產業代碼"
        date trade_date "交易日期"
        varchar unified_direction "統一方向"
        varchar direction_strength "方向強度"
        decimal unified_score "統一評分"
        varchar grade "評級 A+/A/B+/B/C/D"
        int dimension_coverage "維度覆蓋數"
        decimal unified_confidence "統一信心度"
        varchar signal_types "信號類型標籤"
        varchar validity_period "有效期間"
        text key_factors "關鍵因素"
        varchar source_modules "來源模組"
        int contributing_count "組成信號數"
        boolean is_consumed "是否已消費"
        varchar batch_id FK "批次 ID"
        timestamp created_at "建立時間"
        timestamp updated_at "更新時間"
    }

    %% 信號組成明細表
    signal_contributors {
        bigint id PK
        varchar unified_signal_id FK "統一信號 ID"
        varchar raw_signal_id FK "原始信號 ID"
        varchar source_module "來源模組"
        varchar signal_type "信號類型"
        varchar signal_code "信號代碼"
        varchar signal_name "信號名稱"
        varchar signal_direction "信號方向"
        decimal source_confidence "來源信心度"
        decimal weight "權重"
        decimal weighted_confidence "加權信心度"
        timestamp created_at "建立時間"
    }

    %% 去重處理記錄表
    signal_dedup_log {
        bigint id PK
        varchar batch_id FK "批次 ID"
        date trade_date "交易日期"
        varchar stock_id FK "股票代碼"
        varchar dedup_type "去重類型"
        text original_signal_ids "原始信號 IDs"
        varchar result_action "結果動作"
        varchar kept_signal_id "保留信號 ID"
        text removed_signal_ids "移除信號 IDs"
        varchar merge_reason "合併原因"
        timestamp created_at "建立時間"
    }

    %% 評分計算明細表
    signal_scoring_details {
        bigint id PK
        varchar unified_signal_id FK,UK "統一信號 ID"
        decimal strength_score "強度分數"
        decimal strength_weight "強度權重"
        decimal strength_weighted "加權強度"
        decimal direction_score "方向分數"
        decimal confidence_score "信心度分數"
        decimal coverage_score "覆蓋分數"
        decimal coverage_weight "覆蓋權重"
        decimal coverage_weighted "加權覆蓋"
        decimal coverage_base_score "覆蓋基礎分"
        decimal coverage_bonus "覆蓋加分"
        decimal historical_score "歷史績效分"
        decimal historical_weight "歷史權重"
        decimal historical_weighted "加權歷史"
        decimal historical_accuracy "歷史準確率"
        int historical_sample_count "歷史樣本數"
        decimal market_score "市場環境分"
        decimal market_weight "市場權重"
        decimal market_weighted "加權市場"
        varchar market_trend "市場趨勢"
        varchar sector_strength "產業強度"
        decimal freshness_score "時效分數"
        decimal freshness_weight "時效權重"
        decimal freshness_weighted "加權時效"
        int signal_age_hours "信號年齡小時"
        decimal base_total_score "基礎總分"
        jsonb adjustment_factors "調整因素"
        decimal final_score "最終分數"
        varchar grade "評級"
        timestamp created_at "建立時間"
    }

    %% 信號消費記錄表
    signal_consumption_log {
        bigint id PK
        varchar unified_signal_id FK "統一信號 ID"
        varchar consumer_module "消費模組 M14-M18"
        varchar consumer_action "消費動作"
        varchar notes "備註"
        timestamp consumed_at "消費時間"
        timestamp created_at "建立時間"
    }

    %% 信號訂閱配置表
    signal_subscriptions {
        bigint id PK
        varchar subscription_id UK "訂閱 ID"
        varchar subscription_name "訂閱名稱"
        varchar consumer_module "消費模組"
        jsonb conditions "訂閱條件"
        varchar notification_channel "通知管道"
        varchar webhook_url "Webhook URL"
        boolean is_active "是否啟用"
        int triggered_count "觸發次數"
        timestamp last_triggered_at "最後觸發時間"
        timestamp created_at "建立時間"
        timestamp updated_at "更新時間"
    }

    %% 每日推薦清單表
    daily_recommendations {
        bigint id PK
        date trade_date "交易日期"
        varchar direction "方向 BUY/SELL"
        int rank "排名"
        varchar unified_signal_id FK "統一信號 ID"
        varchar stock_id FK "股票代碼"
        varchar stock_name "股票名稱"
        varchar sector_name "產業名稱"
        decimal unified_score "統一評分"
        varchar grade "評級"
        varchar direction_strength "方向強度"
        int dimension_coverage "維度覆蓋"
        text key_factors "關鍵因素"
        varchar signal_types "信號類型"
        timestamp generated_at "產生時間"
        timestamp created_at "建立時間"
    }

    %% 語義信號群組定義表
    signal_semantic_groups {
        bigint id PK
        varchar group_code UK "群組代碼"
        varchar group_name "群組名稱"
        text signal_codes "信號代碼列表"
        varchar merge_strategy "合併策略"
        varchar description "描述"
        boolean is_active "是否啟用"
        timestamp created_at "建立時間"
        timestamp updated_at "更新時間"
    }

    %% 批次處理記錄表
    signal_processing_batch {
        bigint id PK
        varchar batch_id UK "批次 ID"
        date trade_date "交易日期"
        varchar batch_type "批次類型"
        varchar status "處理狀態"
        timestamp collection_started_at "收集開始"
        timestamp collection_completed_at "收集完成"
        int raw_signals_collected "收集原始信號數"
        timestamp dedup_started_at "去重開始"
        timestamp dedup_completed_at "去重完成"
        int signals_after_dedup "去重後信號數"
        timestamp merge_started_at "合併開始"
        timestamp merge_completed_at "合併完成"
        int unified_signals_created "建立統一信號數"
        timestamp scoring_started_at "評分開始"
        timestamp scoring_completed_at "評分完成"
        timestamp publish_started_at "發布開始"
        timestamp publish_completed_at "發布完成"
        bigint total_execution_time_ms "總執行時間"
        text error_message "錯誤訊息"
        timestamp created_at "建立時間"
        timestamp updated_at "更新時間"
    }

    %% ========================================
    %% 關聯定義
    %% ========================================

    %% 原始信號 -> 統一信號 (多對一)
    raw_signals ||--o{ unified_signals : "合併為"

    %% 原始信號 -> 批次 (多對一)
    raw_signals }o--|| signal_processing_batch : "屬於批次"

    %% 統一信號 -> 信號組成 (一對多)
    unified_signals ||--o{ signal_contributors : "包含"

    %% 統一信號 -> 評分明細 (一對一)
    unified_signals ||--|| signal_scoring_details : "評分"

    %% 統一信號 -> 消費記錄 (一對多)
    unified_signals ||--o{ signal_consumption_log : "被消費"

    %% 統一信號 -> 每日推薦 (一對多)
    unified_signals ||--o{ daily_recommendations : "推薦"

    %% 統一信號 -> 批次 (多對一)
    unified_signals }o--|| signal_processing_batch : "屬於批次"

    %% 去重記錄 -> 批次 (多對一)
    signal_dedup_log }o--|| signal_processing_batch : "屬於批次"
```

---

## 2. 關聯說明

### 2.1 核心關聯

| 關聯 | 類型 | 說明 |
|-----|------|------|
| raw_signals → unified_signals | 多對一 | 多個原始信號合併為一個統一信號 |
| unified_signals → signal_contributors | 一對多 | 一個統一信號由多個原始信號組成 |
| unified_signals → signal_scoring_details | 一對一 | 每個統一信號有一份評分明細 |
| unified_signals → signal_consumption_log | 一對多 | 一個信號可被多個模組消費 |
| unified_signals → daily_recommendations | 一對多 | 一個信號可出現在多天推薦清單 |

### 2.2 批次關聯

| 關聯 | 類型 | 說明 |
|-----|------|------|
| signal_processing_batch → raw_signals | 一對多 | 一個批次收集多個原始信號 |
| signal_processing_batch → unified_signals | 一對多 | 一個批次產生多個統一信號 |
| signal_processing_batch → signal_dedup_log | 一對多 | 一個批次有多條去重記錄 |

---

## 3. 信號處理流程圖

```mermaid
flowchart TB
    subgraph Collection["收集階段"]
        M07[(M07 技術信號)]
        M08[(M08 基本面信號)]
        M09[(M09 籌碼信號)]
        M10[(M10 型態信號)]
        M11[(M11 策略信號)]
        M12[(M12 產業信號)]
        RAW[(raw_signals)]

        M07 --> RAW
        M08 --> RAW
        M09 --> RAW
        M10 --> RAW
        M11 --> RAW
        M12 --> RAW
    end

    subgraph Deduplication["去重階段"]
        DEDUP[去重處理器]
        SEMANTIC[(signal_semantic_groups)]
        LOG[(signal_dedup_log)]

        RAW --> DEDUP
        SEMANTIC -.-> DEDUP
        DEDUP --> LOG
    end

    subgraph Merge["合併階段"]
        MERGER[信號合併器]
        UNIFIED[(unified_signals)]
        CONTRIB[(signal_contributors)]

        DEDUP --> MERGER
        MERGER --> UNIFIED
        MERGER --> CONTRIB
    end

    subgraph Scoring["評分階段"]
        SCORER[信號評分器]
        SCORING[(signal_scoring_details)]

        UNIFIED --> SCORER
        SCORER --> SCORING
        SCORER --> |更新評分| UNIFIED
    end

    subgraph Publishing["發布階段"]
        PUBLISHER[信號發布器]
        RECO[(daily_recommendations)]
        SUBS[(signal_subscriptions)]
        CONSUME[(signal_consumption_log)]

        UNIFIED --> PUBLISHER
        PUBLISHER --> RECO
        SUBS -.-> PUBLISHER
        PUBLISHER --> |通知下游| M14[M14 選股]
        PUBLISHER --> |通知下游| M15[M15 警報]
        PUBLISHER --> |通知下游| M16[M16 回測]
        PUBLISHER --> |通知下游| M17[M17 風險]
        PUBLISHER --> |通知下游| M18[M18 投組]

        M14 --> CONSUME
        M15 --> CONSUME
        M16 --> CONSUME
        M17 --> CONSUME
        M18 --> CONSUME
    end

    BATCH[(signal_processing_batch)]
    BATCH -.-> Collection
    BATCH -.-> Deduplication
    BATCH -.-> Merge
    BATCH -.-> Scoring
    BATCH -.-> Publishing
```

---

## 4. 資料流向圖

```mermaid
flowchart LR
    subgraph Upstream["上游模組 (信號來源)"]
        direction TB
        M07[M07 技術分析]
        M08[M08 基本面]
        M09[M09 籌碼分析]
        M10[M10 型態識別]
        M11[M11 量化策略]
        M12[M12 總經產業]
    end

    subgraph M13["M13 信號引擎"]
        direction TB
        RAW[raw_signals<br/>原始信號]
        UNI[unified_signals<br/>統一信號]
        SCORE[signal_scoring_details<br/>評分明細]
        RECO[daily_recommendations<br/>推薦清單]

        RAW --> |去重合併| UNI
        UNI --> |評分| SCORE
        UNI --> |排序| RECO
    end

    subgraph Downstream["下游模組 (信號消費)"]
        direction TB
        M14[M14 選股引擎]
        M15[M15 警報通知]
        M16[M16 回測系統]
        M17[M17 風險管理]
        M18[M18 投組管理]
    end

    M07 --> RAW
    M08 --> RAW
    M09 --> RAW
    M10 --> RAW
    M11 --> RAW
    M12 --> RAW

    UNI --> M14
    UNI --> M15
    UNI --> M16
    UNI --> M17
    UNI --> M18
```

---

## 5. 表格關聯矩陣

| 表格 | raw | unified | contrib | dedup | scoring | consume | subs | reco | semantic | batch |
|-----|-----|---------|---------|-------|---------|---------|------|------|----------|-------|
| raw_signals | - | FK | - | - | - | - | - | - | - | FK |
| unified_signals | - | - | PK | - | PK | PK | - | PK | - | FK |
| signal_contributors | - | FK | - | - | - | - | - | - | - | - |
| signal_dedup_log | - | - | - | - | - | - | - | - | - | FK |
| signal_scoring_details | - | FK | - | - | - | - | - | - | - | - |
| signal_consumption_log | - | FK | - | - | - | - | - | - | - | - |
| signal_subscriptions | - | - | - | - | - | - | - | - | - | - |
| daily_recommendations | - | FK | - | - | - | - | - | - | - | - |
| signal_semantic_groups | - | - | - | - | - | - | - | - | - | - |
| signal_processing_batch | PK | PK | - | PK | - | - | - | - | - | - |

**圖例**: PK = 被參照, FK = 參照外表, - = 無直接關聯

---

## 6. 相關文檔

- [M13 功能需求](../../specs/functional/M13-信號引擎功能需求.md)
- [M13 API 規格](../../specs/api/M13-API規格.md)
- [M13 資料庫設計](../M13-資料庫設計.md)

---

**文件維護者**: 後端工程師
**最後更新**: 2026-01-14
**下次審核**: 2026-04-14
