# M15-警報通知系統 API 規格

> **文件編號**: API-M15
> **模組名稱**: 警報通知系統 (Alert Notification System)
> **版本**: v1.0
> **最後更新**: 2026-01-15
> **狀態**: Draft

---

## 📋 API 總覽

### 基礎資訊

| 項目 | 說明 |
|-----|------|
| Base URL | `/api/v1/alerts` |
| 認證方式 | Bearer Token (JWT) |
| Content-Type | `application/json` |
| 回應格式 | 統一 JSON 格式 |

### API 清單

| # | 端點 | 方法 | 說明 | 優先級 |
|---|------|------|------|:------:|
| 1 | `/rules` | POST | 建立警報規則 | P0 |
| 2 | `/rules` | GET | 取得規則列表 | P0 |
| 3 | `/rules/{ruleId}` | GET | 取得單一規則 | P0 |
| 4 | `/rules/{ruleId}` | PUT | 更新警報規則 | P0 |
| 5 | `/rules/{ruleId}` | DELETE | 刪除警報規則 | P0 |
| 6 | `/rules/{ruleId}/toggle` | POST | 啟用/停用規則 | P0 |
| 7 | `/history` | GET | 取得警報歷史 | P1 |
| 8 | `/history/{alertId}` | GET | 取得單一警報詳情 | P1 |
| 9 | `/history/{alertId}/read` | POST | 標記已讀 | P1 |
| 10 | `/history/read-all` | POST | 全部標記已讀 | P1 |
| 11 | `/settings` | GET | 取得通知設定 | P1 |
| 12 | `/settings` | PUT | 更新通知設定 | P1 |
| 13 | `/settings/mute` | PUT | 更新靜音設定 | P1 |
| 14 | `/settings/channels/line` | POST | 綁定 Line Notify | P1 |
| 15 | `/settings/channels/line` | DELETE | 解除 Line 綁定 | P1 |
| 16 | `/settings/channels/fcm` | POST | 註冊 FCM Token | P1 |
| 17 | `/templates` | GET | 取得通知範本 | P2 |
| 18 | `/statistics` | GET | 取得警報統計 | P2 |

---

## 1. 警報規則管理

### 1.1 建立警報規則

建立新的警報規則。

**端點**: `POST /api/v1/alerts/rules`

**請求主體**:
```json
{
  "ruleName": "高評級買入信號",
  "ruleType": "SIGNAL",
  "conditions": {
    "stockIds": ["2330", "2317"],
    "directions": ["BUY"],
    "minGrade": "B+",
    "minScore": 70
  },
  "notifications": {
    "channels": ["EMAIL", "LINE"],
    "priority": "HIGH"
  },
  "throttle": {
    "maxPerDay": 10,
    "cooldownMinutes": 30
  }
}
```

| 欄位 | 類型 | 必填 | 說明 |
|-----|------|:----:|------|
| ruleName | string | Y | 規則名稱（最多 50 字） |
| ruleType | string | Y | 規則類型：SIGNAL, PRICE, CHANGE, VOLUME, WATCHLIST |
| conditions | object | Y | 觸發條件 |
| conditions.stockIds | string[] | N | 指定股票（空=全市場） |
| conditions.directions | string[] | N | 信號方向：BUY, SELL, HOLD |
| conditions.minGrade | string | N | 最低評級：A+, A, B+, B, C, D |
| conditions.minScore | number | N | 最低分數（0-100） |
| notifications | object | Y | 通知設定 |
| notifications.channels | string[] | Y | 通知管道：EMAIL, LINE, PUSH |
| notifications.priority | string | N | 優先級：HIGH, NORMAL, LOW（預設 NORMAL） |
| throttle | object | N | 節流設定 |
| throttle.maxPerDay | number | N | 每日最多通知（預設 20） |
| throttle.cooldownMinutes | number | N | 同股票冷卻時間（預設 60） |

**成功回應** (201 Created):
```json
{
  "code": 201,
  "message": "規則建立成功",
  "data": {
    "ruleId": "RULE_20260115_001",
    "ruleName": "高評級買入信號",
    "ruleType": "SIGNAL",
    "enabled": true,
    "conditions": {
      "stockIds": ["2330", "2317"],
      "directions": ["BUY"],
      "minGrade": "B+",
      "minScore": 70
    },
    "notifications": {
      "channels": ["EMAIL", "LINE"],
      "priority": "HIGH"
    },
    "throttle": {
      "maxPerDay": 10,
      "cooldownMinutes": 30
    },
    "createdAt": "2026-01-15T10:30:00+08:00"
  },
  "timestamp": "2026-01-15T10:30:00+08:00"
}
```

**錯誤回應**:
| HTTP 狀態碼 | 錯誤碼 | 說明 |
|------------|-------|------|
| 400 | M15-001 | 規則格式無效 |
| 400 | M15-002 | 超過規則數量限制（最多 20 條） |
| 400 | M15-004 | 通知管道未設定 |

---

### 1.2 取得規則列表

取得用戶的所有警報規則。

**端點**: `GET /api/v1/alerts/rules`

**查詢參數**:
| 參數 | 類型 | 必填 | 說明 |
|-----|------|:----:|------|
| ruleType | string | N | 篩選規則類型 |
| enabled | boolean | N | 篩選啟用狀態 |
| page | number | N | 頁碼（預設 1） |
| size | number | N | 每頁筆數（預設 20） |

**成功回應** (200 OK):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "rules": [
      {
        "ruleId": "RULE_20260115_001",
        "ruleName": "高評級買入信號",
        "ruleType": "SIGNAL",
        "enabled": true,
        "conditionSummary": "股票: 2330, 2317 | 方向: BUY | 評級 >= B+",
        "channels": ["EMAIL", "LINE"],
        "triggeredCount": 15,
        "lastTriggeredAt": "2026-01-14T14:30:00+08:00",
        "createdAt": "2026-01-10T09:00:00+08:00"
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "totalItems": 5,
      "totalPages": 1
    }
  },
  "timestamp": "2026-01-15T10:30:00+08:00"
}
```

---

### 1.3 取得單一規則

取得指定規則的詳細內容。

**端點**: `GET /api/v1/alerts/rules/{ruleId}`

**路徑參數**:
| 參數 | 類型 | 說明 |
|-----|------|------|
| ruleId | string | 規則 ID |

**成功回應** (200 OK):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "ruleId": "RULE_20260115_001",
    "ruleName": "高評級買入信號",
    "ruleType": "SIGNAL",
    "enabled": true,
    "conditions": {
      "stockIds": ["2330", "2317"],
      "stockNames": ["台積電", "鴻海"],
      "directions": ["BUY"],
      "minGrade": "B+",
      "minScore": 70
    },
    "notifications": {
      "channels": ["EMAIL", "LINE"],
      "priority": "HIGH"
    },
    "throttle": {
      "maxPerDay": 10,
      "cooldownMinutes": 30
    },
    "statistics": {
      "triggeredCount": 15,
      "lastTriggeredAt": "2026-01-14T14:30:00+08:00",
      "todayCount": 2
    },
    "createdAt": "2026-01-10T09:00:00+08:00",
    "updatedAt": "2026-01-12T15:00:00+08:00"
  },
  "timestamp": "2026-01-15T10:30:00+08:00"
}
```

---

### 1.4 更新警報規則

更新指定的警報規則。

**端點**: `PUT /api/v1/alerts/rules/{ruleId}`

**路徑參數**:
| 參數 | 類型 | 說明 |
|-----|------|------|
| ruleId | string | 規則 ID |

**請求主體**:
```json
{
  "ruleName": "高評級買入信號（更新）",
  "conditions": {
    "stockIds": ["2330", "2317", "2454"],
    "directions": ["BUY"],
    "minGrade": "A",
    "minScore": 80
  },
  "notifications": {
    "channels": ["EMAIL", "LINE", "PUSH"],
    "priority": "HIGH"
  },
  "throttle": {
    "maxPerDay": 15,
    "cooldownMinutes": 20
  }
}
```

**成功回應** (200 OK):
```json
{
  "code": 200,
  "message": "規則更新成功",
  "data": {
    "ruleId": "RULE_20260115_001",
    "ruleName": "高評級買入信號（更新）",
    "updatedAt": "2026-01-15T11:00:00+08:00"
  },
  "timestamp": "2026-01-15T11:00:00+08:00"
}
```

---

### 1.5 刪除警報規則

刪除指定的警報規則。

**端點**: `DELETE /api/v1/alerts/rules/{ruleId}`

**成功回應** (204 No Content)

---

### 1.6 啟用/停用規則

切換規則的啟用狀態。

**端點**: `POST /api/v1/alerts/rules/{ruleId}/toggle`

**請求主體**:
```json
{
  "enabled": false
}
```

**成功回應** (200 OK):
```json
{
  "code": 200,
  "message": "規則已停用",
  "data": {
    "ruleId": "RULE_20260115_001",
    "enabled": false
  },
  "timestamp": "2026-01-15T11:00:00+08:00"
}
```

---

## 2. 警報歷史

### 2.1 取得警報歷史

查詢警報歷史記錄。

**端點**: `GET /api/v1/alerts/history`

**查詢參數**:
| 參數 | 類型 | 必填 | 說明 |
|-----|------|:----:|------|
| startDate | string | N | 開始日期（YYYY-MM-DD） |
| endDate | string | N | 結束日期（YYYY-MM-DD） |
| ruleId | string | N | 篩選規則 ID |
| stockId | string | N | 篩選股票代碼 |
| alertType | string | N | 篩選警報類型 |
| isRead | boolean | N | 篩選已讀/未讀 |
| page | number | N | 頁碼（預設 1） |
| size | number | N | 每頁筆數（預設 20） |

**成功回應** (200 OK):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "alerts": [
      {
        "alertId": "ALERT_20260115_001",
        "ruleId": "RULE_20260115_001",
        "ruleName": "高評級買入信號",
        "alertType": "SIGNAL",
        "stockId": "2330",
        "stockName": "台積電",
        "signal": {
          "direction": "BUY",
          "grade": "A",
          "score": 85
        },
        "price": {
          "current": 580.00,
          "change": 12.00,
          "changePercent": 2.11
        },
        "triggeredAt": "2026-01-15T14:30:00+08:00",
        "notifiedChannels": ["EMAIL", "LINE"],
        "isRead": false
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "totalItems": 156,
      "totalPages": 8
    },
    "summary": {
      "totalAlerts": 156,
      "unreadCount": 12,
      "todayCount": 5
    }
  },
  "timestamp": "2026-01-15T15:00:00+08:00"
}
```

---

### 2.2 取得單一警報詳情

取得指定警報的詳細資訊。

**端點**: `GET /api/v1/alerts/history/{alertId}`

**成功回應** (200 OK):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "alertId": "ALERT_20260115_001",
    "ruleId": "RULE_20260115_001",
    "ruleName": "高評級買入信號",
    "alertType": "SIGNAL",
    "stock": {
      "stockId": "2330",
      "stockName": "台積電",
      "market": "TWSE",
      "sector": "半導體業"
    },
    "signal": {
      "signalId": "SIG_20260115_2330",
      "direction": "BUY",
      "grade": "A",
      "score": 85,
      "summary": "技術面: RSI 動能轉強 | 籌碼面: 外資連買 5 日"
    },
    "price": {
      "current": 580.00,
      "open": 575.00,
      "high": 582.00,
      "low": 573.00,
      "change": 12.00,
      "changePercent": 2.11,
      "volume": 25000
    },
    "triggeredAt": "2026-01-15T14:30:00+08:00",
    "notifications": [
      {
        "channel": "EMAIL",
        "sentAt": "2026-01-15T14:30:05+08:00",
        "status": "SENT"
      },
      {
        "channel": "LINE",
        "sentAt": "2026-01-15T14:30:08+08:00",
        "status": "SENT"
      }
    ],
    "isRead": false,
    "readAt": null
  },
  "timestamp": "2026-01-15T15:00:00+08:00"
}
```

---

### 2.3 標記已讀

標記單一警報為已讀。

**端點**: `POST /api/v1/alerts/history/{alertId}/read`

**成功回應** (200 OK):
```json
{
  "code": 200,
  "message": "已標記為已讀",
  "data": {
    "alertId": "ALERT_20260115_001",
    "readAt": "2026-01-15T15:30:00+08:00"
  },
  "timestamp": "2026-01-15T15:30:00+08:00"
}
```

---

### 2.4 全部標記已讀

將所有未讀警報標記為已讀。

**端點**: `POST /api/v1/alerts/history/read-all`

**請求主體**（可選）:
```json
{
  "beforeDate": "2026-01-15"
}
```

**成功回應** (200 OK):
```json
{
  "code": 200,
  "message": "已將 12 則警報標記為已讀",
  "data": {
    "markedCount": 12
  },
  "timestamp": "2026-01-15T15:30:00+08:00"
}
```

---

## 3. 通知設定

### 3.1 取得通知設定

取得用戶的通知偏好設定。

**端點**: `GET /api/v1/alerts/settings`

**成功回應** (200 OK):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "userId": "user001",
    "email": {
      "enabled": true,
      "address": "user@example.com",
      "verified": true
    },
    "line": {
      "enabled": true,
      "connected": true,
      "displayName": "User Name"
    },
    "push": {
      "enabled": true,
      "deviceCount": 2
    },
    "mute": {
      "enabled": true,
      "quietHours": {
        "start": "22:00",
        "end": "08:00"
      },
      "weekendMute": true,
      "dailyLimit": {
        "email": 20,
        "line": 10,
        "push": 30
      }
    },
    "batch": {
      "enabled": true,
      "windowMinutes": 5,
      "threshold": 3
    }
  },
  "timestamp": "2026-01-15T10:30:00+08:00"
}
```

---

### 3.2 更新通知設定

更新用戶的通知偏好。

**端點**: `PUT /api/v1/alerts/settings`

**請求主體**:
```json
{
  "email": {
    "enabled": true,
    "address": "new.email@example.com"
  },
  "push": {
    "enabled": false
  },
  "batch": {
    "enabled": true,
    "windowMinutes": 10,
    "threshold": 5
  }
}
```

**成功回應** (200 OK):
```json
{
  "code": 200,
  "message": "通知設定已更新",
  "data": {
    "updatedAt": "2026-01-15T11:00:00+08:00"
  },
  "timestamp": "2026-01-15T11:00:00+08:00"
}
```

---

### 3.3 更新靜音設定

更新靜音/勿擾設定。

**端點**: `PUT /api/v1/alerts/settings/mute`

**請求主體**:
```json
{
  "enabled": true,
  "quietHours": {
    "start": "23:00",
    "end": "07:00"
  },
  "weekendMute": true,
  "dailyLimit": {
    "email": 30,
    "line": 15,
    "push": 50
  }
}
```

**成功回應** (200 OK):
```json
{
  "code": 200,
  "message": "靜音設定已更新",
  "data": {
    "updatedAt": "2026-01-15T11:00:00+08:00"
  },
  "timestamp": "2026-01-15T11:00:00+08:00"
}
```

---

### 3.4 綁定 Line Notify

綁定 Line Notify 服務。

**端點**: `POST /api/v1/alerts/settings/channels/line`

**請求主體**:
```json
{
  "code": "OAUTH_CODE_FROM_LINE"
}
```

**成功回應** (200 OK):
```json
{
  "code": 200,
  "message": "Line Notify 綁定成功",
  "data": {
    "displayName": "User Name",
    "connectedAt": "2026-01-15T11:00:00+08:00"
  },
  "timestamp": "2026-01-15T11:00:00+08:00"
}
```

**錯誤回應**:
| HTTP 狀態碼 | 錯誤碼 | 說明 |
|------------|-------|------|
| 400 | M15-005 | Line Token 無效或已過期 |

---

### 3.5 解除 Line 綁定

解除 Line Notify 綁定。

**端點**: `DELETE /api/v1/alerts/settings/channels/line`

**成功回應** (204 No Content)

---

### 3.6 註冊 FCM Token

註冊 Firebase Cloud Messaging Token（APP 推播）。

**端點**: `POST /api/v1/alerts/settings/channels/fcm`

**請求主體**:
```json
{
  "token": "FCM_DEVICE_TOKEN",
  "deviceId": "DEVICE_UUID",
  "platform": "iOS"
}
```

| 欄位 | 類型 | 必填 | 說明 |
|-----|------|:----:|------|
| token | string | Y | FCM Token |
| deviceId | string | Y | 裝置唯一識別碼 |
| platform | string | Y | 平台：iOS, Android |

**成功回應** (200 OK):
```json
{
  "code": 200,
  "message": "裝置註冊成功",
  "data": {
    "deviceId": "DEVICE_UUID",
    "registeredAt": "2026-01-15T11:00:00+08:00"
  },
  "timestamp": "2026-01-15T11:00:00+08:00"
}
```

---

## 4. 通知範本與統計

### 4.1 取得通知範本

取得可用的通知範本列表。

**端點**: `GET /api/v1/alerts/templates`

**成功回應** (200 OK):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "templates": [
      {
        "templateId": "TPL_SIGNAL_DEFAULT",
        "templateName": "預設信號通知",
        "templateType": "SIGNAL",
        "preview": "📈 {{stockName}} - {{direction}} ({{grade}})",
        "isDefault": true
      },
      {
        "templateId": "TPL_SIGNAL_DETAIL",
        "templateName": "詳細信號通知",
        "templateType": "SIGNAL",
        "preview": "📈 交易信號: {{stockId}} {{stockName}}\n方向: {{direction}}\n評級: {{grade}} ({{score}}分)\n現價: {{price}}",
        "isDefault": false
      }
    ]
  },
  "timestamp": "2026-01-15T10:30:00+08:00"
}
```

---

### 4.2 取得警報統計

取得用戶的警報統計資訊。

**端點**: `GET /api/v1/alerts/statistics`

**查詢參數**:
| 參數 | 類型 | 必填 | 說明 |
|-----|------|:----:|------|
| period | string | N | 統計期間：7d, 30d, 90d（預設 30d） |

**成功回應** (200 OK):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "period": "30d",
    "summary": {
      "totalAlerts": 156,
      "totalNotifications": 312,
      "averagePerDay": 5.2
    },
    "byType": {
      "SIGNAL": 120,
      "PRICE": 25,
      "CHANGE": 8,
      "VOLUME": 3
    },
    "byChannel": {
      "EMAIL": 156,
      "LINE": 120,
      "PUSH": 36
    },
    "byRule": [
      {
        "ruleId": "RULE_001",
        "ruleName": "高評級買入信號",
        "triggeredCount": 45
      },
      {
        "ruleId": "RULE_002",
        "ruleName": "自選股信號",
        "triggeredCount": 38
      }
    ],
    "topStocks": [
      {
        "stockId": "2330",
        "stockName": "台積電",
        "alertCount": 12
      },
      {
        "stockId": "2317",
        "stockName": "鴻海",
        "alertCount": 8
      }
    ],
    "trend": [
      { "date": "2026-01-01", "count": 4 },
      { "date": "2026-01-02", "count": 6 },
      { "date": "2026-01-03", "count": 3 }
    ]
  },
  "timestamp": "2026-01-15T10:30:00+08:00"
}
```

---

## 5. 共用錯誤回應

### 錯誤回應格式

```json
{
  "code": 400,
  "message": "規則格式無效",
  "error": {
    "errorCode": "M15-001",
    "details": "conditions.minGrade 必須是有效的評級值"
  },
  "timestamp": "2026-01-15T10:30:00+08:00"
}
```

### 錯誤碼對照表

| 錯誤碼 | HTTP 狀態碼 | 說明 |
|-------|------------|------|
| M15-001 | 400 | 規則格式無效 |
| M15-002 | 400 | 超過規則數量限制 |
| M15-003 | 404 | 規則不存在 |
| M15-004 | 400 | 通知管道未設定 |
| M15-005 | 400 | Line Token 無效 |
| M15-006 | 500 | Email 發送失敗 |
| M15-007 | 500 | Line 發送失敗 |
| M15-008 | 500 | Push 發送失敗 |
| M15-009 | 429 | 超過每日通知限額 |
| M15-010 | 400 | 靜音設定無效 |

---

## 6. 相關文檔

- [M15 功能需求](../functional/M15-警報通知系統功能需求.md)
- [M15 資料庫設計](../../design/M15-資料庫設計.md)
- [M15 業務流程](../../design/M15-業務流程.md)

---

**文件維護者**: 後端工程師
**最後更新**: 2026-01-15
**下次審核**: 2026-04-15
