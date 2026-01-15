# M11-量化策略模組 API 規格

> **文件編號**: API-M11
> **模組名稱**: 量化策略模組
> **版本**: v1.0
> **最後更新**: 2026-01-14
> **狀態**: Draft

---

## 📋 API 總覽

### 基礎資訊

| 項目 | 值 |
|-----|-----|
| Base URL | `/api/v1/strategy` |
| 認證方式 | JWT Bearer Token |
| 內容類型 | application/json |
| 字元編碼 | UTF-8 |

### API 清單

| 端點 | 方法 | 說明 | 優先級 |
|-----|------|------|-------|
| `/api/v1/strategy` | GET | 查詢策略清單 | P0 |
| `/api/v1/strategy/{strategyId}` | GET | 查詢策略詳情 | P0 |
| `/api/v1/strategy` | POST | 建立新策略 | P0 |
| `/api/v1/strategy/{strategyId}` | PUT | 更新策略 | P0 |
| `/api/v1/strategy/{strategyId}` | DELETE | 刪除策略 | P1 |
| `/api/v1/strategy/{strategyId}/status` | PATCH | 更新策略狀態 | P0 |
| `/api/v1/strategy/{strategyId}/execute` | POST | 執行策略 | P0 |
| `/api/v1/strategy/{strategyId}/signals` | GET | 查詢策略信號 | P0 |
| `/api/v1/strategy/signals/scan` | GET | 全市場策略信號掃描 | P1 |
| `/api/v1/strategy/factors` | GET | 查詢因子清單 | P0 |
| `/api/v1/strategy/factors/{factorId}` | GET | 查詢因子詳情 | P1 |
| `/api/v1/strategy/{strategyId}/executions` | GET | 查詢執行歷史 | P1 |
| `/api/v1/strategy/{strategyId}/optimize` | POST | 策略參數優化 | P1 |
| `/api/v1/strategy/{strategyId}/backtest` | POST | 觸發策略回測 | P1 |
| `/api/v1/strategy/presets` | GET | 查詢預設策略庫 | P0 |

---

## 1. 策略清單查詢

### GET `/api/v1/strategy`

查詢策略清單，支援分頁與篩選。

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| status | string | N | all | 策略狀態（DRAFT, ACTIVE, INACTIVE, ARCHIVED） |
| type | string | N | all | 策略類型（MOMENTUM, VALUE, HYBRID, CUSTOM） |
| keyword | string | N | - | 關鍵字搜尋（名稱、描述） |
| page | integer | N | 0 | 頁碼（從 0 開始） |
| size | integer | N | 20 | 每頁筆數（1-100） |
| sort | string | N | created_at,desc | 排序欄位與方向 |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "strategy_id": "STG_MOMENTUM_001",
        "strategy_name": "動能反轉策略",
        "strategy_type": "MOMENTUM",
        "description": "尋找超賣且有法人進場的股票",
        "version": 1,
        "status": "ACTIVE",
        "is_preset": true,
        "condition_count": 4,
        "last_execution": "2024-12-24T16:30:00+08:00",
        "total_signals_today": 15,
        "created_at": "2024-01-15T10:00:00+08:00",
        "updated_at": "2024-12-20T14:30:00+08:00"
      },
      {
        "strategy_id": "STG_CUSTOM_001",
        "strategy_name": "我的自訂策略",
        "strategy_type": "CUSTOM",
        "description": "自訂的多因子策略",
        "version": 3,
        "status": "ACTIVE",
        "is_preset": false,
        "condition_count": 6,
        "last_execution": "2024-12-24T16:30:00+08:00",
        "total_signals_today": 8,
        "created_at": "2024-06-01T09:00:00+08:00",
        "updated_at": "2024-12-23T11:20:00+08:00"
      }
    ],
    "page": 0,
    "size": 20,
    "total_elements": 12,
    "total_pages": 1
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_stg_001"
}
```

---

## 2. 策略詳情查詢

### GET `/api/v1/strategy/{strategyId}`

查詢單一策略的完整定義。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| strategyId | string | Y | 策略 ID |

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| version | integer | N | latest | 指定版本號（省略則取最新版） |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "strategy_id": "STG_MOMENTUM_001",
    "strategy_name": "動能反轉策略",
    "strategy_type": "MOMENTUM",
    "description": "尋找超賣且有法人進場的股票，捕捉反彈機會",
    "version": 1,
    "status": "ACTIVE",
    "is_preset": true,
    "conditions": {
      "logic": "AND",
      "conditions": [
        {
          "factor_id": "M07_RSI_14",
          "operator": "LESS_THAN",
          "value": 30,
          "description": "RSI(14) < 30"
        },
        {
          "factor_id": "M07_KD_K",
          "operator": "LESS_THAN",
          "value": 20,
          "description": "K值 < 20"
        },
        {
          "logic": "OR",
          "conditions": [
            {
              "factor_id": "M09_FOREIGN_NET",
              "operator": "GREATER_THAN",
              "value": 0
            },
            {
              "factor_id": "M09_TRUST_NET",
              "operator": "GREATER_THAN",
              "value": 0
            }
          ]
        },
        {
          "factor_id": "M06_VOLUME_RATIO",
          "operator": "GREATER_THAN",
          "value": 1.0
        }
      ]
    },
    "parameters": {
      "rsi_threshold": 30,
      "kd_threshold": 20,
      "volume_ratio_min": 1.0,
      "lookback_days": 60
    },
    "output": {
      "signal_type": "BUY",
      "confidence_formula": "(30 - RSI) / 30 * 0.4 + (20 - KD_K) / 20 * 0.3 + volume_score * 0.3"
    },
    "statistics": {
      "total_executions": 250,
      "total_signals": 1580,
      "avg_signals_per_execution": 6.32,
      "avg_confidence": 72.5
    },
    "created_by": "system",
    "created_at": "2024-01-15T10:00:00+08:00",
    "updated_at": "2024-12-20T14:30:00+08:00"
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_stg_002"
}
```

#### 錯誤回應

| HTTP Status | 錯誤碼 | 說明 |
|-------------|-------|------|
| 404 | M11_STG_001 | 策略不存在 |

---

## 3. 建立新策略

### POST `/api/v1/strategy`

建立新的自訂策略。

#### 請求主體

```json
{
  "strategy_name": "我的價值策略",
  "strategy_type": "VALUE",
  "description": "尋找低估值且獲利穩定的股票",
  "conditions": {
    "logic": "AND",
    "conditions": [
      {
        "factor_id": "M08_PE_RATIO",
        "operator": "LESS_THAN",
        "value": 15,
        "description": "本益比 < 15"
      },
      {
        "factor_id": "M08_ROE",
        "operator": "GREATER_THAN",
        "value": 15,
        "description": "ROE > 15%"
      },
      {
        "factor_id": "M08_DIVIDEND_YIELD",
        "operator": "GREATER_THAN",
        "value": 3,
        "description": "殖利率 > 3%"
      }
    ]
  },
  "parameters": {
    "pe_threshold": 15,
    "roe_threshold": 15,
    "dividend_yield_min": 3
  },
  "output": {
    "signal_type": "BUY",
    "confidence_formula": "(15 - PE) / 15 * 0.4 + (ROE - 15) / 30 * 0.3 + dividend_score * 0.3"
  }
}
```

#### 成功回應 (201)

```json
{
  "code": 201,
  "message": "Strategy created successfully",
  "data": {
    "strategy_id": "STG_CUSTOM_002",
    "strategy_name": "我的價值策略",
    "version": 1,
    "status": "DRAFT",
    "created_at": "2024-12-24T15:30:00+08:00"
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_stg_003"
}
```

#### 錯誤回應

| HTTP Status | 錯誤碼 | 說明 |
|-------------|-------|------|
| 400 | M11_STG_002 | 策略定義格式錯誤 |
| 400 | M11_STG_003 | 策略條件無效 |
| 400 | M11_FACTOR_001 | 因子不存在 |

---

## 4. 更新策略

### PUT `/api/v1/strategy/{strategyId}`

更新策略定義（會建立新版本）。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| strategyId | string | Y | 策略 ID |

#### 請求主體

```json
{
  "strategy_name": "我的價值策略（優化版）",
  "description": "調整後的價值策略",
  "conditions": {
    "logic": "AND",
    "conditions": [
      {
        "factor_id": "M08_PE_RATIO",
        "operator": "LESS_THAN",
        "value": 12
      },
      {
        "factor_id": "M08_ROE",
        "operator": "GREATER_THAN",
        "value": 18
      }
    ]
  },
  "parameters": {
    "pe_threshold": 12,
    "roe_threshold": 18
  }
}
```

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Strategy updated successfully",
  "data": {
    "strategy_id": "STG_CUSTOM_002",
    "strategy_name": "我的價值策略（優化版）",
    "version": 2,
    "previous_version": 1,
    "status": "DRAFT",
    "updated_at": "2024-12-24T16:00:00+08:00"
  },
  "timestamp": "2024-12-24T16:00:00+08:00",
  "trace_id": "req_stg_004"
}
```

---

## 5. 更新策略狀態

### PATCH `/api/v1/strategy/{strategyId}/status`

更新策略狀態（啟用/停用）。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| strategyId | string | Y | 策略 ID |

#### 請求主體

```json
{
  "status": "ACTIVE"
}
```

**狀態轉換規則**:

| 當前狀態 | 可轉換至 |
|---------|---------|
| DRAFT | ACTIVE, ARCHIVED |
| ACTIVE | INACTIVE, ARCHIVED |
| INACTIVE | ACTIVE, ARCHIVED |
| ARCHIVED | （不可轉換） |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Strategy status updated",
  "data": {
    "strategy_id": "STG_CUSTOM_002",
    "previous_status": "DRAFT",
    "current_status": "ACTIVE",
    "updated_at": "2024-12-24T16:30:00+08:00"
  },
  "timestamp": "2024-12-24T16:30:00+08:00",
  "trace_id": "req_stg_005"
}
```

---

## 6. 執行策略

### POST `/api/v1/strategy/{strategyId}/execute`

執行策略，對指定股票或全市場進行策略評估。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| strategyId | string | Y | 策略 ID |

#### 請求主體

```json
{
  "execution_date": "2024-12-24",
  "stock_universe": {
    "type": "MARKET",
    "market_type": "TWSE",
    "min_volume": 1000,
    "exclude_etf": true,
    "industries": null
  },
  "options": {
    "include_factor_values": true,
    "include_diagnostics": true,
    "save_results": true
  }
}
```

**股票範圍類型**:

| type | 說明 | 額外參數 |
|------|------|---------|
| MARKET | 全市場 | market_type, min_volume, exclude_etf |
| WATCHLIST | 自選股 | watchlist_id |
| STOCKS | 指定股票 | stock_ids (陣列) |
| INDUSTRY | 特定產業 | industries (陣列) |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Strategy executed successfully",
  "data": {
    "execution_id": "EXEC_20241224_001",
    "strategy_id": "STG_MOMENTUM_001",
    "strategy_name": "動能反轉策略",
    "execution_date": "2024-12-24",
    "execution_summary": {
      "stocks_evaluated": 1800,
      "signals_generated": 15,
      "buy_signals": 15,
      "sell_signals": 0,
      "avg_confidence": 72.5,
      "execution_time_ms": 45000
    },
    "signals": [
      {
        "signal_id": "STG_SIG_20241224_001",
        "stock_id": "2330",
        "stock_name": "台積電",
        "signal_type": "BUY",
        "confidence_score": 85.5,
        "matched_conditions": [
          {
            "factor_id": "M07_RSI_14",
            "factor_value": 25.3,
            "condition": "RSI < 30",
            "matched": true
          },
          {
            "factor_id": "M07_KD_K",
            "factor_value": 18.5,
            "condition": "K < 20",
            "matched": true
          },
          {
            "factor_id": "M09_FOREIGN_NET",
            "factor_value": 5000000,
            "condition": "外資買超 > 0",
            "matched": true
          },
          {
            "factor_id": "M06_VOLUME_RATIO",
            "factor_value": 1.35,
            "condition": "量比 > 1.0",
            "matched": true
          }
        ],
        "factor_values": {
          "M07_RSI_14": 25.3,
          "M07_KD_K": 18.5,
          "M09_FOREIGN_NET": 5000000,
          "M06_VOLUME_RATIO": 1.35
        }
      },
      {
        "signal_id": "STG_SIG_20241224_002",
        "stock_id": "2454",
        "stock_name": "聯發科",
        "signal_type": "BUY",
        "confidence_score": 78.2,
        "matched_conditions": [
          {
            "factor_id": "M07_RSI_14",
            "factor_value": 28.7,
            "condition": "RSI < 30",
            "matched": true
          }
        ]
      }
    ],
    "diagnostics": {
      "factors_loaded": 4,
      "factors_missing": 0,
      "calculation_errors": 0,
      "warnings": []
    }
  },
  "timestamp": "2024-12-24T16:30:00+08:00",
  "trace_id": "req_stg_006"
}
```

#### 錯誤回應

| HTTP Status | 錯誤碼 | 說明 |
|-------------|-------|------|
| 404 | M11_STG_001 | 策略不存在 |
| 400 | M11_STG_005 | 策略已停用 |
| 400 | M11_FACTOR_002 | 因子數據不足 |
| 500 | M11_EXEC_001 | 策略執行失敗 |
| 504 | M11_EXEC_002 | 執行逾時 |

---

## 7. 查詢策略信號

### GET `/api/v1/strategy/{strategyId}/signals`

查詢指定策略產生的信號。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| strategyId | string | Y | 策略 ID |

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| start_date | string | N | 30天前 | 開始日期（YYYY-MM-DD） |
| end_date | string | N | 今日 | 結束日期 |
| signal_type | string | N | all | 信號類型（BUY, SELL, HOLD） |
| stock_id | string | N | all | 指定股票 |
| min_confidence | number | N | 0 | 最低信心度 |
| page | integer | N | 0 | 頁碼 |
| size | integer | N | 50 | 每頁筆數 |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "strategy_id": "STG_MOMENTUM_001",
    "strategy_name": "動能反轉策略",
    "content": [
      {
        "signal_id": "STG_SIG_20241224_001",
        "stock_id": "2330",
        "stock_name": "台積電",
        "trade_date": "2024-12-24",
        "signal_type": "BUY",
        "confidence_score": 85.5,
        "close_price": 580.00,
        "factor_summary": {
          "rsi": 25.3,
          "kd_k": 18.5,
          "foreign_net": 5000000
        }
      },
      {
        "signal_id": "STG_SIG_20241223_005",
        "stock_id": "2317",
        "stock_name": "鴻海",
        "trade_date": "2024-12-23",
        "signal_type": "BUY",
        "confidence_score": 72.8,
        "close_price": 105.50,
        "factor_summary": {
          "rsi": 28.1,
          "kd_k": 19.2,
          "trust_net": 800000
        }
      }
    ],
    "summary": {
      "total_signals": 156,
      "buy_signals": 145,
      "sell_signals": 11,
      "avg_confidence": 71.2
    },
    "page": 0,
    "size": 50,
    "total_elements": 156,
    "total_pages": 4
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_stg_007"
}
```

---

## 8. 全市場策略信號掃描

### GET `/api/v1/strategy/signals/scan`

掃描全市場，取得所有活躍策略產生的信號。

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| trade_date | string | N | 最近交易日 | 交易日期 |
| signal_type | string | N | all | 信號類型 |
| min_confidence | number | N | 60 | 最低信心度 |
| strategy_type | string | N | all | 策略類型 |
| limit | integer | N | 100 | 回傳筆數 |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "trade_date": "2024-12-24",
    "scan_time_ms": 2500,
    "strategies_scanned": 10,
    "total_signals": 45,
    "signal_summary": {
      "buy": 38,
      "sell": 5,
      "hold": 2
    },
    "signals": [
      {
        "signal_id": "STG_SIG_20241224_001",
        "strategy_id": "STG_MOMENTUM_001",
        "strategy_name": "動能反轉策略",
        "stock_id": "2330",
        "stock_name": "台積電",
        "signal_type": "BUY",
        "confidence_score": 85.5
      },
      {
        "signal_id": "STG_SIG_20241224_015",
        "strategy_id": "STG_VALUE_001",
        "strategy_name": "價值低估策略",
        "stock_id": "2330",
        "stock_name": "台積電",
        "signal_type": "BUY",
        "confidence_score": 78.3
      }
    ],
    "stock_signal_count": {
      "2330": 3,
      "2454": 2,
      "2317": 2
    }
  },
  "timestamp": "2024-12-24T16:30:00+08:00",
  "trace_id": "req_stg_008"
}
```

---

## 9. 查詢因子清單

### GET `/api/v1/strategy/factors`

查詢可用於策略組合的因子清單。

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| category | string | N | all | 因子類別（TECHNICAL, FUNDAMENTAL, CHIP, PRICE_VOLUME） |
| source_module | string | N | all | 來源模組（M06, M07, M08, M09） |
| keyword | string | N | - | 關鍵字搜尋 |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "total_factors": 189,
    "categories": [
      {
        "category": "TECHNICAL",
        "category_name": "技術面因子",
        "source_module": "M07",
        "factor_count": 71,
        "factors": [
          {
            "factor_id": "M07_RSI_14",
            "factor_name": "RSI(14)",
            "display_name": "相對強弱指標 14 日",
            "data_type": "NUMERIC",
            "value_range": {"min": 0, "max": 100},
            "typical_thresholds": [30, 70],
            "description": "衡量價格變動速度與幅度的動量指標"
          },
          {
            "factor_id": "M07_MACD_HISTOGRAM",
            "factor_name": "MACD柱狀圖",
            "display_name": "MACD 差離值",
            "data_type": "NUMERIC",
            "description": "MACD 與信號線的差值"
          }
        ]
      },
      {
        "category": "FUNDAMENTAL",
        "category_name": "基本面因子",
        "source_module": "M08",
        "factor_count": 75,
        "factors": [
          {
            "factor_id": "M08_PE_RATIO",
            "factor_name": "PE",
            "display_name": "本益比",
            "data_type": "NUMERIC",
            "typical_thresholds": [10, 20],
            "description": "股價除以每股盈餘"
          }
        ]
      },
      {
        "category": "CHIP",
        "category_name": "籌碼面因子",
        "source_module": "M09",
        "factor_count": 28,
        "factors": [
          {
            "factor_id": "M09_FOREIGN_NET",
            "factor_name": "外資買賣超",
            "display_name": "外資淨買賣",
            "data_type": "NUMERIC",
            "description": "外資當日買進減賣出股數"
          }
        ]
      }
    ]
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_stg_009"
}
```

---

## 10. 查詢因子詳情

### GET `/api/v1/strategy/factors/{factorId}`

查詢單一因子的詳細資訊。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| factorId | string | Y | 因子 ID |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "factor_id": "M07_RSI_14",
    "factor_name": "RSI(14)",
    "display_name": "相對強弱指標 14 日",
    "category": "TECHNICAL",
    "source_module": "M07",
    "data_type": "NUMERIC",
    "value_range": {
      "min": 0,
      "max": 100
    },
    "typical_thresholds": [30, 70],
    "default_operator": "LESS_THAN",
    "supported_operators": [
      "EQUAL", "GREATER_THAN", "LESS_THAN",
      "BETWEEN", "CROSS_ABOVE", "CROSS_BELOW"
    ],
    "description": "相對強弱指標（Relative Strength Index），衡量價格變動速度與幅度的動量指標。RSI < 30 通常被視為超賣，RSI > 70 被視為超買。",
    "calculation_formula": "RSI = 100 - 100 / (1 + RS)，其中 RS = 14日平均漲幅 / 14日平均跌幅",
    "update_frequency": "DAILY",
    "example_conditions": [
      {
        "description": "超賣訊號",
        "condition": {"factor_id": "M07_RSI_14", "operator": "LESS_THAN", "value": 30}
      },
      {
        "description": "超買訊號",
        "condition": {"factor_id": "M07_RSI_14", "operator": "GREATER_THAN", "value": 70}
      }
    ],
    "related_factors": ["M07_KD_K", "M07_KD_D", "M07_STOCH_RSI"]
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_stg_010"
}
```

---

## 11. 查詢執行歷史

### GET `/api/v1/strategy/{strategyId}/executions`

查詢策略的執行歷史記錄。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| strategyId | string | Y | 策略 ID |

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| start_date | string | N | 30天前 | 開始日期 |
| end_date | string | N | 今日 | 結束日期 |
| page | integer | N | 0 | 頁碼 |
| size | integer | N | 20 | 每頁筆數 |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "strategy_id": "STG_MOMENTUM_001",
    "strategy_name": "動能反轉策略",
    "content": [
      {
        "execution_id": "EXEC_20241224_001",
        "execution_date": "2024-12-24",
        "execution_type": "SCHEDULED",
        "stocks_evaluated": 1800,
        "signals_generated": 15,
        "buy_signals": 15,
        "sell_signals": 0,
        "avg_confidence": 72.5,
        "execution_time_ms": 45000,
        "status": "SUCCESS",
        "executed_at": "2024-12-24T16:30:00+08:00"
      },
      {
        "execution_id": "EXEC_20241223_001",
        "execution_date": "2024-12-23",
        "execution_type": "SCHEDULED",
        "stocks_evaluated": 1800,
        "signals_generated": 12,
        "buy_signals": 12,
        "sell_signals": 0,
        "avg_confidence": 68.3,
        "execution_time_ms": 42000,
        "status": "SUCCESS",
        "executed_at": "2024-12-23T16:30:00+08:00"
      }
    ],
    "summary": {
      "total_executions": 250,
      "successful_executions": 248,
      "failed_executions": 2,
      "avg_execution_time_ms": 43500,
      "total_signals_generated": 1580
    },
    "page": 0,
    "size": 20,
    "total_elements": 250,
    "total_pages": 13
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_stg_011"
}
```

---

## 12. 策略參數優化

### POST `/api/v1/strategy/{strategyId}/optimize`

對策略參數進行網格搜索優化。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| strategyId | string | Y | 策略 ID |

#### 請求主體

```json
{
  "optimization_config": {
    "method": "GRID_SEARCH",
    "objective": "SHARPE_RATIO",
    "parameters": [
      {
        "param_name": "rsi_threshold",
        "min_value": 20,
        "max_value": 40,
        "step": 5
      },
      {
        "param_name": "volume_ratio_min",
        "min_value": 0.5,
        "max_value": 2.0,
        "step": 0.25
      }
    ],
    "backtest_config": {
      "start_date": "2023-01-01",
      "end_date": "2024-12-31",
      "initial_capital": 1000000
    }
  }
}
```

#### 成功回應 (202)

```json
{
  "code": 202,
  "message": "Optimization job submitted",
  "data": {
    "optimization_id": "OPT_20241224_001",
    "strategy_id": "STG_MOMENTUM_001",
    "status": "RUNNING",
    "total_combinations": 25,
    "estimated_time_minutes": 15,
    "submitted_at": "2024-12-24T16:30:00+08:00"
  },
  "timestamp": "2024-12-24T16:30:00+08:00",
  "trace_id": "req_stg_012"
}
```

---

## 13. 觸發策略回測

### POST `/api/v1/strategy/{strategyId}/backtest`

觸發 M16 回測系統執行策略回測。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| strategyId | string | Y | 策略 ID |

#### 請求主體

```json
{
  "backtest_config": {
    "start_date": "2023-01-01",
    "end_date": "2024-12-31",
    "initial_capital": 1000000,
    "position_size": 0.1,
    "max_positions": 10,
    "commission_rate": 0.001425,
    "tax_rate": 0.003,
    "slippage": 0.001
  },
  "stock_universe": {
    "market_type": "TWSE",
    "min_volume": 1000,
    "exclude_etf": true
  }
}
```

#### 成功回應 (202)

```json
{
  "code": 202,
  "message": "Backtest job submitted to M16",
  "data": {
    "backtest_id": "BT_20241224_001",
    "strategy_id": "STG_MOMENTUM_001",
    "status": "QUEUED",
    "estimated_time_minutes": 30,
    "callback_url": "/api/v1/backtest/BT_20241224_001",
    "submitted_at": "2024-12-24T16:30:00+08:00"
  },
  "timestamp": "2024-12-24T16:30:00+08:00",
  "trace_id": "req_stg_013"
}
```

---

## 14. 查詢預設策略庫

### GET `/api/v1/strategy/presets`

查詢系統內建的預設策略。

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "preset_strategies": [
      {
        "strategy_id": "STG_MOMENTUM_001",
        "strategy_name": "動能反轉策略",
        "strategy_type": "MOMENTUM",
        "description": "尋找超賣且有法人進場的股票，捕捉反彈機會",
        "difficulty": "BEGINNER",
        "avg_signals_per_day": 5,
        "backtest_performance": {
          "annual_return": 15.2,
          "sharpe_ratio": 1.25,
          "max_drawdown": -12.5,
          "win_rate": 58.3
        }
      },
      {
        "strategy_id": "STG_VALUE_001",
        "strategy_name": "價值低估策略",
        "strategy_type": "VALUE",
        "description": "尋找低估值且獲利穩定的股票",
        "difficulty": "INTERMEDIATE",
        "avg_signals_per_day": 8,
        "backtest_performance": {
          "annual_return": 12.8,
          "sharpe_ratio": 1.15,
          "max_drawdown": -10.2,
          "win_rate": 55.1
        }
      },
      {
        "strategy_id": "STG_CHIP_001",
        "strategy_name": "法人認養策略",
        "strategy_type": "HYBRID",
        "description": "追蹤三大法人同買且技術面轉強的股票",
        "difficulty": "INTERMEDIATE",
        "avg_signals_per_day": 3,
        "backtest_performance": {
          "annual_return": 18.5,
          "sharpe_ratio": 1.42,
          "max_drawdown": -15.3,
          "win_rate": 62.1
        }
      }
    ]
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_stg_014"
}
```

---

## 共用錯誤回應格式

```json
{
  "code": 400,
  "message": "Bad Request",
  "error": {
    "error_code": "M11_STG_002",
    "error_message": "策略定義格式錯誤",
    "error_detail": "conditions 欄位缺少必要的 logic 屬性",
    "suggestion": "請確認 conditions 包含 logic (AND/OR) 與 conditions 陣列"
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_stg_err_001"
}
```

---

## 📚 相關文檔

- [M11 功能需求](../functional/M11-量化策略功能需求.md)
- [M11 資料庫設計](../../design/M11-資料庫設計.md)
- [API 回應格式總綱](../technical/00-全系統契約.md#44-api-回應格式)
- [M16 回測系統 API](./M16-API規格.md)

---

**文件維護者**: 後端工程師
**最後更新**: 2026-01-14
**下次審核**: 2026-04-14
