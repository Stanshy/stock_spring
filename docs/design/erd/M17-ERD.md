# M17-風險管理模組 ERD

> **文件編號**: ERD-M17
> **模組名稱**: 風險管理模組 (Risk Management Module)
> **版本**: v1.0
> **最後更新**: 2026-01-15
> **狀態**: Draft

---

## 1. ERD 總覽

```mermaid
erDiagram
    %% ===== 核心風險表 =====
    risk_snapshots {
        bigint id PK
        varchar portfolio_id FK
        date snapshot_date
        varchar risk_level
        int risk_score
        decimal var_95_daily
        decimal var_99_daily
        decimal cvar_95
        varchar var_method
        decimal volatility_daily
        decimal volatility_annualized
        decimal downside_volatility
        decimal beta
        decimal correlation_market
        varchar benchmark_code
        decimal max_drawdown
        decimal current_drawdown
        decimal hhi
        decimal top5_weight
        decimal largest_position_pct
        decimal sharpe_ratio
        decimal sortino_ratio
        jsonb risk_attribution
        jsonb sector_exposure
        jsonb top_risk_contributors
        decimal total_value
        int position_count
        timestamp calculated_at
        timestamp created_at
    }

    risk_var_results {
        bigint id PK
        varchar portfolio_id FK
        date calculation_date
        varchar method
        decimal confidence_level
        int horizon_days
        int lookback_days
        int simulations
        decimal var_value
        decimal var_percentage
        decimal cvar_value
        decimal cvar_percentage
        decimal standard_error
        jsonb component_var
        int backtest_exceedances
        decimal expected_exceedances
        decimal backtest_pvalue
        jsonb sensitivity_analysis
        jsonb distribution_stats
        decimal total_value
        timestamp calculated_at
        int calculation_time_ms
        timestamp created_at
    }

    %% ===== 限額管理表 =====
    risk_limits {
        bigint id PK
        varchar limit_id UK
        varchar portfolio_id FK
        varchar user_id FK
        varchar limit_type
        decimal limit_value
        varchar limit_unit
        decimal warning_threshold
        decimal critical_threshold
        varchar action_type
        text description
        boolean enabled
        decimal current_value
        decimal current_utilization
        varchar current_status
        timestamp last_checked_at
        timestamp created_at
        timestamp updated_at
    }

    risk_limit_checks {
        bigint id PK
        varchar limit_id FK
        varchar portfolio_id FK
        date check_date
        varchar limit_type
        decimal limit_value
        decimal current_value
        decimal utilization
        varchar status
        decimal warning_threshold
        decimal critical_threshold
        decimal previous_value
        decimal value_change
        decimal value_change_pct
        boolean alert_triggered
        varchar alert_id FK
        timestamp checked_at
    }

    %% ===== 預警管理表 =====
    risk_alert_rules {
        bigint id PK
        varchar rule_id UK
        varchar portfolio_id FK
        varchar user_id FK
        varchar alert_type
        varchar rule_name
        text description
        jsonb condition
        varchar severity
        jsonb notification_channels
        int cooldown_minutes
        boolean enabled
        int triggered_count
        timestamp last_triggered_at
        timestamp created_at
        timestamp updated_at
    }

    risk_alerts {
        bigint id PK
        varchar alert_id UK
        varchar alert_rule_id FK
        varchar portfolio_id FK
        varchar alert_type
        varchar severity
        varchar title
        text message
        decimal trigger_value
        decimal threshold_value
        varchar trigger_metric
        varchar stock_id
        varchar stock_name
        varchar status
        timestamp triggered_at
        timestamp acknowledged_at
        varchar acknowledged_by
        text acknowledge_note
        timestamp resolved_at
        varchar resolved_by
        text resolution_note
        timestamp created_at
        timestamp updated_at
    }

    %% ===== 壓力測試表 =====
    risk_stress_tests {
        bigint id PK
        varchar test_id UK
        varchar portfolio_id FK
        varchar user_id FK
        varchar test_name
        text[] scenario_ids
        jsonb custom_scenarios
        boolean include_historical
        varchar status
        int total_scenarios
        int completed_scenarios
        int scenarios_passed
        int scenarios_failed
        varchar worst_case_scenario
        decimal worst_case_loss
        decimal worst_case_pct
        decimal avg_loss
        decimal total_value
        timestamp started_at
        timestamp completed_at
        timestamp created_at
        timestamp updated_at
    }

    risk_stress_results {
        bigint id PK
        varchar test_id FK
        varchar scenario_id
        varchar scenario_name
        varchar scenario_type
        text scenario_description
        jsonb shocks
        decimal market_change
        decimal portfolio_change
        decimal estimated_loss
        decimal final_value
        jsonb position_impacts
        boolean passed
        text[] breached_limits
        timestamp created_at
    }

    risk_scenarios {
        bigint id PK
        varchar scenario_id UK
        varchar user_id FK
        varchar scenario_name
        text description
        varchar scenario_type
        jsonb shocks
        boolean is_public
        int usage_count
        timestamp last_used_at
        timestamp created_at
        timestamp updated_at
    }

    %% ===== 快取表 =====
    risk_correlation_cache {
        bigint id PK
        varchar portfolio_id FK
        date cache_date
        varchar period
        varchar method
        text[] stock_ids
        jsonb stock_names
        jsonb correlation_matrix
        decimal avg_correlation
        decimal max_correlation
        text[] max_correlation_pair
        decimal min_correlation
        text[] min_correlation_pair
        decimal diversification_ratio
        jsonb clusters
        timestamp expires_at
        timestamp calculated_at
        timestamp created_at
    }

    %% ===== 跨模組依賴 (外部表) =====
    M18_portfolios {
        varchar portfolio_id PK
        varchar portfolio_name
        varchar user_id
        decimal total_value
    }

    M18_portfolio_positions {
        varchar portfolio_id FK
        varchar stock_id FK
        int shares
        decimal market_value
        decimal weight
    }

    M06_stock_daily_prices {
        varchar stock_id PK
        date trade_date PK
        decimal close_price
        decimal daily_return
    }

    M07_technical_indicators {
        varchar stock_id FK
        date calc_date
        decimal volatility
        decimal beta
    }

    %% ===== 關聯定義 =====

    %% 風險快照與投組
    M18_portfolios ||--o{ risk_snapshots : "has"
    M18_portfolios ||--o{ risk_var_results : "has"

    %% 限額管理
    M18_portfolios ||--o{ risk_limits : "has"
    risk_limits ||--o{ risk_limit_checks : "checked_by"
    risk_limit_checks }o--o| risk_alerts : "triggers"

    %% 預警管理
    M18_portfolios ||--o{ risk_alert_rules : "has"
    risk_alert_rules ||--o{ risk_alerts : "generates"
    M18_portfolios ||--o{ risk_alerts : "has"

    %% 壓力測試
    M18_portfolios ||--o{ risk_stress_tests : "has"
    risk_stress_tests ||--o{ risk_stress_results : "contains"
    risk_scenarios }o--o{ risk_stress_results : "used_in"

    %% 相關性快取
    M18_portfolios ||--o{ risk_correlation_cache : "has"

    %% 跨模組數據來源
    M06_stock_daily_prices }o--|| risk_var_results : "source"
    M07_technical_indicators }o--|| risk_snapshots : "source"
    M18_portfolio_positions }o--|| risk_snapshots : "source"
```

---

## 2. 實體關聯說明

### 2.1 核心風險計算

| 來源表 | 目標表 | 關聯類型 | 說明 |
|-------|-------|---------|------|
| M18_portfolios | risk_snapshots | 1:N | 每個投組每日一筆風險快照 |
| M18_portfolios | risk_var_results | 1:N | 每個投組可有多種 VaR 計算結果 |
| M06_stock_daily_prices | risk_var_results | N:1 | VaR 計算依賴歷史價格 |
| M07_technical_indicators | risk_snapshots | N:1 | 風險快照參考技術指標 |

### 2.2 限額與預警

| 來源表 | 目標表 | 關聯類型 | 說明 |
|-------|-------|---------|------|
| risk_limits | risk_limit_checks | 1:N | 每個限額每日檢查一次 |
| risk_limit_checks | risk_alerts | 1:0..1 | 檢查可能觸發預警 |
| risk_alert_rules | risk_alerts | 1:N | 規則可觸發多個預警 |
| M18_portfolios | risk_alerts | 1:N | 投組可有多個預警 |

### 2.3 壓力測試

| 來源表 | 目標表 | 關聯類型 | 說明 |
|-------|-------|---------|------|
| risk_stress_tests | risk_stress_results | 1:N | 每次測試包含多個情境結果 |
| risk_scenarios | risk_stress_results | N:M | 情境可用於多次測試 |

---

## 3. 資料流向圖

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          M17 風險管理資料流向                             │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                         輸入資料來源                              │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │                                                                   │   │
│  │  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐        │   │
│  │  │ M06 股價資料   │  │ M07 技術指標   │  │ M18 投組持倉   │        │   │
│  │  │ daily_prices  │  │ indicators    │  │ positions     │        │   │
│  │  └───────┬───────┘  └───────┬───────┘  └───────┬───────┘        │   │
│  │          │                  │                  │                 │   │
│  └──────────┼──────────────────┼──────────────────┼─────────────────┘   │
│             │                  │                  │                      │
│             ▼                  ▼                  ▼                      │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                       風險計算引擎                                │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │                                                                   │   │
│  │  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐          │   │
│  │  │ VaR Engine  │    │ Volatility  │    │ Correlation │          │   │
│  │  │             │    │ Calculator  │    │ Analyzer    │          │   │
│  │  └──────┬──────┘    └──────┬──────┘    └──────┬──────┘          │   │
│  │         │                  │                  │                  │   │
│  └─────────┼──────────────────┼──────────────────┼──────────────────┘   │
│            │                  │                  │                       │
│            ▼                  ▼                  ▼                       │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                        風險資料儲存                               │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │                                                                   │   │
│  │  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐        │   │
│  │  │risk_snapshots │  │risk_var_results│ │correlation    │        │   │
│  │  │ (每日快照)     │  │ (VaR 結果)     │  │_cache         │        │   │
│  │  └───────────────┘  └───────────────┘  └───────────────┘        │   │
│  │                                                                   │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                    │                                     │
│            ┌───────────────────────┼───────────────────────┐            │
│            ▼                       ▼                       ▼            │
│  ┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐    │
│  │   限額檢查       │    │   預警觸發       │    │   壓力測試       │    │
│  ├─────────────────┤    ├─────────────────┤    ├─────────────────┤    │
│  │ risk_limits     │    │ risk_alert_rules│    │risk_stress_tests│    │
│  │       │         │    │       │         │    │       │         │    │
│  │       ▼         │    │       ▼         │    │       ▼         │    │
│  │limit_checks     │    │  risk_alerts    │    │stress_results   │    │
│  └─────────────────┘    └─────────────────┘    └─────────────────┘    │
│            │                       │                       │            │
│            └───────────────────────┼───────────────────────┘            │
│                                    ▼                                     │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                          輸出服務                                 │   │
│  ├─────────────────────────────────────────────────────────────────┤   │
│  │  • 風險報告生成                                                   │   │
│  │  • 預警通知發送                                                   │   │
│  │  • M18 投組管理 (下游)                                            │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 4. 索引策略

### 4.1 查詢模式分析

| 查詢場景 | 頻率 | 對應索引 |
|---------|------|---------|
| 取得投組最新風險快照 | 高 | `idx_risk_snapshots_portfolio` |
| 查詢歷史風險趨勢 | 中 | `idx_risk_snapshots_date` |
| 取得有效預警 | 高 | `idx_alerts_active` |
| 限額狀態檢查 | 高 | `idx_risk_limits_portfolio` |
| 壓力測試結果 | 低 | `idx_stress_results_test` |

### 4.2 關鍵索引

```sql
-- 風險快照：投組+日期查詢
CREATE INDEX idx_risk_snapshots_portfolio ON risk_snapshots(portfolio_id, snapshot_date DESC);

-- VaR 結果：按方法查詢
CREATE INDEX idx_var_results_method ON risk_var_results(portfolio_id, method, calculation_date DESC);

-- 限額：啟用中的限額
CREATE INDEX idx_risk_limits_active ON risk_limits(portfolio_id, enabled) WHERE enabled = TRUE;

-- 預警：有效預警快速查詢
CREATE INDEX idx_alerts_active ON risk_alerts(portfolio_id, status) WHERE status = 'ACTIVE';

-- 相關性快取：過期清理
CREATE INDEX idx_correlation_cache_expires ON risk_correlation_cache(expires_at);
```

---

## 5. 資料生命週期

### 5.1 保留策略

| 資料表 | 保留期限 | 清理策略 |
|-------|---------|---------|
| risk_snapshots | 3 年 | 按月保留月底快照 |
| risk_var_results | 1 年 | 保留每日最新計算 |
| risk_limit_checks | 1 年 | 定期清理 |
| risk_alerts | 2 年 | 已解決超過 1 年可清理 |
| risk_stress_tests | 1 年 | 保留結果摘要 |
| risk_correlation_cache | 7 天 | 自動過期 |

### 5.2 資料量預估

| 資料表 | 每投組每日筆數 | 100 投組/年預估 |
|-------|--------------|---------------|
| risk_snapshots | 1 | 36,500 筆 |
| risk_var_results | 3 (不同方法) | 109,500 筆 |
| risk_limit_checks | 8 (限額類型) | 292,000 筆 |
| risk_alerts | 0.5 (平均) | 18,250 筆 |

---

## 6. 跨模組依賴

### 6.1 上游依賴

```
┌─────────────────────────────────────────────────────────────┐
│                    M17 上游依賴                              │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌─────────────┐      ┌─────────────┐      ┌─────────────┐ │
│  │    M06      │      │    M07      │      │    M18      │ │
│  │  資料管理    │      │  技術分析    │      │  投組管理    │ │
│  ├─────────────┤      ├─────────────┤      ├─────────────┤ │
│  │ • 股價資料   │      │ • 波動度     │      │ • 投組定義   │ │
│  │ • 成交量     │      │ • Beta      │      │ • 持倉明細   │ │
│  │ • 歷史報酬   │      │ • 技術指標   │      │ • 市值權重   │ │
│  └──────┬──────┘      └──────┬──────┘      └──────┬──────┘ │
│         │                    │                    │         │
│         └────────────────────┼────────────────────┘         │
│                              ▼                               │
│                     ┌─────────────┐                         │
│                     │    M17      │                         │
│                     │  風險管理    │                         │
│                     └─────────────┘                         │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### 6.2 下游輸出

```
┌─────────────────────────────────────────────────────────────┐
│                    M17 下游輸出                              │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│                     ┌─────────────┐                         │
│                     │    M17      │                         │
│                     │  風險管理    │                         │
│                     └──────┬──────┘                         │
│                            │                                 │
│         ┌──────────────────┼──────────────────┐             │
│         ▼                  ▼                  ▼             │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐     │
│  │    M18      │    │   通知系統   │    │   報表系統   │     │
│  │  投組管理    │    │             │    │             │     │
│  ├─────────────┤    ├─────────────┤    ├─────────────┤     │
│  │ • 風險控制   │    │ • 預警通知   │    │ • 風險報告   │     │
│  │ • 再平衡建議 │    │ • Email     │    │ • 儀表板     │     │
│  │ • 限額檢查   │    │ • Push      │    │ • PDF 匯出   │     │
│  └─────────────┘    └─────────────┘    └─────────────┘     │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 7. 相關文檔

- [M17 功能需求](../../specs/functional/M17-風險管理功能需求.md)
- [M17 API 規格](../../specs/api/M17-API規格.md)
- [M17 資料庫設計](../M17-資料庫設計.md)
- [M18 ERD](./M18-ERD.md)

---

**文件維護者**: 後端工程師
**最後更新**: 2026-01-15
**下次審核**: 2026-04-15
