# M12-總經與產業分析模組 API 規格

> **文件編號**: API-M12
> **模組名稱**: 總經與產業分析模組
> **版本**: v1.0
> **最後更新**: 2026-01-14
> **狀態**: Draft

---

## 1. API 總覽

### 1.1 基本資訊

| 項目 | 說明 |
|-----|------|
| Base URL | `/api/v1/macro-industry` |
| 認證方式 | Bearer Token (JWT) |
| 回應格式 | JSON |
| 字元編碼 | UTF-8 |

### 1.2 API 端點清單

| HTTP Method | 端點 | 說明 |
|-------------|------|------|
| GET | `/macro/indicators` | 查詢總經指標清單 |
| GET | `/macro/indicators/{code}` | 查詢單一總經指標歷史 |
| GET | `/macro/cycle` | 查詢當前經濟週期 |
| GET | `/macro/cycle/history` | 查詢經濟週期歷史 |
| GET | `/industry/sectors` | 查詢產業分類清單 |
| GET | `/industry/sectors/{code}` | 查詢單一產業詳情 |
| GET | `/industry/sectors/{code}/stocks` | 查詢產業成分股 |
| GET | `/industry/performance` | 查詢產業績效總覽 |
| GET | `/industry/performance/{code}` | 查詢單一產業績效 |
| GET | `/industry/ranking` | 查詢產業排行榜 |
| GET | `/industry/rotation` | 查詢產業輪動分析 |
| GET | `/industry/valuation` | 查詢產業估值比較 |
| GET | `/industry/signals` | 查詢產業信號 |
| GET | `/industry/signals/unconsumed` | 查詢未消費信號 (供 M13) |
| POST | `/industry/signals/consumed` | 標記信號已消費 (供 M13) |
| GET | `/themes` | 查詢自訂主題清單 |
| GET | `/themes/{code}/stocks` | 查詢主題成分股 |

---

## 2. 共用元件

### 2.1 標準回應格式

```json
{
  "code": 200,
  "message": "Success",
  "data": { },
  "timestamp": "2024-12-24T14:30:00+08:00",
  "trace_id": "req_m12_abc123"
}
```

### 2.2 錯誤碼定義

| 錯誤碼 | HTTP Status | 說明 |
|-------|-------------|------|
| M12_MACRO_001 | 404 | 總經指標不存在 |
| M12_MACRO_002 | 400 | 無效的日期範圍 |
| M12_IND_001 | 404 | 產業代碼不存在 |
| M12_IND_002 | 400 | 無效的排行榜類型 |
| M12_IND_003 | 400 | 無效的產業類型 |
| M12_THEME_001 | 404 | 主題不存在 |
| M12_PARAM_001 | 400 | 參數格式錯誤 |

---

## 3. API 詳細規格

### 3.1 總經指標 API

#### GET /macro/indicators

**說明**: 查詢總經指標清單與最新數值

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| region | String | 否 | 地區：TW, US, GLOBAL（預設 TW） |
| category | String | 否 | 類別：GDP, INFLATION, INTEREST, TRADE, MONEY |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "indicators": [
      {
        "indicator_code": "TW_GDP_YOY",
        "indicator_name": "GDP 年增率",
        "region": "TW",
        "category": "GDP",
        "latest_value": 3.25,
        "latest_date": "2024-09-30",
        "previous_value": 2.85,
        "previous_date": "2024-06-30",
        "change": 0.40,
        "unit": "%",
        "frequency": "QUARTERLY",
        "source": "主計總處"
      },
      {
        "indicator_code": "TW_CPI_YOY",
        "indicator_name": "CPI 年增率",
        "region": "TW",
        "category": "INFLATION",
        "latest_value": 2.15,
        "latest_date": "2024-11-30",
        "previous_value": 1.98,
        "previous_date": "2024-10-31",
        "change": 0.17,
        "unit": "%",
        "frequency": "MONTHLY",
        "source": "主計總處"
      }
    ],
    "total": 13,
    "updated_at": "2024-12-24T09:00:00+08:00"
  }
}
```

---

#### GET /macro/indicators/{code}

**說明**: 查詢單一總經指標的歷史數據

**Path Parameters**:

| 參數 | 類型 | 說明 |
|-----|------|------|
| code | String | 總經指標代碼 |

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| start_date | String | 否 | 開始日期 (YYYY-MM-DD) |
| end_date | String | 否 | 結束日期 (YYYY-MM-DD) |
| limit | Integer | 否 | 筆數限制（預設 24） |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "indicator_code": "TW_GDP_YOY",
    "indicator_name": "GDP 年增率",
    "region": "TW",
    "category": "GDP",
    "unit": "%",
    "frequency": "QUARTERLY",
    "source": "主計總處",
    "history": [
      {
        "date": "2024-09-30",
        "value": 3.25,
        "yoy_change": 0.40
      },
      {
        "date": "2024-06-30",
        "value": 2.85,
        "yoy_change": 0.15
      }
    ],
    "statistics": {
      "avg_5y": 2.45,
      "max_5y": 6.25,
      "min_5y": -0.85,
      "current_percentile": 65
    }
  }
}
```

---

#### GET /macro/cycle

**說明**: 查詢當前經濟週期判斷

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "current_stage": "EXPANSION",
    "stage_name": "擴張期",
    "confidence": 0.78,
    "stage_duration_months": 8,
    "analysis_date": "2024-12-24",
    "key_indicators": {
      "leading_index": {
        "value": 102.5,
        "trend": "UP",
        "months_in_trend": 6
      },
      "coincident_index": {
        "value": 98.3,
        "level": "MID_HIGH",
        "percentile": 68
      },
      "monitor_score": {
        "value": 28,
        "signal": "GREEN_YELLOW",
        "description": "景氣穩定"
      }
    },
    "investment_implications": {
      "recommended_sectors": ["科技", "非必需消費"],
      "avoid_sectors": ["公用事業", "必需消費"],
      "strategy": "維持股票部位，關注成長股"
    },
    "previous_stage": {
      "stage": "RECOVERY",
      "ended_at": "2024-04-30"
    }
  }
}
```

---

#### GET /macro/cycle/history

**說明**: 查詢經濟週期歷史變化

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| years | Integer | 否 | 查詢年數（預設 5） |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "cycles": [
      {
        "stage": "EXPANSION",
        "start_date": "2024-05-01",
        "end_date": null,
        "duration_months": 8,
        "is_current": true
      },
      {
        "stage": "RECOVERY",
        "start_date": "2023-09-01",
        "end_date": "2024-04-30",
        "duration_months": 8,
        "is_current": false
      },
      {
        "stage": "RECESSION",
        "start_date": "2022-10-01",
        "end_date": "2023-08-31",
        "duration_months": 11,
        "is_current": false
      }
    ],
    "total_cycles": 5,
    "avg_cycle_duration_months": 12.5
  }
}
```

---

### 3.2 產業分類 API

#### GET /industry/sectors

**說明**: 查詢產業分類清單

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| level | Integer | 否 | 分類層級（1: 大類, 2: 中類） |
| parent_code | String | 否 | 父分類代碼 |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "sectors": [
      {
        "sector_code": "24",
        "sector_name": "半導體業",
        "level": 2,
        "parent_code": "E",
        "parent_name": "電子工業",
        "stock_count": 98,
        "total_market_cap": 35800000000000,
        "weight_in_market": 42.5
      },
      {
        "sector_code": "25",
        "sector_name": "電腦及週邊設備業",
        "level": 2,
        "parent_code": "E",
        "parent_name": "電子工業",
        "stock_count": 45,
        "total_market_cap": 5200000000000,
        "weight_in_market": 6.2
      }
    ],
    "total": 30
  }
}
```

---

#### GET /industry/sectors/{code}

**說明**: 查詢單一產業詳細資訊

**Path Parameters**:

| 參數 | 類型 | 說明 |
|-----|------|------|
| code | String | 產業代碼 |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "sector_code": "24",
    "sector_name": "半導體業",
    "level": 2,
    "parent_code": "E",
    "parent_name": "電子工業",
    "description": "從事積體電路設計、製造、封裝測試之上市公司",
    "stock_count": 98,
    "total_market_cap": 35800000000000,
    "weight_in_market": 42.5,
    "top_stocks": [
      {
        "stock_id": "2330",
        "stock_name": "台積電",
        "market_cap": 25000000000000,
        "weight_in_sector": 69.8
      },
      {
        "stock_id": "2454",
        "stock_name": "聯發科",
        "market_cap": 1800000000000,
        "weight_in_sector": 5.0
      }
    ],
    "sub_sectors": [
      {
        "sector_code": "2401",
        "sector_name": "IC 設計",
        "stock_count": 35
      },
      {
        "sector_code": "2402",
        "sector_name": "晶圓代工",
        "stock_count": 8
      }
    ]
  }
}
```

---

#### GET /industry/sectors/{code}/stocks

**說明**: 查詢產業成分股清單

**Path Parameters**:

| 參數 | 類型 | 說明 |
|-----|------|------|
| code | String | 產業代碼 |

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| sort_by | String | 否 | 排序欄位：market_cap, return_1d, return_20d |
| order | String | 否 | 排序方向：asc, desc（預設 desc） |
| page | Integer | 否 | 頁碼（預設 1） |
| size | Integer | 否 | 每頁筆數（預設 20） |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "sector_code": "24",
    "sector_name": "半導體業",
    "stocks": [
      {
        "stock_id": "2330",
        "stock_name": "台積電",
        "market_cap": 25000000000000,
        "weight": 69.8,
        "close": 1050.0,
        "change": 15.0,
        "change_percent": 1.45,
        "return_5d": 3.2,
        "return_20d": 8.5,
        "pe_ratio": 25.5,
        "pb_ratio": 6.8
      }
    ],
    "total": 98,
    "page": 1,
    "size": 20,
    "total_pages": 5
  }
}
```

---

### 3.3 產業績效 API

#### GET /industry/performance

**說明**: 查詢所有產業績效總覽

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| date | String | 否 | 查詢日期（預設最新） |
| level | Integer | 否 | 產業層級（1 或 2） |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "trade_date": "2024-12-24",
    "market_summary": {
      "taiex_close": 22500.50,
      "taiex_change": 1.25,
      "total_volume": 285000000000,
      "advance_count": 580,
      "decline_count": 350,
      "unchanged_count": 70
    },
    "sectors": [
      {
        "sector_code": "24",
        "sector_name": "半導體業",
        "return_1d": 1.85,
        "return_5d": 4.25,
        "return_20d": 12.50,
        "return_60d": 25.80,
        "return_ytd": 45.30,
        "momentum_20d": 1.52,
        "relative_strength": 8.50,
        "breadth": 72.5,
        "volume_change": 15.2
      },
      {
        "sector_code": "17",
        "sector_name": "金融保險業",
        "return_1d": 0.65,
        "return_5d": 1.20,
        "return_20d": 3.80,
        "return_60d": 8.50,
        "return_ytd": 15.20,
        "momentum_20d": 0.85,
        "relative_strength": -2.30,
        "breadth": 58.5,
        "volume_change": -5.8
      }
    ],
    "total_sectors": 30
  }
}
```

---

#### GET /industry/performance/{code}

**說明**: 查詢單一產業績效歷史

**Path Parameters**:

| 參數 | 類型 | 說明 |
|-----|------|------|
| code | String | 產業代碼 |

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| start_date | String | 否 | 開始日期 |
| end_date | String | 否 | 結束日期 |
| period | String | 否 | 期間：1M, 3M, 6M, 1Y, 3Y（預設 3M） |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "sector_code": "24",
    "sector_name": "半導體業",
    "period": "3M",
    "summary": {
      "total_return": 18.50,
      "annualized_return": 74.0,
      "volatility": 22.5,
      "sharpe_ratio": 2.85,
      "max_drawdown": -8.5,
      "win_rate": 62.5
    },
    "vs_benchmark": {
      "benchmark": "TAIEX",
      "alpha": 8.25,
      "beta": 1.15,
      "correlation": 0.85
    },
    "daily_performance": [
      {
        "date": "2024-12-24",
        "return": 1.85,
        "cumulative_return": 18.50,
        "vs_benchmark": 0.60
      }
    ]
  }
}
```

---

### 3.4 產業排行榜 API

#### GET /industry/ranking

**說明**: 查詢產業排行榜

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| type | String | 是 | 排行類型：RETURN_1D, RETURN_5D, RETURN_20D, MOMENTUM, RELATIVE_STRENGTH, MONEY_FLOW, BREADTH, VALUATION |
| date | String | 否 | 查詢日期（預設最新） |
| limit | Integer | 否 | 排名數量（預設 10） |
| order | String | 否 | 排序：asc, desc（預設 desc） |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "ranking_type": "RETURN_20D",
    "ranking_name": "月漲跌幅排行",
    "trade_date": "2024-12-24",
    "rankings": [
      {
        "rank": 1,
        "sector_code": "24",
        "sector_name": "半導體業",
        "value": 12.50,
        "previous_rank": 2,
        "rank_change": 1
      },
      {
        "rank": 2,
        "sector_code": "26",
        "sector_name": "光電業",
        "value": 10.85,
        "previous_rank": 1,
        "rank_change": -1
      },
      {
        "rank": 3,
        "sector_code": "31",
        "sector_name": "航運業",
        "value": 8.25,
        "previous_rank": 5,
        "rank_change": 2
      }
    ],
    "total": 10
  }
}
```

---

### 3.5 產業輪動 API

#### GET /industry/rotation

**說明**: 查詢產業輪動分析

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| weeks | Integer | 否 | 分析週數（預設 12） |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "analysis_date": "2024-12-24",
    "analysis_period_weeks": 12,
    "rotation_summary": {
      "leading_sectors": [
        {
          "sector_code": "24",
          "sector_name": "半導體業",
          "weeks_in_top": 8,
          "avg_rank": 2.5,
          "momentum_score": 85.5
        }
      ],
      "improving_sectors": [
        {
          "sector_code": "31",
          "sector_name": "航運業",
          "rank_improvement": 8,
          "current_rank": 5,
          "momentum_acceleration": 12.5
        }
      ],
      "weakening_sectors": [
        {
          "sector_code": "01",
          "sector_name": "水泥工業",
          "rank_decline": 6,
          "current_rank": 22,
          "momentum_deceleration": -8.5
        }
      ],
      "lagging_sectors": [
        {
          "sector_code": "14",
          "sector_name": "建材營造業",
          "weeks_in_bottom": 6,
          "avg_rank": 25.5,
          "momentum_score": 25.0
        }
      ]
    },
    "rotation_matrix": [
      {
        "sector_code": "24",
        "sector_name": "半導體業",
        "weekly_ranks": [2, 1, 2, 3, 2, 1, 2, 2, 3, 2, 1, 2],
        "trend": "STABLE_LEADING"
      }
    ],
    "cycle_implication": {
      "current_stage": "EXPANSION",
      "favored_sectors": ["科技", "非必需消費", "工業"],
      "rotation_signal": "科技股持續領漲，符合擴張期特徵"
    }
  }
}
```

---

### 3.6 產業估值 API

#### GET /industry/valuation

**說明**: 查詢產業估值比較

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| date | String | 否 | 查詢日期（預設最新） |
| sort_by | String | 否 | 排序欄位：pe_ratio, pb_ratio, dividend_yield |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "trade_date": "2024-12-24",
    "market_valuation": {
      "pe_ratio": 16.5,
      "pb_ratio": 2.1,
      "dividend_yield": 3.2
    },
    "sectors": [
      {
        "sector_code": "24",
        "sector_name": "半導體業",
        "valuation": {
          "pe_ratio": 22.5,
          "pe_percentile": 55,
          "pe_5y_avg": 20.8,
          "pb_ratio": 4.8,
          "pb_percentile": 62,
          "dividend_yield": 1.8,
          "dy_percentile": 35
        },
        "vs_market": {
          "pe_premium": 36.4,
          "pb_premium": 128.6
        },
        "valuation_signal": "FAIR",
        "valuation_comment": "估值處於歷史中位數附近"
      },
      {
        "sector_code": "17",
        "sector_name": "金融保險業",
        "valuation": {
          "pe_ratio": 12.5,
          "pe_percentile": 25,
          "pe_5y_avg": 14.2,
          "pb_ratio": 1.2,
          "pb_percentile": 30,
          "dividend_yield": 4.5,
          "dy_percentile": 72
        },
        "vs_market": {
          "pe_premium": -24.2,
          "pb_premium": -42.9
        },
        "valuation_signal": "UNDERVALUED",
        "valuation_comment": "估值低於歷史均值，具投資價值"
      }
    ]
  }
}
```

---

### 3.7 產業信號 API

#### GET /industry/signals

**說明**: 查詢產業相關信號

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| date | String | 否 | 查詢日期（預設最新） |
| signal_type | String | 否 | 信號類型：INDUSTRY_ROTATION, INDUSTRY_MOMENTUM, INDUSTRY_VALUATION, MACRO_CYCLE |
| sector_code | String | 否 | 產業代碼 |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "signals": [
      {
        "signal_id": "IND_SIG_2024122401",
        "signal_type": "INDUSTRY_ROTATION",
        "signal_code": "IND_SIG_003",
        "signal_name": "新興領漲產業",
        "target_type": "SECTOR",
        "target_id": "31",
        "target_name": "航運業",
        "signal_date": "2024-12-24",
        "signal_value": {
          "current_rank": 5,
          "previous_rank": 13,
          "rank_change": 8,
          "momentum_score": 75.5
        },
        "confidence_score": 0.78,
        "description": "航運業連續 3 週排名上升，進入產業前 20%",
        "created_at": "2024-12-24T17:00:00+08:00"
      },
      {
        "signal_id": "MACRO_SIG_2024122401",
        "signal_type": "MACRO_CYCLE",
        "signal_code": "MACRO_SIG_003",
        "signal_name": "景氣燈號變化",
        "target_type": "MACRO",
        "target_id": "TW_MONITOR",
        "target_name": "台灣景氣燈號",
        "signal_date": "2024-12-24",
        "signal_value": {
          "previous_signal": "YELLOW_BLUE",
          "current_signal": "GREEN_YELLOW",
          "change_direction": "IMPROVING"
        },
        "confidence_score": 0.95,
        "description": "景氣燈號由黃藍燈轉為綠黃燈，景氣持續改善",
        "created_at": "2024-12-24T09:30:00+08:00"
      }
    ],
    "total": 5
  }
}
```

---

#### GET /industry/signals/unconsumed

**說明**: 查詢未被 M13 消費的產業信號

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| date | String | 否 | 查詢日期（預設最新） |
| limit | Integer | 否 | 筆數限制（預設 100） |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "signals": [
      {
        "signal_id": "IND_SIG_2024122401",
        "signal_type": "INDUSTRY_ROTATION",
        "signal_code": "IND_SIG_003",
        "target_type": "SECTOR",
        "target_id": "31",
        "target_name": "航運業",
        "signal_date": "2024-12-24",
        "confidence_score": 0.78,
        "is_consumed": false,
        "created_at": "2024-12-24T17:00:00+08:00"
      }
    ],
    "total_unconsumed": 3
  }
}
```

---

#### POST /industry/signals/consumed

**說明**: 標記信號已被 M13 消費

**Request Body**:
```json
{
  "signal_ids": ["IND_SIG_2024122401", "IND_SIG_2024122402"]
}
```

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "consumed_count": 2,
    "consumed_at": "2024-12-24T17:30:00+08:00"
  }
}
```

---

### 3.8 主題分類 API

#### GET /themes

**說明**: 查詢自訂主題清單

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "themes": [
      {
        "theme_code": "AI_CONCEPT",
        "theme_name": "AI 概念股",
        "description": "人工智慧相關概念股",
        "stock_count": 35,
        "total_market_cap": 28500000000000,
        "return_20d": 15.5,
        "is_active": true
      },
      {
        "theme_code": "EV_CONCEPT",
        "theme_name": "電動車概念",
        "description": "電動車供應鏈相關",
        "stock_count": 48,
        "total_market_cap": 8500000000000,
        "return_20d": 8.2,
        "is_active": true
      }
    ],
    "total": 10
  }
}
```

---

#### GET /themes/{code}/stocks

**說明**: 查詢主題成分股清單

**Path Parameters**:

| 參數 | 類型 | 說明 |
|-----|------|------|
| code | String | 主題代碼 |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "theme_code": "AI_CONCEPT",
    "theme_name": "AI 概念股",
    "stocks": [
      {
        "stock_id": "2330",
        "stock_name": "台積電",
        "inclusion_reason": "AI 晶片代工領導者",
        "market_cap": 25000000000000,
        "weight": 87.7,
        "return_20d": 12.5
      },
      {
        "stock_id": "2454",
        "stock_name": "聯發科",
        "inclusion_reason": "AI 邊緣運算晶片",
        "market_cap": 1800000000000,
        "weight": 6.3,
        "return_20d": 18.2
      }
    ],
    "total": 35
  }
}
```

---

## 4. 錯誤回應範例

### 4.1 產業不存在

```json
{
  "code": 404,
  "message": "產業代碼不存在",
  "error": {
    "error_code": "M12_IND_001",
    "error_message": "找不到產業代碼: XX",
    "suggestion": "請確認產業代碼是否正確"
  },
  "timestamp": "2024-12-24T14:30:00+08:00",
  "trace_id": "req_m12_err001"
}
```

### 4.2 無效的排行榜類型

```json
{
  "code": 400,
  "message": "無效的排行榜類型",
  "error": {
    "error_code": "M12_IND_002",
    "error_message": "不支援的排行榜類型: INVALID_TYPE",
    "valid_types": ["RETURN_1D", "RETURN_5D", "RETURN_20D", "MOMENTUM", "RELATIVE_STRENGTH", "MONEY_FLOW", "BREADTH", "VALUATION"]
  },
  "timestamp": "2024-12-24T14:30:00+08:00",
  "trace_id": "req_m12_err002"
}
```

---

## 📚 相關文檔

- [M12 功能需求](../functional/M12-總經產業分析功能需求.md)
- [M12 資料庫設計](../../design/M12-資料庫設計.md)
- [全系統 API 契約](../technical/00-全系統契約.md)
- [M13 信號引擎 API](./M13-API規格.md)

---

**文件維護者**: 後端工程師
**最後更新**: 2026-01-14
**下次審核**: 2026-04-14
