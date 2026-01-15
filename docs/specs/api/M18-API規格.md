# M18 投資組合管理 API 規格

## 文件資訊
| 項目 | 內容 |
|------|------|
| 模組代號 | M18 |
| 模組名稱 | 投資組合管理 Portfolio Management |
| API 版本 | v1 |
| 基礎路徑 | `/api/v1/portfolios` |
| 文件版本 | 1.0 |
| 建立日期 | 2026-01-15 |

---

## API 總覽

| # | 端點 | 方法 | 說明 |
|---|------|------|------|
| 1 | `/portfolios` | POST | 建立投資組合 |
| 2 | `/portfolios` | GET | 查詢投資組合列表 |
| 3 | `/portfolios/{id}` | GET | 查詢單一投資組合 |
| 4 | `/portfolios/{id}` | PUT | 更新投資組合 |
| 5 | `/portfolios/{id}` | DELETE | 刪除投資組合 |
| 6 | `/portfolios/{id}/positions` | GET | 查詢持倉列表 |
| 7 | `/portfolios/{id}/positions/{stockId}` | GET | 查詢單一持倉 |
| 8 | `/portfolios/{id}/trades` | POST | 記錄交易 |
| 9 | `/portfolios/{id}/trades` | GET | 查詢交易記錄 |
| 10 | `/portfolios/{id}/performance` | GET | 查詢績效指標 |
| 11 | `/portfolios/{id}/performance/history` | GET | 查詢歷史績效 |
| 12 | `/portfolios/{id}/snapshots` | GET | 查詢投組快照 |
| 13 | `/portfolios/{id}/snapshots/latest` | GET | 查詢最新快照 |
| 14 | `/portfolios/{id}/signals` | GET | 查詢訂閱信號 |
| 15 | `/portfolios/{id}/signals/subscribe` | POST | 訂閱信號 |
| 16 | `/portfolios/{id}/signals/unsubscribe` | POST | 取消訂閱信號 |
| 17 | `/portfolios/{id}/rebalance` | GET | 取得再平衡建議 |
| 18 | `/portfolios/{id}/rebalance/execute` | POST | 執行再平衡 |
| 19 | `/portfolios/{id}/attribution` | GET | 查詢績效歸因 |
| 20 | `/portfolios/{id}/benchmark` | GET | 查詢基準比較 |
| 21 | `/portfolios/{id}/cash` | GET | 查詢現金部位 |
| 22 | `/portfolios/{id}/cash/transactions` | POST | 記錄現金異動 |
| 23 | `/portfolios/{id}/dividends` | GET | 查詢股利記錄 |
| 24 | `/portfolios/{id}/dividends/reinvest` | POST | 股利再投資 |
| 25 | `/portfolios/{id}/reports` | POST | 產生投組報告 |
| 26 | `/portfolios/{id}/reports/{reportId}` | GET | 下載投組報告 |
| 27 | `/benchmarks` | GET | 查詢可用基準 |

---

## API 詳細規格

### 1. POST /portfolios
建立新的投資組合

**Request Body:**
```json
{
  "name": "我的投資組合",
  "description": "長期價值投資組合",
  "currency": "TWD",
  "benchmarkId": "0050",
  "initialCash": 1000000,
  "targetAllocations": [
    {
      "stockId": "2330",
      "targetWeight": 0.30,
      "minWeight": 0.20,
      "maxWeight": 0.40
    },
    {
      "stockId": "2317",
      "targetWeight": 0.20,
      "minWeight": 0.10,
      "maxWeight": 0.30
    }
  ],
  "settings": {
    "rebalanceThreshold": 0.05,
    "autoRebalance": false,
    "trackDividends": true,
    "dividendReinvest": false
  }
}
```

**Response (201 Created):**
```json
{
  "code": 201,
  "message": "投資組合建立成功",
  "data": {
    "id": "pf_001",
    "name": "我的投資組合",
    "description": "長期價值投資組合",
    "currency": "TWD",
    "benchmarkId": "0050",
    "cashBalance": 1000000,
    "totalValue": 1000000,
    "status": "ACTIVE",
    "createdAt": "2026-01-15T10:00:00+08:00"
  },
  "timestamp": "2026-01-15T10:00:00+08:00"
}
```

---

### 2. GET /portfolios
查詢投資組合列表

**Query Parameters:**
| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| status | String | 否 | 狀態篩選 (ACTIVE/CLOSED) |
| page | Integer | 否 | 頁碼，預設 0 |
| size | Integer | 否 | 每頁筆數，預設 20 |
| sort | String | 否 | 排序欄位 (createdAt/totalValue/name) |

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": "pf_001",
        "name": "我的投資組合",
        "totalValue": 1250000,
        "cashBalance": 150000,
        "positionCount": 5,
        "todayReturn": 0.0125,
        "totalReturn": 0.25,
        "status": "ACTIVE",
        "updatedAt": "2026-01-15T09:00:00+08:00"
      }
    ],
    "totalElements": 3,
    "totalPages": 1,
    "number": 0,
    "size": 20
  },
  "timestamp": "2026-01-15T10:00:00+08:00"
}
```

---

### 3. GET /portfolios/{id}
查詢單一投資組合詳情

**Path Parameters:**
| 參數 | 類型 | 說明 |
|------|------|------|
| id | String | 投資組合 ID |

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "id": "pf_001",
    "name": "我的投資組合",
    "description": "長期價值投資組合",
    "currency": "TWD",
    "benchmarkId": "0050",
    "benchmarkName": "元大台灣50",
    "cashBalance": 150000,
    "totalValue": 1250000,
    "marketValue": 1100000,
    "unrealizedPnL": 180000,
    "realizedPnL": 45000,
    "status": "ACTIVE",
    "positions": [
      {
        "stockId": "2330",
        "stockName": "台積電",
        "shares": 1000,
        "avgCost": 550.00,
        "currentPrice": 680.00,
        "marketValue": 680000,
        "weight": 0.544,
        "targetWeight": 0.30,
        "unrealizedPnL": 130000,
        "unrealizedPnLPct": 0.2364
      }
    ],
    "targetAllocations": [
      {
        "stockId": "2330",
        "targetWeight": 0.30,
        "currentWeight": 0.544,
        "deviation": 0.244,
        "needsRebalance": true
      }
    ],
    "settings": {
      "rebalanceThreshold": 0.05,
      "autoRebalance": false,
      "trackDividends": true,
      "dividendReinvest": false
    },
    "createdAt": "2025-06-01T10:00:00+08:00",
    "updatedAt": "2026-01-15T09:00:00+08:00"
  },
  "timestamp": "2026-01-15T10:00:00+08:00"
}
```

---

### 4. PUT /portfolios/{id}
更新投資組合設定

**Request Body:**
```json
{
  "name": "價值投資組合",
  "description": "更新後的描述",
  "benchmarkId": "0051",
  "targetAllocations": [
    {
      "stockId": "2330",
      "targetWeight": 0.25,
      "minWeight": 0.15,
      "maxWeight": 0.35
    }
  ],
  "settings": {
    "rebalanceThreshold": 0.03,
    "autoRebalance": true
  }
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "投資組合更新成功",
  "data": {
    "id": "pf_001",
    "name": "價值投資組合",
    "updatedAt": "2026-01-15T10:05:00+08:00"
  },
  "timestamp": "2026-01-15T10:05:00+08:00"
}
```

---

### 5. DELETE /portfolios/{id}
刪除投資組合（軟刪除）

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "投資組合已關閉",
  "data": {
    "id": "pf_001",
    "status": "CLOSED",
    "closedAt": "2026-01-15T10:10:00+08:00"
  },
  "timestamp": "2026-01-15T10:10:00+08:00"
}
```

---

### 6. GET /portfolios/{id}/positions
查詢持倉列表

**Query Parameters:**
| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| sort | String | 否 | 排序 (weight/value/pnl) |
| includeZero | Boolean | 否 | 包含零持倉，預設 false |

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "portfolioId": "pf_001",
    "asOfDate": "2026-01-15",
    "totalMarketValue": 1100000,
    "positions": [
      {
        "stockId": "2330",
        "stockName": "台積電",
        "industry": "半導體業",
        "shares": 1000,
        "avgCost": 550.00,
        "totalCost": 550000,
        "currentPrice": 680.00,
        "marketValue": 680000,
        "weight": 0.618,
        "unrealizedPnL": 130000,
        "unrealizedPnLPct": 0.2364,
        "dayChange": 5.00,
        "dayChangePct": 0.0074,
        "firstBuyDate": "2025-06-15",
        "lastTradeDate": "2025-12-10",
        "holdingDays": 214
      },
      {
        "stockId": "2317",
        "stockName": "鴻海",
        "industry": "其他電子業",
        "shares": 2000,
        "avgCost": 105.00,
        "totalCost": 210000,
        "currentPrice": 115.00,
        "marketValue": 230000,
        "weight": 0.209,
        "unrealizedPnL": 20000,
        "unrealizedPnLPct": 0.0952,
        "dayChange": -1.50,
        "dayChangePct": -0.0129,
        "firstBuyDate": "2025-08-20",
        "lastTradeDate": "2025-11-05",
        "holdingDays": 148
      }
    ],
    "summary": {
      "positionCount": 5,
      "totalUnrealizedPnL": 180000,
      "topHolding": "2330",
      "industryConcentration": {
        "半導體業": 0.618,
        "其他電子業": 0.209,
        "金融保險業": 0.173
      }
    }
  },
  "timestamp": "2026-01-15T10:00:00+08:00"
}
```

---

### 7. GET /portfolios/{id}/positions/{stockId}
查詢單一持倉詳情

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "portfolioId": "pf_001",
    "stockId": "2330",
    "stockName": "台積電",
    "shares": 1000,
    "avgCost": 550.00,
    "currentPrice": 680.00,
    "marketValue": 680000,
    "unrealizedPnL": 130000,
    "unrealizedPnLPct": 0.2364,
    "costLots": [
      {
        "lotId": "lot_001",
        "purchaseDate": "2025-06-15",
        "shares": 500,
        "costPerShare": 520.00,
        "totalCost": 260000,
        "currentValue": 340000,
        "pnl": 80000,
        "holdingDays": 214
      },
      {
        "lotId": "lot_002",
        "purchaseDate": "2025-09-20",
        "shares": 500,
        "costPerShare": 580.00,
        "totalCost": 290000,
        "currentValue": 340000,
        "pnl": 50000,
        "holdingDays": 117
      }
    ],
    "dividendHistory": [
      {
        "exDate": "2025-09-15",
        "payDate": "2025-10-15",
        "dividendPerShare": 3.50,
        "totalDividend": 1750,
        "reinvested": false
      }
    ],
    "tradeHistory": [
      {
        "tradeDate": "2025-06-15",
        "type": "BUY",
        "shares": 500,
        "price": 520.00,
        "amount": 260000
      },
      {
        "tradeDate": "2025-09-20",
        "type": "BUY",
        "shares": 500,
        "price": 580.00,
        "amount": 290000
      }
    ]
  },
  "timestamp": "2026-01-15T10:00:00+08:00"
}
```

---

### 8. POST /portfolios/{id}/trades
記錄交易

**Request Body:**
```json
{
  "stockId": "2330",
  "tradeDate": "2026-01-15",
  "tradeTime": "09:15:00",
  "type": "BUY",
  "shares": 500,
  "price": 685.00,
  "fees": 487,
  "tax": 0,
  "notes": "加碼買進"
}
```

**Response (201 Created):**
```json
{
  "code": 201,
  "message": "交易記錄成功",
  "data": {
    "tradeId": "tr_20260115_001",
    "portfolioId": "pf_001",
    "stockId": "2330",
    "type": "BUY",
    "shares": 500,
    "price": 685.00,
    "grossAmount": 342500,
    "fees": 487,
    "tax": 0,
    "netAmount": 342987,
    "position": {
      "newShares": 1500,
      "newAvgCost": 594.33,
      "totalCost": 891500
    },
    "cashBalance": {
      "before": 493000,
      "after": 150013,
      "change": -342987
    },
    "createdAt": "2026-01-15T09:16:00+08:00"
  },
  "timestamp": "2026-01-15T09:16:00+08:00"
}
```

**交易類型:**
| 類型 | 說明 |
|------|------|
| BUY | 買進 |
| SELL | 賣出 |
| DIVIDEND_REINVEST | 股利再投資 |
| STOCK_DIVIDEND | 股票股利 |
| SPLIT | 股票分割 |
| TRANSFER_IN | 轉入 |
| TRANSFER_OUT | 轉出 |

---

### 9. GET /portfolios/{id}/trades
查詢交易記錄

**Query Parameters:**
| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| stockId | String | 否 | 股票代碼篩選 |
| type | String | 否 | 交易類型篩選 |
| startDate | String | 否 | 開始日期 |
| endDate | String | 否 | 結束日期 |
| page | Integer | 否 | 頁碼 |
| size | Integer | 否 | 每頁筆數 |

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "tradeId": "tr_20260115_001",
        "tradeDate": "2026-01-15",
        "tradeTime": "09:15:00",
        "stockId": "2330",
        "stockName": "台積電",
        "type": "BUY",
        "shares": 500,
        "price": 685.00,
        "grossAmount": 342500,
        "fees": 487,
        "tax": 0,
        "netAmount": 342987,
        "notes": "加碼買進"
      }
    ],
    "totalElements": 25,
    "totalPages": 3,
    "summary": {
      "totalBuys": 18,
      "totalSells": 7,
      "totalBuyAmount": 2850000,
      "totalSellAmount": 680000,
      "totalFees": 5230,
      "totalTax": 2040,
      "netCashFlow": -2177270
    }
  },
  "timestamp": "2026-01-15T10:00:00+08:00"
}
```

---

### 10. GET /portfolios/{id}/performance
查詢績效指標

**Query Parameters:**
| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| period | String | 否 | 期間 (1M/3M/6M/YTD/1Y/3Y/ALL) |
| includeRiskMetrics | Boolean | 否 | 包含風險指標 |

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "portfolioId": "pf_001",
    "asOfDate": "2026-01-15",
    "period": "YTD",
    "periodStart": "2026-01-01",
    "periodEnd": "2026-01-15",
    "returns": {
      "twr": 0.0285,
      "twrAnnualized": 0.8234,
      "mwr": 0.0312,
      "mwrAnnualized": 0.8956,
      "simpleReturn": 0.0275,
      "cumulativeReturn": 0.25
    },
    "riskMetrics": {
      "volatility": 0.1825,
      "volatilityAnnualized": 0.2895,
      "sharpeRatio": 1.45,
      "sortinoRatio": 2.12,
      "maxDrawdown": -0.0823,
      "maxDrawdownDate": "2025-08-15",
      "recoveryDate": "2025-10-02",
      "calmarRatio": 3.05,
      "beta": 1.12,
      "alpha": 0.0035,
      "treynorRatio": 0.0856,
      "informationRatio": 0.78,
      "trackingError": 0.0542
    },
    "comparison": {
      "benchmarkId": "0050",
      "benchmarkName": "元大台灣50",
      "benchmarkReturn": 0.0185,
      "excessReturn": 0.0100,
      "relativeStrength": 1.54
    },
    "periodReturns": {
      "1D": 0.0125,
      "1W": 0.0234,
      "1M": 0.0285,
      "3M": 0.0756,
      "6M": 0.1245,
      "YTD": 0.0285,
      "1Y": 0.2150,
      "inception": 0.25
    }
  },
  "timestamp": "2026-01-15T10:00:00+08:00"
}
```

---

### 11. GET /portfolios/{id}/performance/history
查詢歷史績效

**Query Parameters:**
| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| startDate | String | 是 | 開始日期 |
| endDate | String | 是 | 結束日期 |
| interval | String | 否 | 間隔 (DAILY/WEEKLY/MONTHLY) |

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "portfolioId": "pf_001",
    "interval": "DAILY",
    "history": [
      {
        "date": "2026-01-15",
        "totalValue": 1250000,
        "marketValue": 1100000,
        "cashBalance": 150000,
        "dailyReturn": 0.0125,
        "cumulativeReturn": 0.25,
        "twrCumulative": 0.2485,
        "benchmarkReturn": 0.0085,
        "benchmarkCumulative": 0.18,
        "excessReturn": 0.0040
      },
      {
        "date": "2026-01-14",
        "totalValue": 1234500,
        "marketValue": 1084500,
        "cashBalance": 150000,
        "dailyReturn": 0.0078,
        "cumulativeReturn": 0.2347
      }
    ],
    "chart": {
      "labels": ["2026-01-01", "2026-01-02", "..."],
      "portfolioValues": [1200000, 1205000, "..."],
      "benchmarkValues": [100, 100.5, "..."],
      "cumulativeReturns": [0, 0.0042, "..."]
    }
  },
  "timestamp": "2026-01-15T10:00:00+08:00"
}
```

---

### 12. GET /portfolios/{id}/snapshots
查詢投組快照

**Query Parameters:**
| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| startDate | String | 否 | 開始日期 |
| endDate | String | 否 | 結束日期 |
| page | Integer | 否 | 頁碼 |
| size | Integer | 否 | 每頁筆數 |

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "content": [
      {
        "snapshotId": "snap_20260115",
        "snapshotDate": "2026-01-15",
        "totalValue": 1250000,
        "marketValue": 1100000,
        "cashBalance": 150000,
        "unrealizedPnL": 180000,
        "realizedPnL": 45000,
        "dailyPnL": 15500,
        "dailyReturn": 0.0125,
        "positionCount": 5,
        "positions": [
          {
            "stockId": "2330",
            "shares": 1000,
            "avgCost": 550.00,
            "closePrice": 680.00,
            "marketValue": 680000,
            "weight": 0.544
          }
        ]
      }
    ],
    "totalElements": 200,
    "totalPages": 10
  },
  "timestamp": "2026-01-15T10:00:00+08:00"
}
```

---

### 13. GET /portfolios/{id}/snapshots/latest
查詢最新快照

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "snapshotId": "snap_20260115",
    "snapshotDate": "2026-01-15",
    "snapshotTime": "13:30:00",
    "totalValue": 1250000,
    "marketValue": 1100000,
    "cashBalance": 150000,
    "unrealizedPnL": 180000,
    "realizedPnL": 45000,
    "dailyPnL": 15500,
    "dailyReturn": 0.0125,
    "positions": [
      {
        "stockId": "2330",
        "stockName": "台積電",
        "shares": 1000,
        "avgCost": 550.00,
        "closePrice": 680.00,
        "marketValue": 680000,
        "weight": 0.544,
        "unrealizedPnL": 130000
      }
    ]
  },
  "timestamp": "2026-01-15T13:30:00+08:00"
}
```

---

### 14. GET /portfolios/{id}/signals
查詢訂閱的信號

**Query Parameters:**
| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| status | String | 否 | 狀態 (ACTIVE/TRIGGERED/EXPIRED) |
| signalType | String | 否 | 信號類型篩選 |

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "portfolioId": "pf_001",
    "subscriptions": [
      {
        "subscriptionId": "sub_001",
        "stockId": "2330",
        "stockName": "台積電",
        "signalTypes": ["TECHNICAL_BUY", "TECHNICAL_SELL", "FUNDAMENTAL_VALUE"],
        "status": "ACTIVE",
        "createdAt": "2025-12-01T10:00:00+08:00"
      }
    ],
    "recentSignals": [
      {
        "signalId": "sig_20260115_001",
        "stockId": "2330",
        "signalType": "TECHNICAL_BUY",
        "signalSource": "M13",
        "strength": 0.85,
        "message": "MACD 黃金交叉，RSI 超賣反彈",
        "triggeredAt": "2026-01-15T09:30:00+08:00",
        "acknowledged": false
      }
    ],
    "summary": {
      "activeSubscriptions": 5,
      "pendingSignals": 3,
      "todaySignals": 2
    }
  },
  "timestamp": "2026-01-15T10:00:00+08:00"
}
```

---

### 15. POST /portfolios/{id}/signals/subscribe
訂閱信號

**Request Body:**
```json
{
  "stockIds": ["2330", "2317", "2454"],
  "signalTypes": [
    "TECHNICAL_BUY",
    "TECHNICAL_SELL",
    "FUNDAMENTAL_VALUE",
    "FUNDAMENTAL_GROWTH"
  ],
  "minStrength": 0.7,
  "notifyEmail": true,
  "notifyPush": true
}
```

**Response (201 Created):**
```json
{
  "code": 201,
  "message": "信號訂閱成功",
  "data": {
    "subscriptions": [
      {
        "subscriptionId": "sub_002",
        "stockId": "2330",
        "signalTypes": ["TECHNICAL_BUY", "TECHNICAL_SELL"],
        "status": "ACTIVE"
      }
    ],
    "totalSubscribed": 3
  },
  "timestamp": "2026-01-15T10:00:00+08:00"
}
```

---

### 16. POST /portfolios/{id}/signals/unsubscribe
取消訂閱信號

**Request Body:**
```json
{
  "subscriptionIds": ["sub_001", "sub_002"],
  "stockIds": ["2330"]
}
```

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "信號訂閱已取消",
  "data": {
    "unsubscribed": 2
  },
  "timestamp": "2026-01-15T10:00:00+08:00"
}
```

---

### 17. GET /portfolios/{id}/rebalance
取得再平衡建議

**Query Parameters:**
| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| method | String | 否 | 再平衡方法 (THRESHOLD/PERIODIC/TACTICAL) |
| cashConstraint | Boolean | 否 | 考慮現金限制 |

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "portfolioId": "pf_001",
    "asOfDate": "2026-01-15",
    "needsRebalance": true,
    "rebalanceReason": "權重偏離超過閾值",
    "currentState": {
      "totalValue": 1250000,
      "cashBalance": 150000,
      "cashWeight": 0.12
    },
    "recommendations": [
      {
        "stockId": "2330",
        "stockName": "台積電",
        "currentShares": 1000,
        "currentWeight": 0.544,
        "targetWeight": 0.30,
        "deviation": 0.244,
        "action": "SELL",
        "recommendedShares": 450,
        "estimatedAmount": 306000,
        "estimatedFees": 918,
        "estimatedTax": 918,
        "priority": 1
      },
      {
        "stockId": "2317",
        "stockName": "鴻海",
        "currentShares": 2000,
        "currentWeight": 0.184,
        "targetWeight": 0.20,
        "deviation": -0.016,
        "action": "BUY",
        "recommendedShares": 174,
        "estimatedAmount": 20010,
        "estimatedFees": 28,
        "priority": 2
      }
    ],
    "summary": {
      "totalSellAmount": 306000,
      "totalBuyAmount": 85000,
      "netCashChange": 219054,
      "estimatedFees": 1200,
      "estimatedTax": 918,
      "postRebalanceCash": 369054,
      "turnoverRate": 0.156
    },
    "warnings": [
      "部分建議交易可能產生短期資本利得稅"
    ]
  },
  "timestamp": "2026-01-15T10:00:00+08:00"
}
```

---

### 18. POST /portfolios/{id}/rebalance/execute
執行再平衡

**Request Body:**
```json
{
  "recommendations": [
    {
      "stockId": "2330",
      "action": "SELL",
      "shares": 450,
      "limitPrice": 680.00
    },
    {
      "stockId": "2317",
      "action": "BUY",
      "shares": 174,
      "limitPrice": 116.00
    }
  ],
  "executeImmediately": false,
  "validUntil": "2026-01-15T13:30:00+08:00"
}
```

**Response (202 Accepted):**
```json
{
  "code": 202,
  "message": "再平衡訂單已建立",
  "data": {
    "rebalanceId": "rb_20260115_001",
    "status": "PENDING",
    "orders": [
      {
        "orderId": "ord_001",
        "stockId": "2330",
        "action": "SELL",
        "shares": 450,
        "limitPrice": 680.00,
        "status": "PENDING"
      },
      {
        "orderId": "ord_002",
        "stockId": "2317",
        "action": "BUY",
        "shares": 174,
        "limitPrice": 116.00,
        "status": "PENDING"
      }
    ],
    "validUntil": "2026-01-15T13:30:00+08:00"
  },
  "timestamp": "2026-01-15T10:00:00+08:00"
}
```

---

### 19. GET /portfolios/{id}/attribution
查詢績效歸因

**Query Parameters:**
| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| startDate | String | 是 | 開始日期 |
| endDate | String | 是 | 結束日期 |
| method | String | 否 | 歸因方法 (BRINSON/FACTOR) |

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "portfolioId": "pf_001",
    "period": {
      "startDate": "2025-01-01",
      "endDate": "2025-12-31"
    },
    "method": "BRINSON",
    "totalReturn": 0.2150,
    "benchmarkReturn": 0.1520,
    "activeReturn": 0.0630,
    "attribution": {
      "allocationEffect": 0.0215,
      "selectionEffect": 0.0380,
      "interactionEffect": 0.0035,
      "total": 0.0630
    },
    "sectorAttribution": [
      {
        "sector": "半導體業",
        "portfolioWeight": 0.45,
        "benchmarkWeight": 0.35,
        "portfolioReturn": 0.35,
        "benchmarkReturn": 0.28,
        "allocationEffect": 0.028,
        "selectionEffect": 0.0245,
        "totalEffect": 0.0525
      },
      {
        "sector": "金融保險業",
        "portfolioWeight": 0.15,
        "benchmarkWeight": 0.20,
        "portfolioReturn": 0.08,
        "benchmarkReturn": 0.12,
        "allocationEffect": -0.006,
        "selectionEffect": -0.008,
        "totalEffect": -0.014
      }
    ],
    "stockAttribution": [
      {
        "stockId": "2330",
        "stockName": "台積電",
        "portfolioWeight": 0.30,
        "portfolioReturn": 0.40,
        "contribution": 0.12
      }
    ]
  },
  "timestamp": "2026-01-15T10:00:00+08:00"
}
```

---

### 20. GET /portfolios/{id}/benchmark
查詢基準比較

**Query Parameters:**
| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| period | String | 否 | 比較期間 |
| metrics | String[] | 否 | 指定比較指標 |

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "portfolioId": "pf_001",
    "benchmarkId": "0050",
    "benchmarkName": "元大台灣50",
    "period": "1Y",
    "comparison": {
      "portfolio": {
        "return": 0.2150,
        "volatility": 0.2895,
        "sharpe": 1.45,
        "maxDrawdown": -0.0823,
        "beta": 1.12
      },
      "benchmark": {
        "return": 0.1520,
        "volatility": 0.2456,
        "sharpe": 1.12,
        "maxDrawdown": -0.1025,
        "beta": 1.00
      },
      "difference": {
        "return": 0.0630,
        "volatility": 0.0439,
        "sharpe": 0.33,
        "maxDrawdown": 0.0202,
        "alpha": 0.0035
      }
    },
    "relativeMetrics": {
      "informationRatio": 0.78,
      "trackingError": 0.0542,
      "upCapture": 1.15,
      "downCapture": 0.92,
      "winRate": 0.58
    },
    "rollingComparison": [
      {
        "date": "2026-01-15",
        "portfolioReturn": 0.0125,
        "benchmarkReturn": 0.0085,
        "excessReturn": 0.0040,
        "cumulativeExcess": 0.0630
      }
    ]
  },
  "timestamp": "2026-01-15T10:00:00+08:00"
}
```

---

### 21. GET /portfolios/{id}/cash
查詢現金部位

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "portfolioId": "pf_001",
    "cashBalance": 150000,
    "cashWeight": 0.12,
    "availableCash": 145000,
    "pendingSettlement": 5000,
    "recentTransactions": [
      {
        "transactionId": "ct_001",
        "date": "2026-01-15",
        "type": "TRADE_SETTLEMENT",
        "amount": -342987,
        "balance": 150000,
        "reference": "tr_20260115_001",
        "description": "買進 2330 x 500"
      },
      {
        "transactionId": "ct_002",
        "date": "2026-01-10",
        "type": "DIVIDEND",
        "amount": 5250,
        "balance": 493000,
        "reference": "div_20260110_001",
        "description": "2330 現金股利"
      }
    ],
    "summary": {
      "totalDeposits": 1200000,
      "totalWithdrawals": 100000,
      "totalDividends": 35000,
      "totalFees": 12500,
      "totalTax": 4500
    }
  },
  "timestamp": "2026-01-15T10:00:00+08:00"
}
```

---

### 22. POST /portfolios/{id}/cash/transactions
記錄現金異動

**Request Body:**
```json
{
  "date": "2026-01-15",
  "type": "DEPOSIT",
  "amount": 500000,
  "description": "追加投資",
  "reference": "bank_transfer_001"
}
```

**交易類型:**
| 類型 | 說明 |
|------|------|
| DEPOSIT | 存入資金 |
| WITHDRAWAL | 提領資金 |
| DIVIDEND | 現金股利 |
| INTEREST | 利息收入 |
| FEE | 管理費用 |
| TAX | 稅金 |
| ADJUSTMENT | 調整 |

**Response (201 Created):**
```json
{
  "code": 201,
  "message": "現金異動記錄成功",
  "data": {
    "transactionId": "ct_003",
    "date": "2026-01-15",
    "type": "DEPOSIT",
    "amount": 500000,
    "balanceBefore": 150000,
    "balanceAfter": 650000,
    "description": "追加投資"
  },
  "timestamp": "2026-01-15T10:00:00+08:00"
}
```

---

### 23. GET /portfolios/{id}/dividends
查詢股利記錄

**Query Parameters:**
| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| year | Integer | 否 | 年度篩選 |
| stockId | String | 否 | 股票篩選 |
| type | String | 否 | 類型 (CASH/STOCK) |

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "portfolioId": "pf_001",
    "dividends": [
      {
        "dividendId": "div_001",
        "stockId": "2330",
        "stockName": "台積電",
        "exDate": "2025-09-15",
        "payDate": "2025-10-15",
        "recordDate": "2025-09-15",
        "type": "CASH",
        "sharesHeld": 500,
        "dividendPerShare": 3.50,
        "grossAmount": 1750,
        "taxWithheld": 0,
        "netAmount": 1750,
        "reinvested": false,
        "status": "PAID"
      },
      {
        "dividendId": "div_002",
        "stockId": "2317",
        "stockName": "鴻海",
        "exDate": "2025-08-20",
        "payDate": "2025-09-20",
        "recordDate": "2025-08-20",
        "type": "STOCK",
        "sharesHeld": 2000,
        "dividendPerShare": 0.5,
        "sharesReceived": 1000,
        "status": "RECEIVED"
      }
    ],
    "summary": {
      "totalCashDividends": 35000,
      "totalStockDividends": 1500,
      "reinvestedAmount": 12000,
      "dividendYield": 0.028,
      "byYear": {
        "2025": {
          "cashDividends": 35000,
          "stockDividends": 1500
        }
      }
    },
    "upcoming": [
      {
        "stockId": "2330",
        "stockName": "台積電",
        "exDate": "2026-03-15",
        "estimatedDividend": 3.50,
        "sharesHeld": 1500,
        "estimatedAmount": 5250
      }
    ]
  },
  "timestamp": "2026-01-15T10:00:00+08:00"
}
```

---

### 24. POST /portfolios/{id}/dividends/reinvest
股利再投資

**Request Body:**
```json
{
  "dividendId": "div_001",
  "reinvestAmount": 1750,
  "targetStockId": "2330",
  "priceLimit": 690.00
}
```

**Response (201 Created):**
```json
{
  "code": 201,
  "message": "股利再投資訂單已建立",
  "data": {
    "reinvestId": "ri_001",
    "dividendId": "div_001",
    "amount": 1750,
    "targetStockId": "2330",
    "estimatedShares": 2,
    "status": "PENDING",
    "createdAt": "2026-01-15T10:00:00+08:00"
  },
  "timestamp": "2026-01-15T10:00:00+08:00"
}
```

---

### 25. POST /portfolios/{id}/reports
產生投組報告

**Request Body:**
```json
{
  "reportType": "MONTHLY",
  "period": {
    "startDate": "2025-12-01",
    "endDate": "2025-12-31"
  },
  "sections": [
    "SUMMARY",
    "PERFORMANCE",
    "HOLDINGS",
    "TRANSACTIONS",
    "ATTRIBUTION",
    "RISK"
  ],
  "format": "PDF",
  "language": "zh-TW"
}
```

**報告類型:**
| 類型 | 說明 |
|------|------|
| MONTHLY | 月報 |
| QUARTERLY | 季報 |
| ANNUAL | 年報 |
| CUSTOM | 自訂期間 |
| PERFORMANCE | 績效專題報告 |
| TAX | 稅務報告 |

**Response (202 Accepted):**
```json
{
  "code": 202,
  "message": "報告產生中",
  "data": {
    "reportId": "rpt_20260115_001",
    "reportType": "MONTHLY",
    "status": "GENERATING",
    "estimatedTime": 30,
    "createdAt": "2026-01-15T10:00:00+08:00"
  },
  "timestamp": "2026-01-15T10:00:00+08:00"
}
```

---

### 26. GET /portfolios/{id}/reports/{reportId}
下載投組報告

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "reportId": "rpt_20260115_001",
    "reportType": "MONTHLY",
    "period": "2025-12",
    "status": "COMPLETED",
    "format": "PDF",
    "fileSize": 2048576,
    "downloadUrl": "/api/v1/portfolios/pf_001/reports/rpt_20260115_001/download",
    "expiresAt": "2026-01-22T10:00:00+08:00",
    "generatedAt": "2026-01-15T10:00:30+08:00"
  },
  "timestamp": "2026-01-15T10:01:00+08:00"
}
```

---

### 27. GET /benchmarks
查詢可用基準

**Query Parameters:**
| 參數 | 類型 | 必填 | 說明 |
|------|------|------|------|
| type | String | 否 | 類型 (ETF/INDEX/CUSTOM) |
| keyword | String | 否 | 關鍵字搜尋 |

**Response (200 OK):**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "benchmarks": [
      {
        "id": "0050",
        "name": "元大台灣50",
        "type": "ETF",
        "description": "追蹤臺灣50指數",
        "inception": "2003-06-30",
        "ytdReturn": 0.0185
      },
      {
        "id": "0051",
        "name": "元大中型100",
        "type": "ETF",
        "description": "追蹤臺灣中型100指數",
        "inception": "2006-08-31",
        "ytdReturn": 0.0125
      },
      {
        "id": "TAIEX",
        "name": "加權股價指數",
        "type": "INDEX",
        "description": "臺灣證券交易所發行量加權股價指數",
        "inception": "1966-01-05",
        "ytdReturn": 0.0165
      }
    ]
  },
  "timestamp": "2026-01-15T10:00:00+08:00"
}
```

---

## 錯誤碼定義

| 錯誤碼 | HTTP 狀態 | 說明 |
|--------|-----------|------|
| M18-001 | 400 | 投資組合名稱重複 |
| M18-002 | 400 | 目標權重總和必須為 1 |
| M18-003 | 400 | 交易數量必須大於 0 |
| M18-004 | 400 | 現金餘額不足 |
| M18-005 | 400 | 持倉數量不足 |
| M18-006 | 404 | 投資組合不存在 |
| M18-007 | 404 | 持倉不存在 |
| M18-008 | 404 | 交易記錄不存在 |
| M18-009 | 409 | 投資組合已關閉 |
| M18-010 | 422 | 股票代碼無效 |
| M18-011 | 422 | 日期範圍無效 |
| M18-012 | 429 | 報告產生請求過於頻繁 |

---

## 附錄

### A. 績效計算公式

**時間加權報酬率 (TWR):**
```
TWR = (1 + r₁) × (1 + r₂) × ... × (1 + rₙ) - 1
其中 rᵢ = (MVᵢ - MVᵢ₋₁ - CFᵢ) / (MVᵢ₋₁ + CFᵢ)
```

**金額加權報酬率 (MWR):**
```
Σ CFᵢ / (1 + MWR)^tᵢ = MV_end / (1 + MWR)^T
使用牛頓法求解 MWR
```

### B. 移動加權平均成本計算

```
新均價 = (原持股數 × 原均價 + 新買入股數 × 新買入價) / (原持股數 + 新買入股數)
```

### C. Brinson 績效歸因

```
配置效果 = Σ (wp,i - wb,i) × (rb,i - rb)
選股效果 = Σ wb,i × (rp,i - rb,i)
交互效果 = Σ (wp,i - wb,i) × (rp,i - rb,i)
```
