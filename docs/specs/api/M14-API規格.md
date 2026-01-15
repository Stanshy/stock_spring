# M14-選股引擎 API 規格

> **文件編號**: API-M14
> **模組名稱**: 選股引擎 (Stock Screening Engine)
> **版本**: v1.0
> **最後更新**: 2026-01-14
> **狀態**: Draft

---

## 1. API 總覽

| # | API | Method | 路徑 | 說明 |
|---|-----|--------|-----|------|
| 1 | 快速選股 | POST | `/api/m14/screening/quick` | 使用預設模板選股 |
| 2 | 自訂選股 | POST | `/api/m14/screening/custom` | 自訂條件選股 |
| 3 | 信號選股 | POST | `/api/m14/screening/signal` | 基於 M13 信號選股 |
| 4 | 建立策略 | POST | `/api/m14/strategies` | 建立選股策略 |
| 5 | 查詢策略列表 | GET | `/api/m14/strategies` | 查詢用戶策略列表 |
| 6 | 查詢策略詳情 | GET | `/api/m14/strategies/{strategyId}` | 查詢單一策略詳情 |
| 7 | 更新策略 | PUT | `/api/m14/strategies/{strategyId}` | 更新策略 |
| 8 | 刪除策略 | DELETE | `/api/m14/strategies/{strategyId}` | 刪除策略 |
| 9 | 執行策略 | POST | `/api/m14/strategies/{strategyId}/execute` | 執行已儲存策略 |
| 10 | 複製策略 | POST | `/api/m14/strategies/{strategyId}/copy` | 複製策略 |
| 11 | 查詢快速模板 | GET | `/api/m14/templates` | 查詢可用快速模板 |
| 12 | 查詢篩選條件 | GET | `/api/m14/conditions` | 查詢可用篩選條件定義 |
| 13 | 查詢執行歷史 | GET | `/api/m14/history` | 查詢選股執行歷史 |
| 14 | 查詢執行詳情 | GET | `/api/m14/history/{executionId}` | 查詢單次執行詳情 |
| 15 | 重新執行 | POST | `/api/m14/history/{executionId}/re-execute` | 重新執行歷史選股 |
| 16 | 查詢績效追蹤 | GET | `/api/m14/performance/{executionId}` | 查詢選股績效 |
| 17 | 策略績效統計 | GET | `/api/m14/strategies/{strategyId}/performance` | 查詢策略歷史績效 |
| 18 | 公開策略列表 | GET | `/api/m14/strategies/public` | 查詢公開策略 |

---

## 2. API 詳細規格

### 2.1 快速選股

**端點**: `POST /api/m14/screening/quick`

**說明**: 使用預設快速選股模板執行選股。

**Request Body**:
```json
{
  "template_code": "QUICK_01",
  "market": "TWSE",
  "sectors": ["SEMICONDUCTOR", "ELECTRONICS"],
  "exclude_stocks": ["2330"],
  "limit": 50,
  "sort_by": "S_SCORE",
  "sort_direction": "DESC"
}
```

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| template_code | string | Y | 快速模板代碼 |
| market | string | N | 市場篩選（TWSE/OTC/ALL） |
| sectors | array | N | 產業篩選 |
| exclude_stocks | array | N | 排除股票 |
| limit | integer | N | 回傳筆數（預設 50，最大 200） |
| sort_by | string | N | 排序欄位 |
| sort_direction | string | N | 排序方向（ASC/DESC） |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "execution_id": "EXEC_20241224_001",
    "template_code": "QUICK_01",
    "template_name": "強勢股",
    "executed_at": "2024-12-24T16:30:00+08:00",
    "execution_time_ms": 1250,
    "total_candidates": 1200,
    "matched_count": 35,
    "results": [
      {
        "rank": 1,
        "stock_id": "2330",
        "stock_name": "台積電",
        "market": "TWSE",
        "sector": "半導體",
        "price": 580.00,
        "price_change": 2.5,
        "price_change_pct": 0.43,
        "signal_score": 92.5,
        "signal_grade": "A+",
        "signal_direction": "BUY",
        "key_metrics": {
          "pe": 12.5,
          "roe": 25.3,
          "foreign_net_5d": 15000
        }
      },
      {
        "rank": 2,
        "stock_id": "2454",
        "stock_name": "聯發科",
        "market": "TWSE",
        "sector": "半導體",
        "price": 985.00,
        "price_change": 15.0,
        "price_change_pct": 1.55,
        "signal_score": 88.3,
        "signal_grade": "A",
        "signal_direction": "BUY",
        "key_metrics": {
          "pe": 15.2,
          "roe": 22.1,
          "foreign_net_5d": 8500
        }
      }
    ]
  },
  "timestamp": "2024-12-24T16:30:01+08:00"
}
```

---

### 2.2 自訂選股

**端點**: `POST /api/m14/screening/custom`

**說明**: 使用自訂條件組合執行選股。

**Request Body**:
```json
{
  "name": "價值成長股篩選",
  "conditions": {
    "logic": "AND",
    "conditions": [
      {
        "condition_code": "F_PE",
        "operator": "BETWEEN",
        "values": [5, 15]
      },
      {
        "condition_code": "F_ROE",
        "operator": ">=",
        "values": [15]
      },
      {
        "logic": "OR",
        "conditions": [
          {
            "condition_code": "C_FOREIGN_CONT",
            "operator": ">=",
            "values": [3]
          },
          {
            "condition_code": "C_TRUST_CONT",
            "operator": ">=",
            "values": [3]
          }
        ]
      },
      {
        "condition_code": "S_GRADE",
        "operator": ">=",
        "values": ["B+"]
      }
    ]
  },
  "market": "ALL",
  "sectors": null,
  "min_price": 10,
  "max_price": 1000,
  "min_volume": 500,
  "limit": 100,
  "sort_by": ["S_SCORE", "F_ROE"],
  "sort_direction": ["DESC", "DESC"],
  "save_as_strategy": false
}
```

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| name | string | N | 篩選名稱 |
| conditions | object | Y | 條件組合物件 |
| market | string | N | 市場篩選 |
| sectors | array | N | 產業篩選 |
| min_price | number | N | 最低股價 |
| max_price | number | N | 最高股價 |
| min_volume | number | N | 最低成交量（張） |
| limit | integer | N | 回傳筆數 |
| sort_by | array | N | 排序欄位（最多 3 個） |
| sort_direction | array | N | 排序方向 |
| save_as_strategy | boolean | N | 是否儲存為策略 |

**條件物件結構**:

```json
{
  "condition_code": "string",
  "operator": "string",
  "values": ["any"]
}
```

| 欄位 | 類型 | 說明 |
|-----|------|------|
| condition_code | string | 條件代碼（如 F_PE, T_RSI） |
| operator | string | 運算符（<, <=, =, >=, >, BETWEEN, IN） |
| values | array | 條件值 |

**組合邏輯物件結構**:
```json
{
  "logic": "AND|OR",
  "conditions": [...]
}
```

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "execution_id": "EXEC_20241224_002",
    "name": "價值成長股篩選",
    "executed_at": "2024-12-24T16:35:00+08:00",
    "execution_time_ms": 2150,
    "condition_summary": "(PE 5~15) AND (ROE >= 15) AND (外資連買 >= 3 OR 投信連買 >= 3) AND (評級 >= B+)",
    "total_candidates": 1800,
    "matched_count": 28,
    "results": [
      {
        "rank": 1,
        "stock_id": "2330",
        "stock_name": "台積電",
        "market": "TWSE",
        "sector": "半導體",
        "price": 580.00,
        "matched_conditions": [
          "F_PE: 12.5 (5~15 ✓)",
          "F_ROE: 25.3% (>=15% ✓)",
          "C_FOREIGN_CONT: 8 日 (>=3 ✓)",
          "S_GRADE: A+ (>=B+ ✓)"
        ],
        "key_metrics": {
          "pe": 12.5,
          "pb": 4.2,
          "roe": 25.3,
          "eps": 46.5,
          "dividend_yield": 2.1,
          "foreign_cont_days": 8,
          "signal_score": 92.5,
          "signal_grade": "A+"
        }
      }
    ],
    "strategy_id": null
  },
  "timestamp": "2024-12-24T16:35:02+08:00"
}
```

---

### 2.3 信號選股

**端點**: `POST /api/m14/screening/signal`

**說明**: 專門基於 M13 統一信號的選股。

**Request Body**:
```json
{
  "mode": "SIGNAL_BUY",
  "trade_date": "2024-12-24",
  "min_grade": "B+",
  "min_score": 70,
  "min_coverage": 3,
  "direction_strength": "STRONG",
  "signal_types": ["MOMENTUM", "INSTITUTIONAL"],
  "market": "TWSE",
  "limit": 30
}
```

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| mode | string | Y | 信號選股模式 |
| trade_date | string | N | 信號日期（預設今日） |
| min_grade | string | N | 最低評級 |
| min_score | number | N | 最低評分 |
| min_coverage | integer | N | 最低維度覆蓋數 |
| direction_strength | string | N | 方向強度篩選 |
| signal_types | array | N | 信號類型篩選 |
| market | string | N | 市場篩選 |
| limit | integer | N | 回傳筆數 |

**信號選股模式**:

| 模式 | 說明 |
|-----|------|
| SIGNAL_TOP | 信號評分前 N 名 |
| SIGNAL_GRADE | 指定評級篩選 |
| SIGNAL_BUY | 強烈買入信號 |
| SIGNAL_SELL | 強烈賣出信號 |
| SIGNAL_NEW | 今日新信號 |
| SIGNAL_MULTI | 多維度覆蓋 |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "execution_id": "EXEC_20241224_003",
    "mode": "SIGNAL_BUY",
    "trade_date": "2024-12-24",
    "executed_at": "2024-12-24T16:40:00+08:00",
    "matched_count": 18,
    "results": [
      {
        "rank": 1,
        "stock_id": "2330",
        "stock_name": "台積電",
        "signal_id": "UNI_2024122401_2330",
        "signal_score": 92.5,
        "signal_grade": "A+",
        "signal_direction": "BUY",
        "direction_strength": "STRONG",
        "dimension_coverage": 5,
        "signal_types": ["MOMENTUM", "INSTITUTIONAL", "VALUE"],
        "key_factors": ["外資連買 8 日", "RSI 超賣反彈", "PE 歷史低位"],
        "price": 580.00,
        "price_change_pct": 0.43
      }
    ]
  },
  "timestamp": "2024-12-24T16:40:01+08:00"
}
```

---

### 2.4 建立策略

**端點**: `POST /api/m14/strategies`

**說明**: 建立新的選股策略。

**Request Body**:
```json
{
  "strategy_name": "我的價值成長策略",
  "description": "低估值高成長股篩選",
  "conditions": {
    "logic": "AND",
    "conditions": [
      { "condition_code": "F_PE", "operator": "<=", "values": [15] },
      { "condition_code": "F_ROE", "operator": ">=", "values": [15] },
      { "condition_code": "S_GRADE", "operator": ">=", "values": ["B"] }
    ]
  },
  "market": "ALL",
  "sort_by": ["S_SCORE", "F_ROE"],
  "sort_direction": ["DESC", "DESC"],
  "default_limit": 50,
  "is_public": false
}
```

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| strategy_name | string | Y | 策略名稱（最長 50 字） |
| description | string | N | 策略描述 |
| conditions | object | Y | 條件組合 |
| market | string | N | 預設市場 |
| sort_by | array | N | 預設排序欄位 |
| sort_direction | array | N | 預設排序方向 |
| default_limit | integer | N | 預設回傳筆數 |
| is_public | boolean | N | 是否公開 |

**Response**:
```json
{
  "code": 200,
  "message": "Strategy created",
  "data": {
    "strategy_id": "STR_20241224_001",
    "strategy_name": "我的價值成長策略",
    "owner_id": "user_001",
    "is_public": false,
    "created_at": "2024-12-24T16:45:00+08:00"
  },
  "timestamp": "2024-12-24T16:45:01+08:00"
}
```

---

### 2.5 查詢策略列表

**端點**: `GET /api/m14/strategies`

**說明**: 查詢用戶的選股策略列表。

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| include_public | boolean | N | 是否包含公開策略 |
| keyword | string | N | 名稱關鍵字搜尋 |
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
        "strategy_id": "STR_20241224_001",
        "strategy_name": "我的價值成長策略",
        "description": "低估值高成長股篩選",
        "is_public": false,
        "condition_count": 3,
        "execution_count": 15,
        "last_executed_at": "2024-12-24T16:30:00+08:00",
        "created_at": "2024-12-20T10:00:00+08:00"
      }
    ],
    "page": 0,
    "size": 20,
    "total_elements": 5,
    "total_pages": 1
  },
  "timestamp": "2024-12-24T16:45:00+08:00"
}
```

---

### 2.6 查詢策略詳情

**端點**: `GET /api/m14/strategies/{strategyId}`

**說明**: 查詢單一策略的完整詳情。

**Path Parameters**:

| 參數 | 類型 | 說明 |
|-----|------|------|
| strategyId | string | 策略 ID |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "strategy_id": "STR_20241224_001",
    "strategy_name": "我的價值成長策略",
    "description": "低估值高成長股篩選",
    "owner_id": "user_001",
    "is_public": false,
    "conditions": {
      "logic": "AND",
      "conditions": [
        { "condition_code": "F_PE", "operator": "<=", "values": [15] },
        { "condition_code": "F_ROE", "operator": ">=", "values": [15] },
        { "condition_code": "S_GRADE", "operator": ">=", "values": ["B"] }
      ]
    },
    "condition_summary": "(PE <= 15) AND (ROE >= 15%) AND (評級 >= B)",
    "market": "ALL",
    "sort_by": ["S_SCORE", "F_ROE"],
    "sort_direction": ["DESC", "DESC"],
    "default_limit": 50,
    "execution_count": 15,
    "last_executed_at": "2024-12-24T16:30:00+08:00",
    "last_execution_result": {
      "matched_count": 28,
      "top_3_stocks": ["2330", "2454", "2317"]
    },
    "created_at": "2024-12-20T10:00:00+08:00",
    "updated_at": "2024-12-23T14:00:00+08:00"
  },
  "timestamp": "2024-12-24T16:45:00+08:00"
}
```

---

### 2.7 更新策略

**端點**: `PUT /api/m14/strategies/{strategyId}`

**說明**: 更新選股策略。

**Request Body**:
```json
{
  "strategy_name": "我的價值成長策略 v2",
  "description": "更新版：加入籌碼條件",
  "conditions": {
    "logic": "AND",
    "conditions": [
      { "condition_code": "F_PE", "operator": "<=", "values": [15] },
      { "condition_code": "F_ROE", "operator": ">=", "values": [15] },
      { "condition_code": "C_FOREIGN_CONT", "operator": ">=", "values": [3] },
      { "condition_code": "S_GRADE", "operator": ">=", "values": ["B"] }
    ]
  },
  "is_public": false
}
```

**Response**:
```json
{
  "code": 200,
  "message": "Strategy updated",
  "data": {
    "strategy_id": "STR_20241224_001",
    "strategy_name": "我的價值成長策略 v2",
    "updated_at": "2024-12-24T17:00:00+08:00"
  },
  "timestamp": "2024-12-24T17:00:01+08:00"
}
```

---

### 2.8 刪除策略

**端點**: `DELETE /api/m14/strategies/{strategyId}`

**說明**: 刪除選股策略。

**Response**:
```json
{
  "code": 200,
  "message": "Strategy deleted",
  "data": {
    "strategy_id": "STR_20241224_001",
    "deleted_at": "2024-12-24T17:05:00+08:00"
  },
  "timestamp": "2024-12-24T17:05:01+08:00"
}
```

---

### 2.9 執行策略

**端點**: `POST /api/m14/strategies/{strategyId}/execute`

**說明**: 執行已儲存的選股策略。

**Request Body**:
```json
{
  "override_market": "TWSE",
  "override_limit": 30,
  "override_sort_by": ["F_ROE"],
  "override_sort_direction": ["DESC"]
}
```

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| override_market | string | N | 覆蓋市場設定 |
| override_limit | integer | N | 覆蓋筆數限制 |
| override_sort_by | array | N | 覆蓋排序欄位 |
| override_sort_direction | array | N | 覆蓋排序方向 |

**Response**: 同自訂選股回應格式。

---

### 2.10 複製策略

**端點**: `POST /api/m14/strategies/{strategyId}/copy`

**說明**: 複製策略為新策略。

**Request Body**:
```json
{
  "new_name": "我的價值成長策略 (複製)"
}
```

**Response**:
```json
{
  "code": 200,
  "message": "Strategy copied",
  "data": {
    "original_strategy_id": "STR_20241224_001",
    "new_strategy_id": "STR_20241224_002",
    "new_name": "我的價值成長策略 (複製)",
    "created_at": "2024-12-24T17:10:00+08:00"
  },
  "timestamp": "2024-12-24T17:10:01+08:00"
}
```

---

### 2.11 查詢快速模板

**端點**: `GET /api/m14/templates`

**說明**: 查詢可用的快速選股模板。

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "templates": [
      {
        "template_code": "QUICK_01",
        "template_name": "強勢股",
        "description": "M13 高評級買入信號股票",
        "category": "信號",
        "condition_summary": "信號評級 >= A, 方向 = BUY"
      },
      {
        "template_code": "QUICK_02",
        "template_name": "外資連買",
        "description": "外資連續買超的股票",
        "category": "籌碼",
        "condition_summary": "外資連續買超 >= 3 日"
      },
      {
        "template_code": "QUICK_03",
        "template_name": "低估價值股",
        "description": "低估值高殖利率股票",
        "category": "基本面",
        "condition_summary": "PE < 15, ROE > 15%, 股利率 > 3%"
      }
    ]
  },
  "timestamp": "2024-12-24T16:30:00+08:00"
}
```

---

### 2.12 查詢篩選條件

**端點**: `GET /api/m14/conditions`

**說明**: 查詢所有可用的篩選條件定義。

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| category | string | N | 條件類別（FUNDAMENTAL/TECHNICAL/CHIP/SIGNAL/ATTRIBUTE） |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "categories": [
      {
        "category": "FUNDAMENTAL",
        "category_name": "基本面條件",
        "conditions": [
          {
            "condition_code": "F_PE",
            "condition_name": "本益比",
            "description": "股價 / 每股盈餘",
            "data_source": "M08",
            "data_type": "NUMBER",
            "operators": ["<", "<=", "=", ">=", ">", "BETWEEN"],
            "unit": "倍",
            "example": { "operator": "BETWEEN", "values": [5, 15] }
          },
          {
            "condition_code": "F_ROE",
            "condition_name": "股東權益報酬率",
            "description": "淨利 / 股東權益",
            "data_source": "M08",
            "data_type": "NUMBER",
            "operators": ["<", "<=", "=", ">=", ">", "BETWEEN"],
            "unit": "%",
            "example": { "operator": ">=", "values": [15] }
          }
        ]
      },
      {
        "category": "TECHNICAL",
        "category_name": "技術面條件",
        "conditions": [
          {
            "condition_code": "T_RSI",
            "condition_name": "RSI 指標",
            "description": "相對強弱指標（14 日）",
            "data_source": "M07",
            "data_type": "NUMBER",
            "operators": ["<", "<=", "=", ">=", ">", "BETWEEN"],
            "unit": null,
            "value_range": [0, 100],
            "example": { "operator": "BETWEEN", "values": [30, 70] }
          }
        ]
      },
      {
        "category": "SIGNAL",
        "category_name": "信號條件",
        "conditions": [
          {
            "condition_code": "S_GRADE",
            "condition_name": "信號評級",
            "description": "M13 統一信號評級",
            "data_source": "M13",
            "data_type": "ENUM",
            "operators": ["=", ">="],
            "allowed_values": ["A+", "A", "B+", "B", "C", "D"],
            "example": { "operator": ">=", "values": ["B+"] }
          }
        ]
      }
    ]
  },
  "timestamp": "2024-12-24T16:30:00+08:00"
}
```

---

### 2.13 查詢執行歷史

**端點**: `GET /api/m14/history`

**說明**: 查詢選股執行歷史。

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| strategy_id | string | N | 策略 ID 篩選 |
| start_date | string | N | 起始日期 |
| end_date | string | N | 結束日期 |
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
        "execution_id": "EXEC_20241224_001",
        "execution_type": "QUICK",
        "template_code": "QUICK_01",
        "strategy_id": null,
        "name": "強勢股",
        "executed_at": "2024-12-24T16:30:00+08:00",
        "execution_time_ms": 1250,
        "total_candidates": 1200,
        "matched_count": 35,
        "top_stocks": ["2330", "2454", "2317"]
      },
      {
        "execution_id": "EXEC_20241224_002",
        "execution_type": "STRATEGY",
        "template_code": null,
        "strategy_id": "STR_20241224_001",
        "name": "我的價值成長策略",
        "executed_at": "2024-12-24T16:35:00+08:00",
        "execution_time_ms": 2150,
        "total_candidates": 1800,
        "matched_count": 28,
        "top_stocks": ["2330", "2454", "2382"]
      }
    ],
    "page": 0,
    "size": 20,
    "total_elements": 45,
    "total_pages": 3
  },
  "timestamp": "2024-12-24T17:00:00+08:00"
}
```

---

### 2.14 查詢執行詳情

**端點**: `GET /api/m14/history/{executionId}`

**說明**: 查詢單次執行的完整詳情。

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "execution_id": "EXEC_20241224_001",
    "execution_type": "QUICK",
    "template_code": "QUICK_01",
    "name": "強勢股",
    "executed_at": "2024-12-24T16:30:00+08:00",
    "execution_time_ms": 1250,
    "condition_snapshot": {
      "logic": "AND",
      "conditions": [
        { "condition_code": "S_GRADE", "operator": ">=", "values": ["A"] },
        { "condition_code": "S_DIRECTION", "operator": "=", "values": ["BUY"] }
      ]
    },
    "total_candidates": 1200,
    "matched_count": 35,
    "results": [
      {
        "rank": 1,
        "stock_id": "2330",
        "stock_name": "台積電",
        "price_at_execution": 580.00,
        "signal_score": 92.5,
        "signal_grade": "A+"
      }
    ]
  },
  "timestamp": "2024-12-24T17:00:00+08:00"
}
```

---

### 2.15 重新執行

**端點**: `POST /api/m14/history/{executionId}/re-execute`

**說明**: 使用歷史執行的條件重新執行選股。

**Response**: 同自訂選股回應格式。

---

### 2.16 查詢績效追蹤

**端點**: `GET /api/m14/performance/{executionId}`

**說明**: 查詢選股結果的後續績效表現。

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "execution_id": "EXEC_20241220_001",
    "executed_at": "2024-12-20T16:30:00+08:00",
    "trade_date": "2024-12-20",
    "matched_count": 30,
    "tracking_days": 4,
    "performance_summary": {
      "return_1d": {
        "avg": 0.012,
        "max": 0.05,
        "min": -0.025,
        "positive_count": 22,
        "win_rate": 0.733
      },
      "return_5d": {
        "avg": 0.028,
        "max": 0.12,
        "min": -0.045,
        "positive_count": 19,
        "win_rate": 0.633
      }
    },
    "stock_performance": [
      {
        "stock_id": "2330",
        "stock_name": "台積電",
        "price_at_execution": 575.00,
        "current_price": 588.00,
        "return_1d": 0.015,
        "return_5d": 0.026,
        "max_return": 0.035,
        "max_drawdown": -0.008
      }
    ]
  },
  "timestamp": "2024-12-24T17:00:00+08:00"
}
```

---

### 2.17 策略績效統計

**端點**: `GET /api/m14/strategies/{strategyId}/performance`

**說明**: 查詢策略的歷史績效統計。

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| days | integer | N | 統計天數（預設 30） |

**Response**:
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "strategy_id": "STR_20241224_001",
    "strategy_name": "我的價值成長策略",
    "period": "2024-11-24 ~ 2024-12-24",
    "total_executions": 15,
    "total_stocks_selected": 380,
    "unique_stocks": 85,
    "performance_stats": {
      "avg_return_1d": 0.008,
      "avg_return_5d": 0.022,
      "avg_return_10d": 0.035,
      "overall_win_rate_5d": 0.62,
      "best_pick": {
        "stock_id": "2454",
        "stock_name": "聯發科",
        "return_5d": 0.15
      },
      "worst_pick": {
        "stock_id": "2382",
        "stock_name": "廣達",
        "return_5d": -0.08
      }
    },
    "compared_to_benchmark": {
      "benchmark": "TAIEX",
      "benchmark_return_30d": 0.025,
      "strategy_return_30d": 0.045,
      "alpha": 0.020
    }
  },
  "timestamp": "2024-12-24T17:00:00+08:00"
}
```

---

### 2.18 公開策略列表

**端點**: `GET /api/m14/strategies/public`

**說明**: 查詢公開的選股策略。

**Query Parameters**:

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| keyword | string | N | 關鍵字搜尋 |
| sort_by | string | N | 排序（popular/recent/performance） |
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
        "strategy_id": "STR_PUBLIC_001",
        "strategy_name": "價值投資精選",
        "description": "長期持有型價值股篩選",
        "owner_name": "投資達人",
        "condition_count": 5,
        "copy_count": 128,
        "execution_count": 450,
        "avg_win_rate_5d": 0.65,
        "created_at": "2024-11-01T10:00:00+08:00"
      }
    ],
    "page": 0,
    "size": 20,
    "total_elements": 25,
    "total_pages": 2
  },
  "timestamp": "2024-12-24T17:00:00+08:00"
}
```

---

## 3. 錯誤碼定義

| 錯誤碼 | HTTP Status | 說明 |
|-------|-------------|------|
| M14_001 | 400 | 無效的篩選條件 |
| M14_002 | 400 | 條件參數錯誤 |
| M14_003 | 400 | 超過條件數量限制 |
| M14_004 | 400 | 無效的策略 ID |
| M14_005 | 404 | 策略不存在 |
| M14_006 | 403 | 無權限操作此策略 |
| M14_007 | 500 | 選股執行失敗 |
| M14_008 | 504 | 選股執行超時 |
| M14_009 | 400 | 無效的排序欄位 |
| M14_010 | 400 | 無效的快速選股模板 |
| M14_011 | 400 | 條件巢狀層級超過限制 |
| M14_012 | 404 | 執行記錄不存在 |

---

## 4. 通用規格

### 4.1 認證

所有 API 需要 Bearer Token 認證：
```
Authorization: Bearer <access_token>
```

### 4.2 速率限制

| 端點類型 | 限制 |
|---------|------|
| 選股執行 | 30 req/min |
| 策略管理 | 60 req/min |
| 查詢類 | 100 req/min |

---

## 5. 相關文檔

- [M14 功能需求](../functional/M14-選股引擎功能需求.md)
- [M14 資料庫設計](../../design/M14-資料庫設計.md)
- [M13 API 規格](./M13-API規格.md)

---

**文件維護者**: 後端工程師
**最後更新**: 2026-01-14
**下次審核**: 2026-04-14
