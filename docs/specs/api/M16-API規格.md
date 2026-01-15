# M16-回測系統 API 規格

> **文件編號**: API-M16
> **模組名稱**: 回測系統 (Backtesting System)
> **版本**: v1.0
> **最後更新**: 2026-01-15
> **狀態**: Draft

---

## 1. API 總覽

### 1.1 基礎資訊

| 項目 | 說明 |
|-----|------|
| Base URL | `/api/v1/backtests` |
| 認證方式 | Bearer Token (JWT) |
| 資料格式 | JSON |
| 字元編碼 | UTF-8 |

### 1.2 API 清單

| 端點 | 方法 | 說明 | 優先級 |
|-----|------|------|:------:|
| `/backtests` | POST | 建立回測任務 | P0 |
| `/backtests/{backtestId}` | GET | 取得回測詳情 | P0 |
| `/backtests/{backtestId}/execute` | POST | 執行回測 | P0 |
| `/backtests/{backtestId}/cancel` | POST | 取消回測 | P0 |
| `/backtests/{backtestId}/status` | GET | 取得執行狀態 | P0 |
| `/backtests/{backtestId}/report` | GET | 取得回測報告 | P0 |
| `/backtests/{backtestId}/trades` | GET | 取得交易明細 | P0 |
| `/backtests/{backtestId}/equity-curve` | GET | 取得淨值曲線 | P0 |
| `/backtests` | GET | 取得回測清單 | P1 |
| `/backtests/{backtestId}` | DELETE | 刪除回測 | P1 |
| `/backtests/{backtestId}/optimize` | POST | 執行參數最佳化 | P1 |
| `/backtests/{backtestId}/optimization-result` | GET | 取得最佳化結果 | P1 |
| `/backtests/compare` | POST | 多策略比較 | P1 |
| `/backtests/benchmarks` | GET | 取得可用基準 | P1 |
| `/backtests/templates` | GET | 取得回測範本 | P2 |
| `/backtests/templates` | POST | 建立回測範本 | P2 |
| `/backtests/{backtestId}/export` | GET | 匯出回測結果 | P2 |
| `/backtests/{backtestId}/walk-forward` | POST | 執行滾動回測 | P2 |

---

## 2. P0 核心 API

### 2.1 建立回測任務

建立新的回測任務。

**端點**
```
POST /api/v1/backtests
```

**請求主體**
```json
{
  "backtestName": "台積電突破策略回測",
  "strategyId": "STR_001",
  "signalSource": "STRATEGY",
  "targetStocks": {
    "mode": "SPECIFIC",
    "stockIds": ["2330", "2317"],
    "watchlistId": null,
    "filterConditions": null
  },
  "period": {
    "startDate": "2023-01-01",
    "endDate": "2025-12-31"
  },
  "initialCapital": 1000000,
  "tradingSettings": {
    "positionSizing": "FIXED_AMOUNT",
    "fixedAmount": 100000,
    "percentOfCapital": null,
    "maxPositions": 5,
    "allowShort": false,
    "tradingCost": {
      "commissionRate": 0.001425,
      "taxRate": 0.003,
      "slippage": 0.001
    }
  },
  "exitRules": {
    "stopLoss": 0.07,
    "takeProfit": 0.15,
    "trailingStop": null,
    "maxHoldingDays": 30
  }
}
```

**請求參數說明**

| 參數 | 類型 | 必填 | 說明 |
|-----|------|:----:|------|
| backtestName | string | Y | 回測名稱 |
| strategyId | string | N | M11 策略 ID |
| signalSource | string | Y | 信號來源：STRATEGY/SIGNAL/CUSTOM |
| targetStocks | object | Y | 目標股票設定 |
| targetStocks.mode | string | Y | 模式：SPECIFIC/WATCHLIST/FILTER |
| targetStocks.stockIds | array | N | 股票代碼清單 |
| targetStocks.watchlistId | string | N | 自選清單 ID |
| period | object | Y | 回測期間 |
| period.startDate | date | Y | 開始日期 |
| period.endDate | date | Y | 結束日期 |
| initialCapital | number | Y | 初始資金 |
| tradingSettings | object | Y | 交易設定 |
| tradingSettings.positionSizing | string | Y | 部位大小方式：FIXED_AMOUNT/PERCENT/EQUAL_WEIGHT |
| tradingSettings.fixedAmount | number | N | 固定金額 |
| tradingSettings.maxPositions | integer | Y | 最大持股數 |
| tradingSettings.allowShort | boolean | N | 是否允許放空，預設 false |
| tradingSettings.tradingCost | object | Y | 交易成本 |
| exitRules | object | N | 出場規則 |
| exitRules.stopLoss | number | N | 停損百分比 (0.07 = 7%) |
| exitRules.takeProfit | number | N | 停利百分比 |
| exitRules.trailingStop | number | N | 移動停損百分比 |
| exitRules.maxHoldingDays | integer | N | 最大持有天數 |

**成功回應 (201 Created)**
```json
{
  "code": 201,
  "message": "Backtest created successfully",
  "data": {
    "backtestId": "BT_20260115_001",
    "backtestName": "台積電突破策略回測",
    "status": "CREATED",
    "signalSource": "STRATEGY",
    "strategyId": "STR_001",
    "targetStocks": ["2330", "2317"],
    "period": {
      "startDate": "2023-01-01",
      "endDate": "2025-12-31",
      "tradingDays": 745
    },
    "initialCapital": 1000000,
    "estimatedDuration": "PT2M30S",
    "createdAt": "2026-01-15T10:30:00+08:00"
  },
  "timestamp": "2026-01-15T10:30:00+08:00",
  "traceId": "req_abc123"
}
```

**錯誤回應**
```json
{
  "code": 400,
  "message": "Invalid request",
  "error": {
    "errorCode": "M16-002",
    "errorMessage": "回測參數無效",
    "details": {
      "field": "period.startDate",
      "reason": "開始日期不能晚於結束日期"
    }
  },
  "timestamp": "2026-01-15T10:30:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 2.2 取得回測詳情

取得回測任務的詳細資訊。

**端點**
```
GET /api/v1/backtests/{backtestId}
```

**路徑參數**

| 參數 | 類型 | 說明 |
|-----|------|------|
| backtestId | string | 回測任務 ID |

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "backtestId": "BT_20260115_001",
    "backtestName": "台積電突破策略回測",
    "status": "COMPLETED",
    "signalSource": "STRATEGY",
    "strategyId": "STR_001",
    "strategyName": "突破策略",
    "targetStocks": ["2330", "2317"],
    "period": {
      "startDate": "2023-01-01",
      "endDate": "2025-12-31",
      "tradingDays": 745
    },
    "initialCapital": 1000000,
    "tradingSettings": {
      "positionSizing": "FIXED_AMOUNT",
      "fixedAmount": 100000,
      "maxPositions": 5,
      "allowShort": false,
      "tradingCost": {
        "commissionRate": 0.001425,
        "taxRate": 0.003,
        "slippage": 0.001
      }
    },
    "exitRules": {
      "stopLoss": 0.07,
      "takeProfit": 0.15,
      "trailingStop": null,
      "maxHoldingDays": 30
    },
    "executionInfo": {
      "startedAt": "2026-01-15T10:30:05+08:00",
      "completedAt": "2026-01-15T10:32:35+08:00",
      "duration": "PT2M30S"
    },
    "createdAt": "2026-01-15T10:30:00+08:00",
    "updatedAt": "2026-01-15T10:32:35+08:00"
  },
  "timestamp": "2026-01-15T10:35:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 2.3 執行回測

啟動回測任務執行。

**端點**
```
POST /api/v1/backtests/{backtestId}/execute
```

**路徑參數**

| 參數 | 類型 | 說明 |
|-----|------|------|
| backtestId | string | 回測任務 ID |

**成功回應 (202 Accepted)**
```json
{
  "code": 202,
  "message": "Backtest execution started",
  "data": {
    "backtestId": "BT_20260115_001",
    "status": "RUNNING",
    "startedAt": "2026-01-15T10:30:05+08:00",
    "estimatedCompletion": "2026-01-15T10:32:35+08:00",
    "progress": {
      "currentDay": "2023-01-01",
      "totalDays": 745,
      "completedDays": 0,
      "percentComplete": 0
    }
  },
  "timestamp": "2026-01-15T10:30:05+08:00",
  "traceId": "req_abc123"
}
```

---

### 2.4 取得執行狀態

查詢回測執行進度。

**端點**
```
GET /api/v1/backtests/{backtestId}/status
```

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "backtestId": "BT_20260115_001",
    "status": "RUNNING",
    "progress": {
      "currentDay": "2024-06-15",
      "totalDays": 745,
      "completedDays": 380,
      "percentComplete": 51.0,
      "currentTrades": 23,
      "currentValue": 1125000
    },
    "startedAt": "2026-01-15T10:30:05+08:00",
    "estimatedCompletion": "2026-01-15T10:32:35+08:00",
    "elapsedTime": "PT1M15S"
  },
  "timestamp": "2026-01-15T10:31:20+08:00",
  "traceId": "req_abc123"
}
```

**狀態說明**

| 狀態 | 說明 |
|-----|------|
| CREATED | 已建立，等待執行 |
| RUNNING | 執行中 |
| COMPLETED | 執行完成 |
| FAILED | 執行失敗 |
| CANCELLED | 已取消 |

---

### 2.5 取消回測

取消正在執行的回測任務。

**端點**
```
POST /api/v1/backtests/{backtestId}/cancel
```

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Backtest cancelled",
  "data": {
    "backtestId": "BT_20260115_001",
    "status": "CANCELLED",
    "cancelledAt": "2026-01-15T10:31:30+08:00",
    "progress": {
      "completedDays": 380,
      "percentComplete": 51.0
    }
  },
  "timestamp": "2026-01-15T10:31:30+08:00",
  "traceId": "req_abc123"
}
```

---

### 2.6 取得回測報告

取得完整的回測結果報告。

**端點**
```
GET /api/v1/backtests/{backtestId}/report
```

**查詢參數**

| 參數 | 類型 | 必填 | 預設值 | 說明 |
|-----|------|:----:|-------|------|
| includeTrades | boolean | N | false | 是否包含交易明細 |
| benchmarkCode | string | N | TAIEX | 比較基準代碼 |

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "reportId": "RPT_BT_20260115_001",
    "backtestId": "BT_20260115_001",
    "generatedAt": "2026-01-15T10:32:35+08:00",

    "backtestInfo": {
      "name": "台積電突破策略回測",
      "strategy": "突破策略",
      "period": {
        "start": "2023-01-01",
        "end": "2025-12-31",
        "tradingDays": 745
      },
      "targetStocks": ["2330", "2317"],
      "initialCapital": 1000000
    },

    "performanceSummary": {
      "finalValue": 1234567,
      "totalReturn": 0.2346,
      "annualizedReturn": 0.0723,
      "benchmarkReturn": 0.1520,
      "alpha": 0.0456,
      "beta": 0.85,
      "sharpeRatio": 1.25,
      "sortinoRatio": 1.82,
      "calmarRatio": 1.90,
      "maxDrawdown": 0.1234,
      "maxDrawdownDuration": 45,
      "volatility": 0.18,
      "downsideVolatility": 0.12,
      "var95": 0.025,
      "cvar95": 0.038
    },

    "tradeStatistics": {
      "totalTrades": 48,
      "winningTrades": 28,
      "losingTrades": 20,
      "winRate": 0.5833,
      "avgWin": 25000,
      "avgLoss": 15000,
      "payoffRatio": 1.67,
      "profitFactor": 1.85,
      "avgHoldingDays": 12.5,
      "maxConsecutiveWins": 6,
      "maxConsecutiveLosses": 3,
      "largestWin": 85000,
      "largestLoss": 42000,
      "totalCommission": 15420,
      "totalTax": 18500,
      "totalSlippage": 12300
    },

    "monthlyReturns": {
      "2023": {
        "Jan": 0.032, "Feb": -0.015, "Mar": 0.045,
        "Apr": 0.028, "May": -0.008, "Jun": 0.052,
        "Jul": -0.025, "Aug": 0.018, "Sep": 0.035,
        "Oct": -0.012, "Nov": 0.041, "Dec": 0.022
      },
      "2024": {},
      "2025": {}
    },

    "drawdowns": [
      {
        "rank": 1,
        "startDate": "2023-06-15",
        "endDate": "2023-08-20",
        "recoveryDate": "2023-10-05",
        "depth": 0.1234,
        "duration": 66,
        "recoveryDays": 46
      },
      {
        "rank": 2,
        "startDate": "2024-03-10",
        "endDate": "2024-04-15",
        "recoveryDate": "2024-05-20",
        "depth": 0.0856,
        "duration": 36,
        "recoveryDays": 35
      }
    ],

    "tradesByStock": [
      {
        "stockId": "2330",
        "stockName": "台積電",
        "trades": 30,
        "winRate": 0.60,
        "totalPnL": 150000,
        "avgReturn": 0.045
      },
      {
        "stockId": "2317",
        "stockName": "鴻海",
        "trades": 18,
        "winRate": 0.55,
        "totalPnL": 84567,
        "avgReturn": 0.038
      }
    ]
  },
  "timestamp": "2026-01-15T10:35:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 2.7 取得交易明細

取得回測的交易記錄清單。

**端點**
```
GET /api/v1/backtests/{backtestId}/trades
```

**查詢參數**

| 參數 | 類型 | 必填 | 預設值 | 說明 |
|-----|------|:----:|-------|------|
| page | integer | N | 1 | 頁碼 |
| size | integer | N | 50 | 每頁筆數 (max: 100) |
| stockId | string | N | - | 篩選股票代碼 |
| tradeType | string | N | - | 篩選交易類型：BUY/SELL |
| startDate | date | N | - | 起始日期 |
| endDate | date | N | - | 結束日期 |

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "trades": [
      {
        "tradeId": "TRD_001",
        "tradeDate": "2023-03-15",
        "stockId": "2330",
        "stockName": "台積電",
        "tradeType": "BUY",
        "price": 525.00,
        "shares": 2000,
        "amount": 1050000,
        "commission": 1496,
        "tax": 0,
        "netAmount": 1051496,
        "signalSource": "M13_SIGNAL",
        "signalId": "SIG_20230315_001",
        "signalGrade": "A"
      },
      {
        "tradeId": "TRD_002",
        "tradeDate": "2023-04-10",
        "stockId": "2330",
        "stockName": "台積電",
        "tradeType": "SELL",
        "price": 562.00,
        "shares": 2000,
        "amount": 1124000,
        "commission": 1602,
        "tax": 3372,
        "netAmount": 1119026,
        "exitReason": "TAKE_PROFIT",
        "holdingDays": 26,
        "profitLoss": 67530,
        "returnPct": 0.0643
      }
    ],
    "summary": {
      "totalTrades": 48,
      "buyTrades": 24,
      "sellTrades": 24,
      "totalCommission": 15420,
      "totalTax": 18500
    },
    "pagination": {
      "page": 1,
      "size": 50,
      "totalItems": 48,
      "totalPages": 1
    }
  },
  "timestamp": "2026-01-15T10:35:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 2.8 取得淨值曲線

取得回測期間的每日淨值數據。

**端點**
```
GET /api/v1/backtests/{backtestId}/equity-curve
```

**查詢參數**

| 參數 | 類型 | 必填 | 預設值 | 說明 |
|-----|------|:----:|-------|------|
| interval | string | N | DAILY | 資料間隔：DAILY/WEEKLY/MONTHLY |
| includeBenchmark | boolean | N | true | 是否包含基準對照 |
| benchmarkCode | string | N | TAIEX | 基準代碼 |

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "backtestId": "BT_20260115_001",
    "interval": "DAILY",
    "benchmarkCode": "TAIEX",
    "equityCurve": [
      {
        "date": "2023-01-02",
        "portfolioValue": 1000000,
        "cash": 1000000,
        "positionValue": 0,
        "dailyReturn": 0,
        "cumulativeReturn": 0,
        "drawdown": 0,
        "benchmarkValue": 100,
        "benchmarkReturn": 0
      },
      {
        "date": "2023-01-03",
        "portfolioValue": 1005000,
        "cash": 895000,
        "positionValue": 110000,
        "dailyReturn": 0.005,
        "cumulativeReturn": 0.005,
        "drawdown": 0,
        "benchmarkValue": 100.5,
        "benchmarkReturn": 0.005
      }
    ],
    "statistics": {
      "dataPoints": 745,
      "highWaterMark": {
        "date": "2025-10-15",
        "value": 1298000
      },
      "lowPoint": {
        "date": "2023-08-20",
        "value": 875000
      }
    }
  },
  "timestamp": "2026-01-15T10:35:00+08:00",
  "traceId": "req_abc123"
}
```

---

## 3. P1 進階 API

### 3.1 取得回測清單

查詢用戶的回測歷史記錄。

**端點**
```
GET /api/v1/backtests
```

**查詢參數**

| 參數 | 類型 | 必填 | 預設值 | 說明 |
|-----|------|:----:|-------|------|
| page | integer | N | 1 | 頁碼 |
| size | integer | N | 20 | 每頁筆數 |
| status | string | N | - | 篩選狀態 |
| strategyId | string | N | - | 篩選策略 |
| startDate | date | N | - | 建立起始日期 |
| endDate | date | N | - | 建立結束日期 |
| sortBy | string | N | createdAt | 排序欄位 |
| sortDir | string | N | DESC | 排序方向 |

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "backtests": [
      {
        "backtestId": "BT_20260115_001",
        "backtestName": "台積電突破策略回測",
        "strategyName": "突破策略",
        "status": "COMPLETED",
        "targetStocksCount": 2,
        "period": "2023-01-01 ~ 2025-12-31",
        "initialCapital": 1000000,
        "totalReturn": 0.2346,
        "sharpeRatio": 1.25,
        "maxDrawdown": 0.1234,
        "createdAt": "2026-01-15T10:30:00+08:00"
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "totalItems": 15,
      "totalPages": 1
    }
  },
  "timestamp": "2026-01-15T10:35:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 3.2 刪除回測

刪除回測任務及其結果。

**端點**
```
DELETE /api/v1/backtests/{backtestId}
```

**成功回應 (204 No Content)**
```
(無回應主體)
```

---

### 3.3 執行參數最佳化

對回測進行參數最佳化搜尋。

**端點**
```
POST /api/v1/backtests/{backtestId}/optimize
```

**請求主體**
```json
{
  "optimizationMethod": "GRID_SEARCH",
  "targetMetric": "SHARPE_RATIO",
  "parameters": [
    {
      "name": "stopLoss",
      "type": "RANGE",
      "min": 0.05,
      "max": 0.15,
      "step": 0.01
    },
    {
      "name": "takeProfit",
      "type": "RANGE",
      "min": 0.10,
      "max": 0.30,
      "step": 0.02
    },
    {
      "name": "maxHoldingDays",
      "type": "VALUES",
      "values": [10, 20, 30, 60]
    }
  ],
  "constraints": {
    "minTrades": 20,
    "maxDrawdown": 0.25
  }
}
```

**參數說明**

| 參數 | 類型 | 必填 | 說明 |
|-----|------|:----:|------|
| optimizationMethod | string | Y | 最佳化方法：GRID_SEARCH/RANDOM_SEARCH/GENETIC |
| targetMetric | string | Y | 目標指標：SHARPE_RATIO/TOTAL_RETURN/CALMAR_RATIO |
| parameters | array | Y | 待最佳化參數 |
| parameters[].name | string | Y | 參數名稱 |
| parameters[].type | string | Y | 參數類型：RANGE/VALUES |
| parameters[].min | number | N | 範圍最小值 (type=RANGE) |
| parameters[].max | number | N | 範圍最大值 (type=RANGE) |
| parameters[].step | number | N | 步進值 (type=RANGE) |
| parameters[].values | array | N | 離散值清單 (type=VALUES) |
| constraints | object | N | 約束條件 |

**成功回應 (202 Accepted)**
```json
{
  "code": 202,
  "message": "Optimization started",
  "data": {
    "optimizationId": "OPT_20260115_001",
    "backtestId": "BT_20260115_001",
    "status": "RUNNING",
    "totalCombinations": 528,
    "estimatedDuration": "PT10M",
    "startedAt": "2026-01-15T11:00:00+08:00"
  },
  "timestamp": "2026-01-15T11:00:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 3.4 取得最佳化結果

取得參數最佳化的結果。

**端點**
```
GET /api/v1/backtests/{backtestId}/optimization-result
```

**查詢參數**

| 參數 | 類型 | 必填 | 預設值 | 說明 |
|-----|------|:----:|-------|------|
| optimizationId | string | N | - | 指定最佳化 ID |
| topN | integer | N | 10 | 返回前 N 名結果 |

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "optimizationId": "OPT_20260115_001",
    "status": "COMPLETED",
    "totalCombinations": 528,
    "completedCombinations": 528,
    "duration": "PT8M32S",

    "bestResult": {
      "rank": 1,
      "parameters": {
        "stopLoss": 0.07,
        "takeProfit": 0.20,
        "maxHoldingDays": 20
      },
      "metrics": {
        "sharpeRatio": 1.45,
        "totalReturn": 0.35,
        "annualizedReturn": 0.11,
        "maxDrawdown": 0.12,
        "winRate": 0.62,
        "profitFactor": 2.1,
        "totalTrades": 45
      }
    },

    "topResults": [
      {
        "rank": 1,
        "parameters": {"stopLoss": 0.07, "takeProfit": 0.20, "maxHoldingDays": 20},
        "sharpeRatio": 1.45,
        "totalReturn": 0.35
      },
      {
        "rank": 2,
        "parameters": {"stopLoss": 0.06, "takeProfit": 0.18, "maxHoldingDays": 20},
        "sharpeRatio": 1.42,
        "totalReturn": 0.33
      }
    ],

    "parameterSensitivity": [
      {
        "parameter": "stopLoss",
        "impact": "HIGH",
        "optimalRange": {"min": 0.06, "max": 0.08},
        "correlation": -0.35
      },
      {
        "parameter": "takeProfit",
        "impact": "MEDIUM",
        "optimalRange": {"min": 0.18, "max": 0.22},
        "correlation": 0.28
      }
    ],

    "completedAt": "2026-01-15T11:08:32+08:00"
  },
  "timestamp": "2026-01-15T11:10:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 3.5 多策略比較

比較多個策略或回測的績效。

**端點**
```
POST /api/v1/backtests/compare
```

**請求主體**
```json
{
  "backtestIds": ["BT_001", "BT_002", "BT_003"],
  "benchmarkCode": "TAIEX",
  "metrics": ["TOTAL_RETURN", "SHARPE_RATIO", "MAX_DRAWDOWN", "WIN_RATE"]
}
```

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "comparisonId": "CMP_20260115_001",
    "benchmarkCode": "TAIEX",
    "benchmarkReturn": 0.152,

    "strategies": [
      {
        "backtestId": "BT_001",
        "strategyName": "突破策略",
        "totalReturn": 0.35,
        "sharpeRatio": 1.45,
        "maxDrawdown": 0.12,
        "winRate": 0.62
      },
      {
        "backtestId": "BT_002",
        "strategyName": "均線策略",
        "totalReturn": 0.28,
        "sharpeRatio": 1.10,
        "maxDrawdown": 0.15,
        "winRate": 0.52
      },
      {
        "backtestId": "BT_003",
        "strategyName": "價值投資",
        "totalReturn": 0.22,
        "sharpeRatio": 0.95,
        "maxDrawdown": 0.18,
        "winRate": 0.48
      }
    ],

    "ranking": {
      "byTotalReturn": ["BT_001", "BT_002", "BT_003"],
      "bySharpeRatio": ["BT_001", "BT_002", "BT_003"],
      "byMaxDrawdown": ["BT_001", "BT_002", "BT_003"],
      "byWinRate": ["BT_001", "BT_002", "BT_003"]
    },

    "correlationMatrix": {
      "BT_001": {"BT_001": 1.00, "BT_002": 0.65, "BT_003": 0.42},
      "BT_002": {"BT_001": 0.65, "BT_002": 1.00, "BT_003": 0.58},
      "BT_003": {"BT_001": 0.42, "BT_002": 0.58, "BT_003": 1.00}
    }
  },
  "timestamp": "2026-01-15T11:15:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 3.6 取得可用基準

取得系統支援的比較基準清單。

**端點**
```
GET /api/v1/backtests/benchmarks
```

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "benchmarks": [
      {
        "code": "TAIEX",
        "name": "加權指數",
        "description": "台灣加權股價指數",
        "dataFrom": "2000-01-04"
      },
      {
        "code": "TPEx",
        "name": "櫃買指數",
        "description": "櫃檯買賣指數",
        "dataFrom": "2000-01-04"
      },
      {
        "code": "TECH",
        "name": "電子類指數",
        "description": "電子類股指數",
        "dataFrom": "2000-01-04"
      },
      {
        "code": "FIN",
        "name": "金融類指數",
        "description": "金融類股指數",
        "dataFrom": "2000-01-04"
      }
    ]
  },
  "timestamp": "2026-01-15T11:15:00+08:00",
  "traceId": "req_abc123"
}
```

---

## 4. P2 次要 API

### 4.1 取得回測範本

取得用戶儲存的回測設定範本。

**端點**
```
GET /api/v1/backtests/templates
```

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "templates": [
      {
        "templateId": "TPL_001",
        "templateName": "保守型設定",
        "description": "低風險、穩健報酬",
        "settings": {
          "tradingSettings": {
            "positionSizing": "FIXED_AMOUNT",
            "fixedAmount": 50000,
            "maxPositions": 10
          },
          "exitRules": {
            "stopLoss": 0.05,
            "takeProfit": 0.10,
            "maxHoldingDays": 60
          }
        },
        "createdAt": "2026-01-10T09:00:00+08:00"
      }
    ]
  },
  "timestamp": "2026-01-15T11:15:00+08:00",
  "traceId": "req_abc123"
}
```

### 4.2 建立回測範本

儲存回測設定為範本。

**端點**
```
POST /api/v1/backtests/templates
```

**請求主體**
```json
{
  "templateName": "積極型設定",
  "description": "高風險、高報酬",
  "settings": {
    "tradingSettings": {
      "positionSizing": "PERCENT",
      "percentOfCapital": 0.20,
      "maxPositions": 5
    },
    "exitRules": {
      "stopLoss": 0.10,
      "takeProfit": 0.25,
      "trailingStop": 0.05,
      "maxHoldingDays": 20
    }
  }
}
```

**成功回應 (201 Created)**
```json
{
  "code": 201,
  "message": "Template created successfully",
  "data": {
    "templateId": "TPL_002",
    "templateName": "積極型設定",
    "createdAt": "2026-01-15T11:20:00+08:00"
  },
  "timestamp": "2026-01-15T11:20:00+08:00",
  "traceId": "req_abc123"
}
```

### 4.3 匯出回測結果

匯出回測結果為檔案格式。

**端點**
```
GET /api/v1/backtests/{backtestId}/export
```

**查詢參數**

| 參數 | 類型 | 必填 | 預設值 | 說明 |
|-----|------|:----:|-------|------|
| format | string | N | CSV | 匯出格式：CSV/EXCEL/PDF |
| content | string | N | ALL | 匯出內容：ALL/TRADES/EQUITY/REPORT |

**成功回應 (200 OK)**

回傳檔案下載，Content-Type 依 format 而定。

---

## 5. 共用錯誤回應

| 錯誤碼 | HTTP Status | 說明 |
|-------|-------------|------|
| M16-001 | 404 | 回測任務不存在 |
| M16-002 | 400 | 回測參數無效 |
| M16-003 | 400 | 回測期間超過限制 (10 年) |
| M16-004 | 400 | 股票數量超過限制 (200 檔) |
| M16-005 | 400 | 初始資金不足 |
| M16-006 | 400 | 歷史數據不足 |
| M16-007 | 404 | 策略不存在 |
| M16-008 | 500 | 回測執行失敗 |
| M16-009 | 409 | 回測已取消 |
| M16-010 | 400 | 最佳化組合超過限制 (1000 組) |

**錯誤回應格式**
```json
{
  "code": 400,
  "message": "Invalid request",
  "error": {
    "errorCode": "M16-003",
    "errorMessage": "回測期間超過限制",
    "details": {
      "maxYears": 10,
      "requestedYears": 12
    }
  },
  "timestamp": "2026-01-15T10:30:00+08:00",
  "traceId": "req_abc123"
}
```

---

## 6. 相關文檔

- [M16 功能需求](../functional/M16-回測系統功能需求.md)
- [M16 資料庫設計](../../design/M16-資料庫設計.md)
- [M11 量化策略 API](./M11-API規格.md)
- [M13 信號引擎 API](./M13-API規格.md)

---

**文件維護者**: 後端工程師
**最後更新**: 2026-01-15
**下次審核**: 2026-04-15
