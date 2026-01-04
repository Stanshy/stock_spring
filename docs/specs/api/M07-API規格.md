# M07-技術分析模組 API 規格

> **文件編號**: API-M07  
> **模組名稱**: 技術分析模組  
> **版本**: v2.0  
> **最後更新**: 2026-01-03  
> **狀態**: Draft

---

## 📋 API 總覽

本文件定義 技術分析模組的所有 REST API 規格。

---

## 4. API 設計

> **重要**: 所有 API 必須遵守 [全系統契約 - API 統一規範](../technical/00-全系統契約.md#4-api-統一規範)

### 4.1 API 列表總覽

#### 指標查詢 API (IndicatorQueryController)

| API 端點 | HTTP Method | 說明 | 功能編號 | 回應格式 |
|---------|-------------|------|---------|---------|
| GET /api/stocks/{stockId}/indicators | GET | 查詢單一股票技術指標 | F-M07-009 | 指標列表 |
| GET /api/stocks/{stockId}/indicators/{name} | GET | 查詢單一股票特定指標 | F-M07-009 | 特定指標資料 |
| GET /api/indicators/latest | GET | 批次查詢最新指標 | F-M07-009 | 列表 |
| GET /api/indicators/signals/crosses | GET | 查詢交叉信號 | F-M07-010 | 信號列表 |
| GET /api/indicators/signals/overbought | GET | 查詢超買超賣信號 | F-M07-010 | 信號列表 |

#### 指標管理 API (IndicatorManagementController)

| API 端點 | HTTP Method | 說明 | 功能編號 | 回應格式 |
|---------|-------------|------|---------|---------|
| GET /api/indicators/definitions | GET | 查詢指標定義清單 | F-M07-006 | 列表 |
| GET /api/indicators/definitions/{name} | GET | 查詢單一指標定義 | F-M07-006 | 單一物件 |

#### Job 管理 API (IndicatorJobController)

| API 端點 | HTTP Method | 說明 | 功能編號 | 回應格式 |
|---------|-------------|------|---------|---------|
| POST /api/jobs/calculate-indicators | POST | 手動觸發指標計算 | F-M07-013 | Job 執行資訊 |

---

### 4.2 API 詳細設計

## 指標查詢 API

#### API-M07-001: 查詢單一股票技術指標

**Request**:
```
GET /api/stocks/2330/indicators?startDate=2025-12-30&endDate=2025-12-31&indicators=MA,RSI&categories=TREND,MOMENTUM
```

**Path Parameters**:
| 參數 | 類型 | 說明 |
|-----|------|------|
| stockId | String | 股票代碼（如 2330） |

**Query Parameters**:
| 參數 | 類型 | 必填 | 說明 | 預設值 |
|-----|------|------|------|-------|
| startDate | String | N | 開始日期（YYYY-MM-DD） | 30天前 |
| endDate | String | N | 結束日期（YYYY-MM-DD） | 今日 |
| indicators | String | N | 指標名稱清單（逗號分隔，如 MA,RSI,MACD） | 全部 |
| categories | String | N | 指標類別（TREND,MOMENTUM,VOLATILITY,VOLUME） | 全部 |

**Response** (成功):
```json
{
    "code": 200,
    "message": "Success",
    "data": {
        "stock_id": "2330",
        "stock_name": "",
        "indicators": [
            {
                "calculation_date": "2025-12-30",
                "trend": {
                    "ma5": 1509.0,
                    "ma20": 1470.0,
                    "ma60": 1455.5833333333333,
                    "macd": {
                        "histogram": 7.13,
                        "macd_line": 17.8,
                        "macd_signal": "BULLISH",
                        "signal_line": 10.68
                    },
                    "ema12": 1484.98,
                    "ema26": 1467.18
                },
                "momentum": {
                    "rsi_14": 61.8,
                    "rsi_signal": "NEUTRAL"
                },
                "volatility": {
                    "bbands": {
                        "lower": 1406.83,
                        "upper": 1533.17,
                        "middle": 1470.0,
                        "signal": "NEAR_UPPER",
                        "bandwidth": 0.09,
                        "percent_b": 0.9
                    }
                },
                "volume": {}
            },
            {
                "calculation_date": "2025-12-31",
                "trend": {
                    "ma5": 1521.0,
                    "ma20": 1476.0,
                    "ma60": 1458.6666666666667,
                    "macd": {
                        "histogram": 8.8,
                        "macd_line": 21.67,
                        "macd_signal": "BULLISH",
                        "signal_line": 12.88
                    },
                    "ema12": 1494.99,
                    "ema26": 1473.32
                },
                "momentum": {
                    "rsi_14": 66.24,
                    "rsi_signal": "NEUTRAL"
                },
                "volatility": {
                    "bbands": {
                        "lower": 1406.67,
                        "upper": 1545.33,
                        "middle": 1476.0,
                        "signal": "ABOVE_UPPER",
                        "bandwidth": 0.09,
                        "percent_b": 1.03
                    }
                },
                "volume": {}
            }
        ],
        "total_count": 2
    },
    "timestamp": "2026-01-04T02:08:43.2181448+08:00"
}
```

**Response** (股票不存在):
```json
{
    "code": 404,
    "message": "Indicator not found for stock '9999' on date '2025-12-30'",
    "error": {
        "details": "indicator",
        "field": "Please check the stock ID and calculation date",
        "suggestion": "Please check the Please check the stock ID and calculation date and try again",
        "error_code": "M07011",
        "error_type": "CLIENT_ERROR"
    },
    "timestamp": "2026-01-04T02:10:12.7945379+08:00",
    "trace_id": "req_253accbcbc7d"
}
```

---

#### API-M07-002: 查詢單一股票特定指標

**Request**:
```
GET /api/stocks/2330/indicators/RSI?startDate=2025-12-11&endDate=2025-12-31
```

**Path Parameters**:
| 參數 | 類型 | 說明 |
|-----|------|------|
| stockId | String | 股票代碼 |
| indicatorName | String | 指標名稱（MA、RSI、MACD等） |

**Query Parameters**:
| 參數 | 類型 | 必填 | 說明 | 預設值 |
|-----|------|------|------|-------|
| startDate | String | N | 開始日期（YYYY-MM-DD） | 30天前 |
| endDate | String | N | 結束日期（YYYY-MM-DD） | 今日 |

**Response** (成功):
```json
{
    "code": 200,
    "message": "Success",
    "data": {
        "stock_id": "2330",
        "indicator_name": "RSI",
        "indicator_params": {
            "period": 14
        },
        "values": [
            {
                "date": "2025-12-11",
                "value": 53.91
            },
            {
                "date": "2025-12-12",
                "value": 55.45
            },
            {
                "date": "2025-12-15",
                "value": 50.05
            },
            {
                "date": "2025-12-16",
                "value": 47.56
            },
            {
                "date": "2025-12-17",
                "value": 46.72
            },
            {
                "date": "2025-12-18",
                "value": 46.72
            },
            {
                "date": "2025-12-19",
                "value": 46.72
            },
            {
                "date": "2025-12-22",
                "value": 53.82
            },
            {
                "date": "2025-12-23",
                "value": 58.11
            },
            {
                "date": "2025-12-24",
                "value": 58.93
            },
            {
                "date": "2025-12-25",
                "value": 58.93
            },
            {
                "date": "2025-12-26",
                "value": 61.38
            },
            {
                "date": "2025-12-29",
                "value": 64.42
            },
            {
                "date": "2025-12-30",
                "value": 61.80
            },
            {
                "date": "2025-12-31",
                "value": 66.24
            }
        ],
        "total_count": 15,
        "statistics": {
            "max": 66.24,
            "min": 46.72,
            "avg": 55.384,
            "current": 66.24,
            "previous": 61.8,
            "change": 4.439999999999998
        }
    },
    "timestamp": "2026-01-04T02:10:58.9794519+08:00"
}
```

**Response** (無效的指標名稱):
```json
{
    "code": 404,
    "message": "Indicator 'EEEEE' not found for stock '2330'",
    "error": {
        "details": "indicator",
        "field": "Please check the stock ID and calculation date",
        "suggestion": "Please check the Please check the stock ID and calculation date and try again",
        "error_code": "M07011",
        "error_type": "CLIENT_ERROR"
    },
    "timestamp": "2026-01-04T02:11:42.8539118+08:00",
    "trace_id": "req_ebed619eb8db"
}
```

---

#### API-M07-003: 批次查詢最新指標(未實作)

**Request**:
```
GET /api/indicators/latest?stock_ids=2330,2317,2454&indicators=MA,RSI,MACD
```

**Query Parameters**:
| 參數 | 類型 | 必填 | 說明 | 預設值 |
|-----|------|------|------|-------|
| stock_ids | String | Y | 股票代碼清單（逗號分隔，最多50個） | - |
| indicators | String | N | 指標名稱清單（逗號分隔） | 基礎組 (P0) |

**Response** (成功):
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "stock_id": "2330",
      "stock_name": "台積電",
      "calculation_date": "2025-12-24",
      "ma5": 580.50,
      "ma20": 570.80,
      "rsi_14": 65.50,
      "macd": {
        "macd_line": 5.90,
        "signal_line": 4.20,
        "histogram": 1.70
      }
    },
    {
      "stock_id": "2317",
      "stock_name": "鴻海",
      "calculation_date": "2025-12-24",
      "ma5": 105.50,
      "ma20": 102.30,
      "rsi_14": 58.20,
      "macd": {
        "macd_line": 1.20,
        "signal_line": 0.80,
        "histogram": 0.40
      }
    },
    {
      "stock_id": "2454",
      "stock_name": "聯發科",
      "calculation_date": "2025-12-24",
      "ma5": 1150.00,
      "ma20": 1120.50,
      "rsi_14": 72.30,
      "macd": {
        "macd_line": 18.50,
        "signal_line": 15.20,
        "histogram": 3.30
      }
    }
  ],
  "timestamp": "2026-01-03T14:30:00+08:00"
}
```

---

#### API-M07-004: 查詢交叉信號(未實作)

**Request**:
```
GET /api/indicators/signals/crosses?cross_type=GOLDEN&date=2025-12-24&market_type=TWSE
```

**Query Parameters**:
| 參數 | 類型 | 必填 | 說明 | 預設值 |
|-----|------|------|------|-------|
| cross_type | String | N | 交叉類型（GOLDEN, DEATH, KD） | 全部 |
| date | String | N | 查詢日期（YYYY-MM-DD） | 最新交易日 |
| market_type | String | N | 市場類型（TWSE, OTC, EMERGING） | 全部 |

**Response** (成功):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "cross_date": "2025-12-24",
    "signals": [
      {
        "stock_id": "2330",
        "stock_name": "台積電",
        "cross_type": "GOLDEN_CROSS",
        "indicator": "MA",
        "short_period": 5,
        "long_period": 20,
        "short_value": 580.50,
        "long_value": 570.80,
        "previous_short": 568.20,
        "previous_long": 570.50,
        "signal_strength": "STRONG",
        "confidence_score": 75
      },
      {
        "stock_id": "2454",
        "stock_name": "聯發科",
        "cross_type": "GOLDEN_CROSS",
        "indicator": "MA",
        "short_period": 5,
        "long_period": 20,
        "short_value": 1150.00,
        "long_value": 1120.50,
        "previous_short": 1118.00,
        "previous_long": 1122.00,
        "signal_strength": "MEDIUM",
        "confidence_score": 62
      }
    ],
    "total_count": 2
  },
  "timestamp": "2026-01-03T14:30:00+08:00"
}
```

---

#### API-M07-005: 查詢超買超賣信號(未實作)

**Request**:
```
GET /api/indicators/signals/overbought?signal_type=OVERBOUGHT&indicator=RSI&date=2025-12-24
```

**Query Parameters**:
| 參數 | 類型 | 必填 | 說明 | 預設值 |
|-----|------|------|------|-------|
| signal_type | String | N | 信號類型（OVERBOUGHT, OVERSOLD） | 全部 |
| indicator | String | N | 指標（RSI, KD, WILLIAMS_R） | 全部 |
| date | String | N | 查詢日期（YYYY-MM-DD） | 最新交易日 |

**Response** (成功):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "signal_date": "2025-12-24",
    "signals": [
      {
        "stock_id": "1234",
        "stock_name": "XX電子",
        "signal_type": "OVERBOUGHT",
        "indicator": "RSI",
        "indicator_value": 78.50,
        "threshold": 70.00,
        "duration_days": 3,
        "signal_strength": "STRONG",
        "confidence_score": 65
      },
      {
        "stock_id": "5678",
        "stock_name": "YY光電",
        "signal_type": "OVERBOUGHT",
        "indicator": "KD",
        "indicator_value": 85.20,
        "threshold": 80.00,
        "duration_days": 2,
        "signal_strength": "MEDIUM",
        "confidence_score": 58
      }
    ],
    "total_count": 2
  },
  "timestamp": "2026-01-03T14:30:00+08:00"
}
```

---

## 指標管理 API

#### API-M07-006: 查詢指標定義清單

**Request**:
```
GET /api/indicators/definitions?category=MOMENTUM&priority=P0&isActive=true
```

**Query Parameters**:
| 參數 | 類型 | 必填 | 說明 | 預設值 |
|-----|------|------|------|-------|
| category | String | N | 指標類別（TREND, MOMENTUM等） | 全部 |
| priority | String | N | 優先級（P0, P1, P2） | 全部 |
| isActive | Boolean | N | 是否啟用 | true |

**Response** (成功):
```json
{
    "code": 200,
    "message": "Success",
    "data": [
        {
            "definition_id": 4,
            "indicator_name": "RSI",
            "indicator_category": "MOMENTUM",
            "indicator_name_zh": "相對強弱指標",
            "description": "測量價格變動速度和幅度",
            "default_params": {
                "period": 14
            },
            "param_ranges": {
                "max_period": 30,
                "min_period": 5
            },
            "pandas_ta_function": "rsi",
            "min_data_points": 14,
            "output_fields": {
                "fields": [
                    "rsi_14"
                ]
            },
            "value_range": {
                "max": 100,
                "min": 0
            },
            "priority": "P0",
            "is_active": true,
            "is_cached": true,
            "created_at": "2026-01-03 02:13:16",
            "updated_at": "2026-01-03 18:05:10"
        },
        {
            "definition_id": 5,
            "indicator_name": "STOCH",
            "indicator_category": "MOMENTUM",
            "indicator_name_zh": "KD隨機指標",
            "description": "測量收盤價在高低區間的位置",
            "default_params": {
                "d": 3,
                "k": 9,
                "smooth_k": 3
            },
            "param_ranges": {
                "d": [
                    2,
                    5
                ],
                "k": [
                    5,
                    20
                ],
                "smooth_k": [
                    2,
                    5
                ]
            },
            "pandas_ta_function": "stoch",
            "min_data_points": 9,
            "output_fields": {
                "fields": [
                    "stoch_k",
                    "stoch_d"
                ]
            },
            "value_range": {
                "max": 100,
                "min": 0
            },
            "priority": "P0",
            "is_active": true,
            "is_cached": true,
            "created_at": "2026-01-03 02:13:16",
            "updated_at": "2026-01-03 18:05:10"
        }
    ],
    "timestamp": "2026-01-04T02:20:22.0317021+08:00"
}
```

---

#### API-M07-007: 查詢單一指標定義

**Request**:
```
GET /api/indicators/definitions/RSI
```

**Path Parameters**:
| 參數 | 類型 | 說明 |
|-----|------|------|
| indicatorName | String | 指標名稱 |

**Response** (成功):
```json
{
    "code": 200,
    "message": "Success",
    "data": {
        "definition_id": 4,
        "indicator_name": "RSI",
        "indicator_category": "MOMENTUM",
        "indicator_name_zh": "相對強弱指標",
        "description": "測量價格變動速度和幅度",
        "default_params": {
            "period": 14
        },
        "param_ranges": {
            "max_period": 30,
            "min_period": 5
        },
        "pandas_ta_function": "rsi",
        "min_data_points": 14,
        "output_fields": {
            "fields": [
                "rsi_14"
            ]
        },
        "value_range": {
            "max": 100,
            "min": 0
        },
        "priority": "P0",
        "is_active": true,
        "is_cached": true,
        "created_at": "2026-01-03 02:13:16",
        "updated_at": "2026-01-03 18:05:10"
    },
    "timestamp": "2026-01-04T02:20:42.7531066+08:00"
}
```

**Response** (指標定義不存在):
```json
{
    "code": 404,
    "message": "指標定義不存在: TEST",
    "error": {
        "details": "indicator",
        "field": "Please check the stock ID and calculation date",
        "suggestion": "Please check the Please check the stock ID and calculation date and try again",
        "error_code": "M07011",
        "error_type": "CLIENT_ERROR"
    },
    "timestamp": "2026-01-04T02:20:57.7069799+08:00",
    "trace_id": "req_05707746116e"
}
```

---

## Job 管理 API

#### API-M07-008: 手動觸發指標計算

**Request**:
```
POST /api/jobs/calculate-indicators
Content-Type: application/json

{
  "calculation_date": "2026-01-02",
  "stock_ids": ["2330", "2317"],
  "indicator_priority": "P0",
  "force_recalculate": false
}
```

**Request Body**:
| 欄位 | 類型 | 必填 | 說明 | 預設值 |
|-----|------|------|------|-------|
| calculation_date | String | Y | 計算日期（YYYY-MM-DD） | - |
| stock_ids | Array | N | 股票代碼清單（空則計算全部） | null |
| indicator_priority | String | N | 指標優先級（P0/P1/P2） | P0 |
| force_recalculate | Boolean | N | 是否強制重新計算 | false |

**Response** (成功):
```json
{
    "code": 200,
    "message": "Success",
    "data": {
        "job_id": 631,
        "job_type": "CALCULATE_INDICATORS",
        "calculation_date": "2026-01-02",
        "stock_list": [
            "2330",
            "2317"
        ],
        "indicator_priority": "P0",
        "status": "PENDING",
        "statistics": {},
        "created_at": "2026-01-04 02:22:11",
        "created_by": "SYSTEM"
    },
    "timestamp": "2026-01-04T02:22:11.9644305+08:00"
}
```

**Response** (未實作)(Job 已在執行中):
```json
{
  "code": 409,
  "message": "Job is already running",
  "error": {
    "details": "Another calculation job for date 2025-12-24 is currently running",
    "field": "calculation_date",
    "suggestion": "Please wait for the current job to complete or check job status",
    "error_code": "M07042",
    "error_type": "CLIENT_ERROR"
  },
  "timestamp": "2026-01-03T14:30:00+08:00",
  "trace_id": "req_0d1e2f3a4b5c"
}
```

**Response** (未實作)(資料不足無法計算):
```json
{
  "code": 422,
  "message": "Insufficient data for calculation",
  "error": {
    "details": "Stock 2330 requires at least 60 days of price data, but only 30 days available",
    "field": "stock_id",
    "suggestion": "Please ensure sufficient historical data is available",
    "error_code": "M07022",
    "error_type": "BUSINESS_ERROR"
  },
  "timestamp": "2026-01-03T14:30:00+08:00",
  "trace_id": "req_1e2f3a4b5c6d"
}
```

---

### 4.3 錯誤碼定義

遵守 [全系統契約 - 錯誤碼規範](../technical/00-全系統契約.md#4-api-統一規範)。

| 錯誤碼 | HTTP Status | 說明 | 處理建議 |
|-------|------------|------|---------|
| M07011 | 404 | 指標不存在 | 檢查股票代碼和計算日期 |
| M07012 | 400 | 無效的指標名稱 | 參照 /api/indicators/definitions |
| M07013 | 400 | 無效的指標類別 | 檢查類別名稱是否正確 |
| M07021 | 500 | 指標計算失敗 | 檢查股價資料完整性 |
| M07022 | 422 | 資料不足無法計算 | 確保有足夠的歷史資料 |
| M07023 | 400 | 無效的計算參數 | 檢查參數範圍 |
| M07031 | 404 | 指標定義不存在 | 檢查指標名稱 |
| M07032 | 409 | 指標定義已存在 | 使用不同的指標名稱 |
| M07041 | 404 | Job 執行記錄不存在 | 檢查 Job ID |
| M07042 | 409 | Job 已在執行中 | 等待當前 Job 完成 |

---

## 📚 相關文檔

- [全系統契約 - API 統一規範](../technical/00-全系統契約.md#4-api-統一規範)
- [M07 功能需求](../functional/M07-技術分析功能需求.md)
- [M07 資料庫設計](../../design/M07-資料庫設計.md)
- [M07 效能考量](../../design/M07-效能考量.md)

---

**文件維護者**: API 設計師  
**審核者**: 架構師  
**最後更新**: 2026-01-03  
**下次審核**: 2026-02-03
