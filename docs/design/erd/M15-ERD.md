# M15-警報通知系統 ERD

> **文件編號**: ERD-M15
> **模組名稱**: 警報通知系統 (Alert Notification System)
> **版本**: v1.0
> **最後更新**: 2026-01-15
> **狀態**: Draft

---

## 1. 實體關聯圖

```mermaid
erDiagram
    %% ==================================================
    %% M15 警報通知系統 核心實體
    %% ==================================================

    alert_rules {
        bigserial id PK
        varchar rule_id UK "規則識別碼"
        varchar user_id "用戶ID"
        varchar rule_name "規則名稱"
        varchar rule_type "規則類型"
        boolean enabled "是否啟用"
        jsonb conditions "觸發條件"
        varchar condition_summary "條件摘要"
        jsonb notification_channels "通知管道"
        varchar notification_priority "優先級"
        integer throttle_max_per_day "每日上限"
        integer throttle_cooldown_minutes "冷卻時間"
        integer triggered_count "觸發次數"
        timestamp last_triggered_at "最後觸發時間"
        timestamp created_at
        timestamp updated_at
    }

    alert_history {
        bigserial id PK
        varchar alert_id UK "警報識別碼"
        varchar rule_id FK "規則ID"
        varchar user_id "用戶ID"
        varchar alert_type "警報類型"
        varchar stock_id "股票代碼"
        varchar stock_name "股票名稱"
        varchar signal_id FK "信號ID"
        varchar signal_direction "信號方向"
        varchar signal_grade "信號評級"
        decimal signal_score "信號評分"
        varchar signal_summary "信號摘要"
        decimal price "現價"
        decimal price_change "漲跌"
        decimal price_change_pct "漲跌幅"
        timestamp triggered_at "觸發時間"
        boolean is_read "是否已讀"
        timestamp read_at "已讀時間"
        timestamp created_at
    }

    notification_logs {
        bigserial id PK
        varchar log_id UK "日誌識別碼"
        varchar alert_id FK "警報ID"
        varchar user_id "用戶ID"
        varchar channel "通知管道"
        varchar status "發送狀態"
        varchar recipient "接收者"
        varchar message_preview "訊息預覽"
        varchar error_message "錯誤訊息"
        integer retry_count "重試次數"
        timestamp sent_at "發送時間"
        timestamp created_at
        timestamp updated_at
    }

    user_notification_settings {
        bigserial id PK
        varchar user_id UK "用戶ID"
        boolean email_enabled "Email啟用"
        varchar email_address "Email地址"
        boolean email_verified "Email已驗證"
        boolean line_enabled "Line啟用"
        varchar line_token "Line Token"
        varchar line_display_name "Line名稱"
        timestamp line_connected_at "Line連接時間"
        boolean push_enabled "Push啟用"
        boolean mute_enabled "靜音啟用"
        time quiet_hours_start "靜音開始"
        time quiet_hours_end "靜音結束"
        varchar quiet_timezone "時區"
        boolean weekend_mute "週末靜音"
        integer daily_limit_email "Email日限"
        integer daily_limit_line "Line日限"
        integer daily_limit_push "Push日限"
        boolean batch_enabled "批次啟用"
        integer batch_window_minutes "批次視窗"
        integer batch_threshold "批次門檻"
        timestamp created_at
        timestamp updated_at
    }

    user_devices {
        bigserial id PK
        varchar user_id FK "用戶ID"
        varchar device_id UK "裝置ID"
        varchar fcm_token "FCM Token"
        varchar platform "平台"
        varchar device_name "裝置名稱"
        varchar app_version "APP版本"
        boolean is_active "是否活躍"
        timestamp last_active_at "最後活躍"
        timestamp created_at
        timestamp updated_at
    }

    notification_templates {
        bigserial id PK
        varchar template_id UK "範本識別碼"
        varchar template_name "範本名稱"
        varchar template_type "範本類型"
        varchar channel "通知管道"
        varchar subject_template "主旨範本"
        text body_template "內容範本"
        boolean is_default "是否預設"
        boolean is_active "是否啟用"
        timestamp created_at
        timestamp updated_at
    }

    daily_notification_counts {
        bigserial id PK
        varchar user_id "用戶ID"
        date count_date "計數日期"
        varchar channel "通知管道"
        integer notification_count "通知數量"
        timestamp created_at
        timestamp updated_at
    }

    %% ==================================================
    %% 關聯定義
    %% ==================================================

    alert_rules ||--o{ alert_history : "triggers"
    alert_history ||--o{ notification_logs : "generates"
    user_notification_settings ||--o{ user_devices : "has"
    user_notification_settings ||--o{ daily_notification_counts : "tracks"
```

---

## 2. 表格關係矩陣

| 主表 | 關聯表 | 關係類型 | 外鍵欄位 | 說明 |
|-----|-------|---------|---------|------|
| alert_rules | alert_history | 1:N | rule_id | 規則觸發的警報 |
| alert_history | notification_logs | 1:N | alert_id | 警報的通知日誌 |
| user_notification_settings | user_devices | 1:N | user_id | 用戶的裝置 |
| user_notification_settings | daily_notification_counts | 1:N | user_id | 每日通知計數 |

---

## 3. 跨模組關聯

```mermaid
erDiagram
    %% ==================================================
    %% M15 與其他模組關聯
    %% ==================================================

    M13_unified_signals {
        varchar signal_id PK
        varchar stock_id FK
        date trade_date
        varchar unified_direction
        varchar grade
        decimal unified_score
        varchar signal_summary
    }

    M06_stocks {
        varchar stock_id PK
        varchar stock_name
        varchar market
    }

    M06_stock_daily {
        varchar stock_id FK
        date trade_date
        decimal close_price
        decimal price_change_pct
    }

    M15_alert_rules {
        varchar rule_id PK
        varchar user_id
        jsonb conditions
        boolean enabled
    }

    M15_alert_history {
        varchar alert_id PK
        varchar rule_id FK
        varchar stock_id FK
        varchar signal_id FK
        timestamp triggered_at
    }

    M15_notification_logs {
        varchar log_id PK
        varchar alert_id FK
        varchar channel
        varchar status
    }

    %% 跨模組關聯
    M13_unified_signals ||--o{ M15_alert_history : "triggers"
    M06_stocks ||--o{ M15_alert_history : "about"
    M06_stock_daily ||--o{ M15_alert_history : "price_data"
    M15_alert_rules ||--o{ M15_alert_history : "from_rule"
    M15_alert_history ||--o{ M15_notification_logs : "notified_via"
```

---

## 4. 跨模組依賴說明

| 來源模組 | 依賴說明 | 用途 |
|---------|---------|------|
| M13 | unified_signals | 信號觸發來源 |
| M06 | stocks | 股票基本資料（名稱、市場） |
| M06 | stock_daily | 即時價格資料（通知內容） |

---

## 5. 資料流說明

### 5.1 警報觸發流程

```
┌─────────────────────────────────────────────────────────────────────┐
│                        警報觸發資料流                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌─────────────────────┐                                            │
│  │  M13 信號引擎       │ ◄── 信號來源                                │
│  │  unified_signals    │                                            │
│  └──────────┬──────────┘                                            │
│             │ 新信號產生                                             │
│             ▼                                                        │
│  ┌─────────────────────┐                                            │
│  │  alert_rules        │ ◄── 比對用戶規則                           │
│  │  (enabled = true)   │                                            │
│  └──────────┬──────────┘                                            │
│             │ 條件符合                                               │
│             ▼                                                        │
│  ┌─────────────────────┐                                            │
│  │  alert_history      │ ◄── 建立警報記錄                           │
│  └──────────┬──────────┘                                            │
│             │                                                        │
│    ┌────────┼────────────────────────────┐                          │
│    │        │                            │                          │
│    ▼        ▼                            ▼                          │
│  ┌──────┐ ┌──────┐                 ┌──────────────┐                 │
│  │Email │ │Line  │                 │ notification │                 │
│  │發送  │ │發送  │ ...             │ _logs        │                 │
│  └──────┘ └──────┘                 └──────────────┘                 │
│                                    ◄── 記錄發送狀態                 │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### 5.2 通知派發流程

```
┌─────────────────────────────────────────────────────────────────────┐
│                        通知派發資料流                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌─────────────────────┐                                            │
│  │  alert_history      │ ◄── 待發送警報                             │
│  └──────────┬──────────┘                                            │
│             │                                                        │
│             ▼                                                        │
│  ┌─────────────────────┐                                            │
│  │ user_notification   │ ◄── 檢查用戶設定                           │
│  │ _settings           │                                            │
│  └──────────┬──────────┘                                            │
│             │                                                        │
│    ┌────────┼────────────────────────────┐                          │
│    │        │                            │                          │
│    ▼        ▼                            ▼                          │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                  │
│  │ 檢查靜音    │  │ 檢查每日   │  │ 檢查管道    │                  │
│  │ 時段       │  │ 限額       │  │ 啟用狀態    │                  │
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘                  │
│         │                │                │                         │
│         └────────────────┼────────────────┘                         │
│                          │                                          │
│                          ▼ 通過檢查                                 │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    通知範本渲染                                │   │
│  │  notification_templates + alert data                          │   │
│  └──────────────────────────┬───────────────────────────────────┘   │
│                             │                                       │
│         ┌───────────────────┼───────────────────┐                   │
│         ▼                   ▼                   ▼                   │
│    ┌─────────┐        ┌─────────┐        ┌─────────┐               │
│    │  Email  │        │  Line   │        │  FCM    │               │
│    │  Sender │        │  Sender │        │  Sender │               │
│    └────┬────┘        └────┬────┘        └────┬────┘               │
│         │                  │                  │                     │
│         └──────────────────┼──────────────────┘                     │
│                            │                                        │
│                            ▼                                        │
│                   ┌─────────────────┐                               │
│                   │ notification    │                               │
│                   │ _logs           │ ◄── 記錄發送結果              │
│                   └─────────────────┘                               │
│                            │                                        │
│                            ▼                                        │
│                   ┌─────────────────┐                               │
│                   │ daily_          │ ◄── 更新每日計數              │
│                   │ notification    │                               │
│                   │ _counts         │                               │
│                   └─────────────────┘                               │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 6. 索引設計摘要

| 表格 | 索引名稱 | 欄位 | 用途 |
|-----|---------|------|------|
| alert_rules | idx_rules_user | user_id, enabled | 用戶規則查詢 |
| alert_rules | idx_rules_type | rule_type, enabled | 類型篩選 |
| alert_history | idx_history_user_date | user_id, triggered_at | 歷史分頁 |
| alert_history | idx_history_user_unread | user_id, is_read | 未讀計數 |
| notification_logs | idx_logs_alert | alert_id | 警報日誌查詢 |
| notification_logs | idx_logs_pending | status | 待發送查詢 |
| user_devices | idx_devices_user | user_id, is_active | 用戶裝置 |
| daily_notification_counts | idx_daily_count | user_id, count_date | 限額查詢 |

---

## 7. 相關文檔

- [M15 功能需求](../../specs/functional/M15-警報通知系統功能需求.md)
- [M15 API 規格](../../specs/api/M15-API規格.md)
- [M15 資料庫設計](../M15-資料庫設計.md)

---

**文件維護者**: 後端工程師
**最後更新**: 2026-01-15
**下次審核**: 2026-04-15
