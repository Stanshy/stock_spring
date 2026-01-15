# M13-信號判斷引擎 API 規格

> **文件編號**: API-M13
> **模組名稱**: 信號判斷引擎 (Signal Judgment Engine)
> **版本**: v1.0
> **最後更新**: 2026-01-14
> **狀態**: Draft

---

## 1. API 總覽

| # | API | Method | 路徑 | 說明 |
|---|-----|--------|-----|------|
| 1 | 手動觸發信號收集 | POST | `/api/m13/signals/collect` | 觸發從上游模組收集信號 |
| 2 | 查詢統一信號 | GET | `/api/m13/signals/unified` | 查詢統一信號清單 |
| 3 | 查詢單一信號詳情 | GET | `/api/m13/signals/unified/{signalId}` | 查詢單一統一信號完整資訊 |
| 4 | 查詢股票信號 | GET | `/api/m13/signals/stock/{stockId}` | 查詢指定股票的所有信號 |
| 5 | 每日推薦清單 | GET | `/api/m13/recommendations/daily` | 取得當日推薦買賣清單 |
| 6 | 歷史推薦清單 | GET | `/api/m13/recommendations/history` | 查詢歷史推薦清單 |
| 7 | 信號消費標記 | POST | `/api/m13/signals/unified/{signalId}/consume` | 標記信號已被消費 |
| 8 | 批次信號消費 | POST | `/api/m13/signals/unified/consume-batch` | 批次標記多個信號已消費 |
| 9 | 信號統計概覽 | GET | `/api/m13/statistics/overview` | 取得信號統計概覽 |
| 10 | 評級分布統計 | GET | `/api/m13/statistics/grades` | 取得評級分布統計 |
| 11 | 來源模組統計 | GET | `/api/m13/statistics/sources` | 取得各來源模組信號統計 |
| 12 | 查詢原始信號 | GET | `/api/m13/signals/raw` | 查詢收集的原始信號 |
| 13 | 信號追溯 | GET | `/api/m13/signals/unified/{signalId}/trace` | 追溯統一信號的原始來源 |
| 14 | 信號評分解析 | GET | `/api/m13/signals/unified/{signalId}/scoring` | 查詢信號評分明細 |
| 15 | 手動重算信號 | POST | `/api/m13/signals/recalculate` | 重新計算指定日期的信號 |
| 16 | 信號訂閱配置 | POST | `/api/m13/subscriptions` | 配置信號訂閱條件 |
| 17 | 查詢訂閱列表 | GET | `/api/m13/subscriptions` | 查詢信號訂閱配置 |
| 18 | 刪除訂閱 | DELETE | `/api/m13/subscriptions/{subscriptionId}` | 刪除信號訂閱 |

---

## 2. API 詳細規格

### 2.1 手動觸發信號收集

**端點**: `POST /api/m13/signals/collect`

**說明**: 手動觸發從上游模組收集信號，通常用於測試或即時更新。

**Request Body**:
```json
{
  "trade_date": "2024-12-24",
  "source_modules": ["M07", "M08", "M09"],
  "force_recollect": false
}
```

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| trade_date | string | Y | 收集信號的交易日期（YYYY-MM-DD） |
| source_modules | array | N | 指定來源模組，不填則收集全部 |
| force_recollect | boolean | N | 是否強制重新收集（預設 false） |

**Response**:
```json
{
  "code": 200,
  "message": "Signal collection completed",
  "data": {
    "trade_date": "2024-12-24",
    "collection_summary": {
      "M07": { "collected": 150, "new": 120, "duplicated": 30 },
      "M08": { "collected": 80, "new": 75, "duplicated": 5 },
      "M09": { "collected": 200, "new": 180, "duplicated": 20 },
      "M10": { "collected": 45, "new": 40, "duplicated": 5 },
      "M11": { "collected": 60, "new": 55, "duplicated": 5 },
      "M12": { "collected": 30, "new": 28, "duplicated": 2 }
    },
    "total_collected": 565,
    "total_new": 498,
    "execution_time_ms": 3200,
    "collected_at": "2024-12-24T17:00:00+08:00"
  },
  "timestamp": "2024-12-24T17:00:03+08:00"
}
```

---

### 2.2 查詢統一信號

**端點**: `GET /api/m13/signals/unified`

**說明**: 多維度查詢統一信號清單。

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| trade_date | string | N | 交易日期（YYYY-MM-DD） |
| start_date | string | N | 起始日期 |
| end_date | string | N | 結束日期 |
| stock_id | string | N | 股票代碼 |
| direction | string | N | 信號方向（BUY/SELL/HOLD） |
| grade | string | N | 評級（A+/A/B+/B/C/D） |
| min_grade | string | N | 最低評級 |
| min_score | number | N | 最低評分 |
| signal_types | string | N | 信號類型標籤（逗號分隔） |
| source_modules | string | N | 來源模組（逗號分隔） |
| direction_strength | string | N | 方向強度（STRONG/MODERATE/WEAK/CONFLICT） |
| is_consumed | boolean | N | 是否已消費 |
| sector_code | string | N | 產業代碼 |
| page | integer | N | 頁碼（預設 0） |
| size | integer | N | 每頁筆數（預設 20，最大 100） |
| sort | string | N | 排序欄位（預設 unified_score,desc） |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "signal_id": "UNI_2024122401_2330",
        "stock_id": "2330",
        "stock_name": "台積電",
        "trade_date": "2024-12-24",
        "unified_direction": "BUY",
        "direction_strength": "STRONG",
        "unified_score": 92.5,
        "grade": "A+",
        "dimension_coverage": 5,
        "unified_confidence": 0.82,
        "signal_types": ["MOMENTUM", "INSTITUTIONAL", "VALUE"],
        "key_factors": ["外資連買 5 日", "RSI 超賣反彈", "PE 歷史低位"],
        "source_modules": ["M07", "M08", "M09", "M10", "M11"],
        "is_consumed": false,
        "created_at": "2024-12-24T17:30:00+08:00"
      },
      {
        "signal_id": "UNI_2024122401_2454",
        "stock_id": "2454",
        "stock_name": "聯發科",
        "trade_date": "2024-12-24",
        "unified_direction": "BUY",
        "direction_strength": "STRONG",
        "unified_score": 88.3,
        "grade": "A",
        "dimension_coverage": 4,
        "unified_confidence": 0.78,
        "signal_types": ["STRATEGY", "SECTOR"],
        "key_factors": ["動能策略觸發", "產業轉強"],
        "source_modules": ["M07", "M11", "M12"],
        "is_consumed": false,
        "created_at": "2024-12-24T17:30:00+08:00"
      }
    ],
    "page": 0,
    "size": 20,
    "total_elements": 120,
    "total_pages": 6
  },
  "timestamp": "2024-12-24T17:35:00+08:00"
}
```

---

### 2.3 查詢單一信號詳情

**端點**: `GET /api/m13/signals/unified/{signalId}`

**說明**: 查詢單一統一信號的完整詳細資訊。

**Path Parameters**:

| 參數 | 類型 | 說明 |
|-----|------|------|
| signalId | string | 統一信號 ID |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "signal_id": "UNI_2024122401_2330",
    "stock_id": "2330",
    "stock_name": "台積電",
    "sector_code": "SEMICONDUCTOR",
    "sector_name": "半導體",
    "trade_date": "2024-12-24",
    "unified_direction": "BUY",
    "direction_strength": "STRONG",
    "unified_score": 92.5,
    "grade": "A+",
    "dimension_coverage": 5,
    "unified_confidence": 0.82,
    "signal_types": ["MOMENTUM", "INSTITUTIONAL", "VALUE", "PATTERN"],
    "validity_period": "SHORT_TERM",
    "key_factors": [
      "外資連買 5 日",
      "RSI 超賣反彈 (25.5)",
      "PE 歷史低位 (12.8)",
      "雙底型態完成"
    ],
    "contributing_signals": [
      {
        "source_signal_id": "TECH_SIG_2024122401",
        "source_module": "M07",
        "signal_type": "TECHNICAL",
        "signal_code": "RSI_OVERSOLD",
        "signal_name": "RSI 超賣反彈",
        "signal_direction": "BUY",
        "source_confidence": 0.75,
        "weight": 0.20
      },
      {
        "source_signal_id": "CHIP_SIG_2024122402",
        "source_module": "M09",
        "signal_type": "CHIP",
        "signal_code": "FOREIGN_CONTINUOUS_BUY",
        "signal_name": "外資連續買超",
        "signal_direction": "BUY",
        "source_confidence": 0.82,
        "weight": 0.20
      }
    ],
    "scoring_breakdown": {
      "signal_strength": { "score": 95.0, "weight": 0.30, "weighted": 28.5 },
      "dimension_coverage": { "score": 90.0, "weight": 0.25, "weighted": 22.5 },
      "historical_performance": { "score": 85.0, "weight": 0.20, "weighted": 17.0 },
      "market_environment": { "score": 88.0, "weight": 0.15, "weighted": 13.2 },
      "freshness": { "score": 95.0, "weight": 0.10, "weighted": 9.5 }
    },
    "consumption_status": {
      "is_consumed": false,
      "consumers": []
    },
    "created_at": "2024-12-24T17:30:00+08:00",
    "updated_at": "2024-12-24T17:30:00+08:00"
  },
  "timestamp": "2024-12-24T17:35:00+08:00"
}
```

---

### 2.4 查詢股票信號

**端點**: `GET /api/m13/signals/stock/{stockId}`

**說明**: 查詢指定股票的所有統一信號歷史。

**Path Parameters**:

| 參數 | 類型 | 說明 |
|-----|------|------|
| stockId | string | 股票代碼 |

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| start_date | string | N | 起始日期（預設 30 天前） |
| end_date | string | N | 結束日期（預設今日） |
| direction | string | N | 信號方向 |
| min_grade | string | N | 最低評級 |
| page | integer | N | 頁碼 |
| size | integer | N | 每頁筆數 |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "stock_id": "2330",
    "stock_name": "台積電",
    "signals": [
      {
        "signal_id": "UNI_2024122401_2330",
        "trade_date": "2024-12-24",
        "unified_direction": "BUY",
        "unified_score": 92.5,
        "grade": "A+",
        "key_factors": ["外資連買", "RSI 超賣反彈"]
      },
      {
        "signal_id": "UNI_2024122001_2330",
        "trade_date": "2024-12-20",
        "unified_direction": "HOLD",
        "unified_score": 55.2,
        "grade": "C",
        "key_factors": ["信號衝突"]
      }
    ],
    "summary": {
      "total_signals": 15,
      "buy_signals": 8,
      "sell_signals": 3,
      "hold_signals": 4,
      "avg_score": 72.5,
      "best_grade": "A+",
      "latest_direction": "BUY"
    },
    "page": 0,
    "size": 20,
    "total_elements": 15,
    "total_pages": 1
  },
  "timestamp": "2024-12-24T17:35:00+08:00"
}
```

---

### 2.5 每日推薦清單

**端點**: `GET /api/m13/recommendations/daily`

**說明**: 取得指定日期的買賣推薦清單。

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| trade_date | string | N | 交易日期（預設今日） |
| top_n | integer | N | 取前 N 名（預設 20） |
| min_grade | string | N | 最低評級（預設 B） |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "trade_date": "2024-12-24",
    "generated_at": "2024-12-24T17:30:00+08:00",
    "buy_recommendations": [
      {
        "rank": 1,
        "stock_id": "2330",
        "stock_name": "台積電",
        "sector_name": "半導體",
        "unified_score": 92.5,
        "grade": "A+",
        "direction_strength": "STRONG",
        "dimension_coverage": 5,
        "key_factors": ["外資連買 5 日", "RSI 超賣反彈", "PE 歷史低位"],
        "signal_types": ["MOMENTUM", "INSTITUTIONAL", "VALUE"]
      },
      {
        "rank": 2,
        "stock_id": "2454",
        "stock_name": "聯發科",
        "sector_name": "半導體",
        "unified_score": 88.3,
        "grade": "A",
        "direction_strength": "STRONG",
        "dimension_coverage": 4,
        "key_factors": ["動能策略觸發", "產業轉強"],
        "signal_types": ["STRATEGY", "SECTOR"]
      }
    ],
    "sell_recommendations": [
      {
        "rank": 1,
        "stock_id": "2317",
        "stock_name": "鴻海",
        "sector_name": "電子零組件",
        "unified_score": 85.5,
        "grade": "A",
        "direction_strength": "STRONG",
        "dimension_coverage": 4,
        "key_factors": ["外資連賣 8 日", "跌破季線"],
        "signal_types": ["INSTITUTIONAL", "MOMENTUM"]
      }
    ],
    "summary": {
      "total_buy_signals": 45,
      "total_sell_signals": 12,
      "avg_buy_score": 78.5,
      "avg_sell_score": 72.3,
      "market_sentiment": "BULLISH"
    }
  },
  "timestamp": "2024-12-24T17:35:00+08:00"
}
```

---

### 2.6 歷史推薦清單

**端點**: `GET /api/m13/recommendations/history`

**說明**: 查詢歷史推薦清單以進行績效追蹤。

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| start_date | string | Y | 起始日期 |
| end_date | string | Y | 結束日期 |
| direction | string | N | 方向篩選（BUY/SELL） |
| min_grade | string | N | 最低評級 |
| include_performance | boolean | N | 是否包含績效統計（預設 false） |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "recommendations": [
      {
        "trade_date": "2024-12-24",
        "buy_count": 45,
        "sell_count": 12,
        "top_buy": { "stock_id": "2330", "score": 92.5, "grade": "A+" },
        "top_sell": { "stock_id": "2317", "score": 85.5, "grade": "A" }
      },
      {
        "trade_date": "2024-12-23",
        "buy_count": 38,
        "sell_count": 15,
        "top_buy": { "stock_id": "2454", "score": 88.0, "grade": "A" },
        "top_sell": { "stock_id": "2882", "score": 82.0, "grade": "A" }
      }
    ],
    "performance_summary": {
      "period": "2024-12-01 ~ 2024-12-24",
      "total_buy_signals": 520,
      "total_sell_signals": 180,
      "buy_accuracy": 0.68,
      "sell_accuracy": 0.62,
      "avg_return_buy_5d": 0.025,
      "avg_return_sell_5d": -0.018
    }
  },
  "timestamp": "2024-12-24T17:35:00+08:00"
}
```

---

### 2.7 信號消費標記

**端點**: `POST /api/m13/signals/unified/{signalId}/consume`

**說明**: 標記信號已被下游模組消費。

**Path Parameters**:

| 參數 | 類型 | 說明 |
|-----|------|------|
| signalId | string | 統一信號 ID |

**Request Body**:
```json
{
  "consumer_module": "M14",
  "consumer_action": "STOCK_SCREENING",
  "notes": "Used for daily screening"
}
```

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| consumer_module | string | Y | 消費模組代碼（M14-M18） |
| consumer_action | string | N | 消費動作說明 |
| notes | string | N | 備註 |

**Response**:
```json
{
  "code": 200,
  "message": "Signal marked as consumed",
  "data": {
    "signal_id": "UNI_2024122401_2330",
    "consumed_by": "M14",
    "consumed_at": "2024-12-24T18:00:00+08:00",
    "is_consumed": true,
    "total_consumers": 1
  },
  "timestamp": "2024-12-24T18:00:01+08:00"
}
```

---

### 2.8 批次信號消費

**端點**: `POST /api/m13/signals/unified/consume-batch`

**說明**: 批次標記多個信號已消費。

**Request Body**:
```json
{
  "signal_ids": [
    "UNI_2024122401_2330",
    "UNI_2024122401_2454",
    "UNI_2024122401_2317"
  ],
  "consumer_module": "M14",
  "consumer_action": "BATCH_SCREENING"
}
```

**Response**:
```json
{
  "code": 200,
  "message": "Batch consumption completed",
  "data": {
    "total_requested": 3,
    "successfully_consumed": 3,
    "already_consumed": 0,
    "failed": 0,
    "consumed_at": "2024-12-24T18:00:00+08:00"
  },
  "timestamp": "2024-12-24T18:00:01+08:00"
}
```

---

### 2.9 信號統計概覽

**端點**: `GET /api/m13/statistics/overview`

**說明**: 取得信號統計概覽。

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| trade_date | string | N | 統計日期（預設今日） |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "trade_date": "2024-12-24",
    "collection_stats": {
      "total_raw_signals": 565,
      "after_deduplication": 420,
      "deduplication_rate": 0.257
    },
    "unified_signal_stats": {
      "total_unified_signals": 180,
      "buy_signals": 120,
      "sell_signals": 45,
      "hold_signals": 15
    },
    "direction_strength_distribution": {
      "STRONG": 65,
      "MODERATE": 72,
      "WEAK": 28,
      "CONFLICT": 15
    },
    "grade_distribution": {
      "A+": 8,
      "A": 22,
      "B+": 45,
      "B": 55,
      "C": 35,
      "D": 15
    },
    "avg_score": 68.5,
    "avg_dimension_coverage": 3.2,
    "consumption_rate": 0.45,
    "generated_at": "2024-12-24T17:30:00+08:00"
  },
  "timestamp": "2024-12-24T17:35:00+08:00"
}
```

---

### 2.10 評級分布統計

**端點**: `GET /api/m13/statistics/grades`

**說明**: 取得評級分布詳細統計。

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| start_date | string | N | 起始日期 |
| end_date | string | N | 結束日期 |
| direction | string | N | 方向篩選 |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "period": "2024-12-01 ~ 2024-12-24",
    "total_signals": 3200,
    "grade_breakdown": [
      {
        "grade": "A+",
        "count": 180,
        "percentage": 0.056,
        "avg_score": 93.5,
        "buy_count": 150,
        "sell_count": 30,
        "accuracy_rate": 0.78
      },
      {
        "grade": "A",
        "count": 520,
        "percentage": 0.163,
        "avg_score": 84.2,
        "buy_count": 380,
        "sell_count": 140,
        "accuracy_rate": 0.72
      },
      {
        "grade": "B+",
        "count": 780,
        "percentage": 0.244,
        "avg_score": 74.5,
        "buy_count": 550,
        "sell_count": 230,
        "accuracy_rate": 0.65
      }
    ],
    "trend": {
      "avg_grade_score_7d": 72.5,
      "avg_grade_score_30d": 70.8,
      "trend_direction": "IMPROVING"
    }
  },
  "timestamp": "2024-12-24T17:35:00+08:00"
}
```

---

### 2.11 來源模組統計

**端點**: `GET /api/m13/statistics/sources`

**說明**: 取得各來源模組信號統計。

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| trade_date | string | N | 統計日期 |
| days | integer | N | 統計天數（預設 7） |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "period": "2024-12-18 ~ 2024-12-24",
    "source_breakdown": [
      {
        "source_module": "M07",
        "module_name": "技術分析",
        "total_signals": 1050,
        "avg_daily_signals": 150,
        "buy_signals": 680,
        "sell_signals": 370,
        "avg_confidence": 0.72,
        "contribution_rate": 0.28
      },
      {
        "source_module": "M09",
        "module_name": "籌碼分析",
        "total_signals": 1400,
        "avg_daily_signals": 200,
        "buy_signals": 850,
        "sell_signals": 550,
        "avg_confidence": 0.78,
        "contribution_rate": 0.35
      },
      {
        "source_module": "M08",
        "module_name": "基本面分析",
        "total_signals": 560,
        "avg_daily_signals": 80,
        "buy_signals": 420,
        "sell_signals": 140,
        "avg_confidence": 0.68,
        "contribution_rate": 0.15
      }
    ],
    "total_collected": 4200,
    "most_active_source": "M09"
  },
  "timestamp": "2024-12-24T17:35:00+08:00"
}
```

---

### 2.12 查詢原始信號

**端點**: `GET /api/m13/signals/raw`

**說明**: 查詢收集的原始信號（去重前）。

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| trade_date | string | N | 交易日期 |
| source_module | string | N | 來源模組 |
| stock_id | string | N | 股票代碼 |
| signal_code | string | N | 信號代碼 |
| page | integer | N | 頁碼 |
| size | integer | N | 每頁筆數 |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "raw_signal_id": "RAW_2024122401_001",
        "source_signal_id": "TECH_SIG_2024122401",
        "source_module": "M07",
        "signal_type": "TECHNICAL",
        "signal_code": "RSI_OVERSOLD",
        "signal_name": "RSI 超賣反彈",
        "stock_id": "2330",
        "signal_date": "2024-12-24",
        "signal_direction": "BUY",
        "source_confidence": 0.75,
        "signal_metadata": {
          "rsi_value": 25.5,
          "threshold": 30,
          "indicator": "RSI_14"
        },
        "dedup_status": "MERGED",
        "unified_signal_id": "UNI_2024122401_2330",
        "collected_at": "2024-12-24T16:30:00+08:00"
      }
    ],
    "page": 0,
    "size": 20,
    "total_elements": 565,
    "total_pages": 29
  },
  "timestamp": "2024-12-24T17:35:00+08:00"
}
```

---

### 2.13 信號追溯

**端點**: `GET /api/m13/signals/unified/{signalId}/trace`

**說明**: 追溯統一信號的完整處理過程與原始來源。

**Path Parameters**:

| 參數 | 類型 | 說明 |
|-----|------|------|
| signalId | string | 統一信號 ID |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "signal_id": "UNI_2024122401_2330",
    "stock_id": "2330",
    "trade_date": "2024-12-24",
    "processing_trace": {
      "collection_phase": {
        "started_at": "2024-12-24T17:00:00+08:00",
        "completed_at": "2024-12-24T17:05:00+08:00",
        "raw_signals_collected": 8
      },
      "deduplication_phase": {
        "started_at": "2024-12-24T17:15:00+08:00",
        "completed_at": "2024-12-24T17:16:00+08:00",
        "signals_before": 8,
        "signals_after": 5,
        "removed_duplicates": [
          {
            "type": "SEMANTIC_DUPLICATE",
            "merged_signals": ["RSI_OVERSOLD", "KD_OVERSOLD"],
            "result": "OVERSOLD_MERGED"
          }
        ]
      },
      "merge_phase": {
        "started_at": "2024-12-24T17:20:00+08:00",
        "completed_at": "2024-12-24T17:21:00+08:00",
        "signals_merged": 5,
        "direction_calculation": {
          "buy_count": 5,
          "sell_count": 0,
          "result": "BUY",
          "strength": "STRONG"
        }
      },
      "scoring_phase": {
        "started_at": "2024-12-24T17:25:00+08:00",
        "completed_at": "2024-12-24T17:26:00+08:00",
        "final_score": 92.5,
        "grade": "A+"
      }
    },
    "original_signals": [
      {
        "source_signal_id": "TECH_SIG_2024122401",
        "source_module": "M07",
        "signal_code": "RSI_OVERSOLD",
        "confidence": 0.75
      },
      {
        "source_signal_id": "CHIP_SIG_2024122402",
        "source_module": "M09",
        "signal_code": "FOREIGN_CONTINUOUS_BUY",
        "confidence": 0.82
      }
    ]
  },
  "timestamp": "2024-12-24T17:35:00+08:00"
}
```

---

### 2.14 信號評分解析

**端點**: `GET /api/m13/signals/unified/{signalId}/scoring`

**說明**: 查詢信號評分的詳細計算過程。

**Path Parameters**:

| 參數 | 類型 | 說明 |
|-----|------|------|
| signalId | string | 統一信號 ID |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "signal_id": "UNI_2024122401_2330",
    "final_score": 92.5,
    "grade": "A+",
    "scoring_dimensions": [
      {
        "dimension": "SIGNAL_STRENGTH",
        "weight": 0.30,
        "raw_score": 95.0,
        "weighted_score": 28.5,
        "calculation_details": {
          "direction_score": 100,
          "direction_strength": "STRONG",
          "confidence_score": 82,
          "unified_confidence": 0.82,
          "formula": "(100 * 0.6) + (82 * 0.4) = 95.0"
        }
      },
      {
        "dimension": "DIMENSION_COVERAGE",
        "weight": 0.25,
        "raw_score": 90.0,
        "weighted_score": 22.5,
        "calculation_details": {
          "coverage_count": 5,
          "max_dimensions": 6,
          "base_score": 66.7,
          "bonus_technical": 5,
          "bonus_chip": 5,
          "bonus_fundamental": 5,
          "bonus_strategy": 5,
          "formula": "(5/6 * 80) + 5 + 5 + 5 + 5 = 90.0"
        }
      },
      {
        "dimension": "HISTORICAL_PERFORMANCE",
        "weight": 0.20,
        "raw_score": 85.0,
        "weighted_score": 17.0,
        "calculation_details": {
          "lookback_days": 90,
          "similar_signals_count": 45,
          "accuracy_rate": 0.68,
          "interpretation": "Good historical accuracy (68%)"
        }
      },
      {
        "dimension": "MARKET_ENVIRONMENT",
        "weight": 0.15,
        "raw_score": 88.0,
        "weighted_score": 13.2,
        "calculation_details": {
          "market_trend": "BULLISH",
          "sector_strength": "STRONG",
          "volatility_level": "NORMAL",
          "interpretation": "Favorable market conditions"
        }
      },
      {
        "dimension": "FRESHNESS",
        "weight": 0.10,
        "raw_score": 95.0,
        "weighted_score": 9.5,
        "calculation_details": {
          "signal_age_hours": 2,
          "freshness_threshold_hours": 24,
          "interpretation": "Very fresh signal"
        }
      }
    ],
    "total_weighted_score": 90.7,
    "adjustment_factors": [
      {
        "factor": "MULTI_DIMENSION_BONUS",
        "adjustment": 1.8,
        "reason": "5+ dimensions covered"
      }
    ],
    "final_calculation": "90.7 + 1.8 = 92.5"
  },
  "timestamp": "2024-12-24T17:35:00+08:00"
}
```

---

### 2.15 手動重算信號

**端點**: `POST /api/m13/signals/recalculate`

**說明**: 重新計算指定日期的信號（管理員功能）。

**Request Body**:
```json
{
  "trade_date": "2024-12-24",
  "stock_ids": ["2330", "2454"],
  "recalculate_scoring": true,
  "reason": "Market data correction"
}
```

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| trade_date | string | Y | 重算日期 |
| stock_ids | array | N | 指定股票（不填則全部） |
| recalculate_scoring | boolean | N | 是否重算評分（預設 true） |
| reason | string | N | 重算原因 |

**Response**:
```json
{
  "code": 200,
  "message": "Recalculation completed",
  "data": {
    "trade_date": "2024-12-24",
    "signals_recalculated": 2,
    "score_changes": [
      {
        "signal_id": "UNI_2024122401_2330",
        "old_score": 90.5,
        "new_score": 92.5,
        "old_grade": "A+",
        "new_grade": "A+"
      }
    ],
    "execution_time_ms": 1500,
    "completed_at": "2024-12-24T18:30:00+08:00"
  },
  "timestamp": "2024-12-24T18:30:01+08:00"
}
```

---

### 2.16 信號訂閱配置

**端點**: `POST /api/m13/subscriptions`

**說明**: 配置信號訂閱條件，符合條件時推送通知。

**Request Body**:
```json
{
  "subscription_name": "A+ 買入信號",
  "consumer_module": "M15",
  "conditions": {
    "direction": "BUY",
    "min_grade": "A+",
    "min_score": 90,
    "sectors": ["SEMICONDUCTOR", "AI"],
    "direction_strength": "STRONG"
  },
  "notification_channel": "EVENT",
  "is_active": true
}
```

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| subscription_name | string | Y | 訂閱名稱 |
| consumer_module | string | Y | 消費模組代碼 |
| conditions | object | Y | 訂閱條件 |
| notification_channel | string | N | 通知方式（EVENT/WEBHOOK） |
| is_active | boolean | N | 是否啟用 |

**Response**:
```json
{
  "code": 200,
  "message": "Subscription created",
  "data": {
    "subscription_id": "SUB_001",
    "subscription_name": "A+ 買入信號",
    "consumer_module": "M15",
    "conditions": {
      "direction": "BUY",
      "min_grade": "A+",
      "min_score": 90
    },
    "is_active": true,
    "created_at": "2024-12-24T18:00:00+08:00"
  },
  "timestamp": "2024-12-24T18:00:01+08:00"
}
```

---

### 2.17 查詢訂閱列表

**端點**: `GET /api/m13/subscriptions`

**說明**: 查詢信號訂閱配置列表。

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| consumer_module | string | N | 消費模組篩選 |
| is_active | boolean | N | 啟用狀態篩選 |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "subscriptions": [
      {
        "subscription_id": "SUB_001",
        "subscription_name": "A+ 買入信號",
        "consumer_module": "M15",
        "conditions": {
          "direction": "BUY",
          "min_grade": "A+"
        },
        "notification_channel": "EVENT",
        "is_active": true,
        "triggered_count": 15,
        "last_triggered_at": "2024-12-24T17:30:00+08:00"
      }
    ],
    "total_count": 5
  },
  "timestamp": "2024-12-24T18:00:00+08:00"
}
```

---

### 2.18 刪除訂閱

**端點**: `DELETE /api/m13/subscriptions/{subscriptionId}`

**說明**: 刪除信號訂閱配置。

**Path Parameters**:

| 參數 | 類型 | 說明 |
|-----|------|------|
| subscriptionId | string | 訂閱 ID |

**Response**:
```json
{
  "code": 200,
  "message": "Subscription deleted",
  "data": {
    "subscription_id": "SUB_001",
    "deleted_at": "2024-12-24T18:00:00+08:00"
  },
  "timestamp": "2024-12-24T18:00:01+08:00"
}
```

---

## 3. 錯誤碼定義

| 錯誤碼 | HTTP Status | 說明 |
|-------|-------------|------|
| M13_001 | 404 | 信號不存在 |
| M13_002 | 400 | 無效的日期範圍 |
| M13_003 | 400 | 無效的評級參數 |
| M13_004 | 400 | 無效的來源模組 |
| M13_005 | 409 | 信號已被消費 |
| M13_006 | 500 | 信號收集失敗 |
| M13_007 | 500 | 評分計算失敗 |
| M13_008 | 404 | 訂閱不存在 |
| M13_009 | 400 | 訂閱條件無效 |
| M13_010 | 429 | 請求頻率過高 |

**錯誤回應範例**:
```json
{
  "code": 404,
  "message": "Signal not found",
  "error": {
    "error_code": "M13_001",
    "detail": "Unified signal 'UNI_2024122401_9999' does not exist"
  },
  "timestamp": "2024-12-24T17:35:00+08:00"
}
```

---

## 4. 通用規格

### 4.1 分頁參數

所有列表查詢 API 支援以下分頁參數：

| 參數 | 類型 | 預設值 | 說明 |
|-----|------|-------|------|
| page | integer | 0 | 頁碼（從 0 開始） |
| size | integer | 20 | 每頁筆數（最大 100） |
| sort | string | - | 排序欄位與方向 |

### 4.2 日期格式

- 日期參數格式：`YYYY-MM-DD`
- 時間戳格式：ISO 8601（`YYYY-MM-DDTHH:mm:ss+08:00`）

### 4.3 認證與授權

所有 API 需要 Bearer Token 認證：
```
Authorization: Bearer <access_token>
```

### 4.4 速率限制

| 端點類型 | 限制 |
|---------|------|
| 查詢類 | 100 req/min |
| 寫入類 | 20 req/min |
| 統計類 | 30 req/min |

---

## 5. 相關文檔

- [M13 功能需求](../functional/M13-信號引擎功能需求.md)
- [M13 資料庫設計](../../design/M13-資料庫設計.md)
- [M13 業務流程](../../design/M13-業務流程.md)

---

**文件維護者**: 後端工程師
**最後更新**: 2026-01-14
**下次審核**: 2026-04-14
