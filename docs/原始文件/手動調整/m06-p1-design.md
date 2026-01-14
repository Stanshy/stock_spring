# M06 P1 功能設計文件

> **文件編號**: DESIGN-M06-P1
> **模組名稱**: 資料管理模組 - P1 功能擴充
> **版本**: v1.0
> **最後更新**: 2026-01-10
> **狀態**: Draft

---

## 1. 整體目標與範圍

### 1.1 P1 功能定位

M06 P1 功能是資料管理模組的**第二階段擴充**，在 P0 基礎功能（股票清單、股價同步、交易日曆）之上，新增：

- **籌碼資料同步**：三大法人買賣超、融資融券餘額
- **財報資料同步**：季報/年報財務數據
- **資料品質檢核執行**：自動化品質驗證
- **資料補齊機制**：歷史資料缺漏修補

### 1.2 目標

| 目標 | 說明 |
|------|------|
| 籌碼資料完整性 | 每日同步三大法人、融資融券資料，支援 M09 籌碼分析 |
| 財報資料可用性 | 每季同步財務報表，支援 M08 基本面分析 |
| 資料品質自動化 | 定期執行品質檢核，主動發現異常 |
| 歷史資料補齊 | 提供缺漏資料的自動/手動補齊能力 |

### 1.3 功能範圍

**P1 包含功能**（依功能需求文件）：

| 功能編號 | 功能名稱 | 優先級 | 說明 |
|---------|---------|-------|------|
| F-M06-003 | 財報資料同步 | P1 | 每季同步財務報表資料 |
| F-M06-004 | 籌碼資料同步 | P1 | 每日同步三大法人、融資融券資料 |
| F-M06-009 | 資料補齊機制 | P1 | 補齊缺失的歷史資料 |
| F-M06-006 | 資料品質檢核（執行） | P1 | 實作品質規則執行引擎 |

**P1 不包含功能**：

- P2 資料源備援機制（F-M06-010）
- 即時報價串流
- 資料歸檔與清理

### 1.4 與現有 P0 功能的關係

```
┌─────────────────────────────────────────────────────────┐
│                    M06 資料管理模組                       │
├─────────────────────────────────────────────────────────┤
│  P0 (已實作)                                             │
│  ├── F-M06-001 股票清單管理                              │
│  ├── F-M06-002 股價資料同步                              │
│  ├── F-M06-005 交易日曆管理                              │
│  ├── F-M06-007 資料查詢 API                              │
│  └── F-M06-008 資料更新排程                              │
├─────────────────────────────────────────────────────────┤
│  P1 (待實作) ← 本文件範圍                                │
│  ├── F-M06-003 財報資料同步                              │
│  ├── F-M06-004 籌碼資料同步                              │
│  ├── F-M06-006 資料品質檢核（執行引擎）                   │
│  └── F-M06-009 資料補齊機制                              │
├─────────────────────────────────────────────────────────┤
│  P2 (未來)                                               │
│  └── F-M06-010 資料源備援                                │
└─────────────────────────────────────────────────────────┘
```

---

## 2. 子模組職責說明

### 2.1 Institutional（法人籌碼）子模組

**職責**：
- 從證交所/櫃買中心取得三大法人（外資、投信、自營商）每日買賣超資料
- 資料清洗與格式轉換
- 批次寫入 `institutional_trading` 資料表
- 提供法人買賣超查詢 API

**資料來源**：
- 台灣證券交易所每日三大法人買賣超資訊
- 櫃檯買賣中心外資及陸資買賣超彙總

**核心實體**：
- `InstitutionalTrading`：法人每日買賣超紀錄

### 2.2 Margin（融資融券）子模組

**職責**：
- 從證交所取得每日融資融券餘額資料
- 計算融資/融券增減、券資比等衍生欄位
- 批次寫入 `margin_trading` 資料表
- 提供融資融券查詢 API

**資料來源**：
- 台灣證券交易所融資融券餘額表

**核心實體**：
- `MarginTrading`：融資融券每日餘額紀錄

### 2.3 Financial（財報）子模組

**職責**：
- 每季從公開資訊觀測站取得財務報表
- 解析損益表、資產負債表、現金流量表
- 計算基礎財務比率（ROE、ROA、負債比等）
- 批次寫入 `financial_statements` 資料表
- 支援 JSONB 欄位儲存完整報表資料

**資料來源**：
- 公開資訊觀測站 XBRL 財報
- FinMind 財報 API（備援）

**核心實體**：
- `FinancialStatement`：季度/年度財務報表（已存在，需擴充）

### 2.4 Quality（資料品質）子模組

**職責**：
- 執行預定義的資料品質檢核規則
- 支援多種檢核類型：完整性、一致性、時效性、準確性
- 記錄檢核結果與發現的問題
- 計算資料品質分數
- 提供品質報告與趨勢分析

**檢核類型**：

| 檢核類型 | 說明 | 範例 |
|---------|------|------|
| COMPLETENESS | 資料完整性 | 交易日是否都有股價資料 |
| CONSISTENCY | 資料一致性 | 開盤價 ≤ 最高價 |
| TIMELINESS | 資料時效性 | 最新資料是否為最近交易日 |
| ACCURACY | 資料準確性 | 漲跌幅是否計算正確 |

**核心實體**：
- `DataQualityCheck`：檢核規則定義（已存在）
- `DataQualityIssue`：檢核問題紀錄（已存在）

### 2.5 DataRepair（資料補齊）子模組

**職責**：
- 偵測資料缺漏（依交易日曆比對）
- 支援指定日期範圍的批次補齊
- 支援單一股票或全市場補齊
- 記錄補齊執行結果

**補齊策略**：

| 策略 | 說明 |
|------|------|
| AUTO_DETECT | 自動偵測缺漏日期並補齊 |
| DATE_RANGE | 指定日期範圍強制重新同步 |
| SINGLE_STOCK | 針對單一股票補齊 |
| FULL_MARKET | 全市場資料重新同步 |

### 2.6 Job（排程任務）子模組

**職責**：
- 管理 P1 功能相關的排程任務
- 提供手動觸發介面
- 追蹤任務執行狀態與結果
- 支援任務重試與錯誤處理

**P1 新增 Job**：

| Job 名稱 | 排程時間 | 說明 |
|---------|---------|------|
| SYNC_INSTITUTIONAL | 每交易日 16:30 | 同步三大法人買賣超 |
| SYNC_MARGIN | 每交易日 17:00 | 同步融資融券餘額 |
| SYNC_FINANCIAL | 每月 15 日 09:00 | 同步最新財報 |
| DATA_QUALITY_CHECK | 每日 06:00 | 執行資料品質檢核 |
| DATA_REPAIR | 手動觸發 | 資料補齊任務 |

---

## 3. Package 與 Class 結構

### 3.1 整體 Package 結構

```
com.chris.fin_shark.m06/
├── config/
│   └── SchedulerConfig.java              # (existing)
│
├── controller/
│   ├── StockManagementController.java    # (existing)
│   ├── JobManagementController.java      # (existing, 擴充)
│   ├── MarketDataQueryController.java    # (existing, 擴充)
│   ├── DataQualityController.java        # (existing, 擴充)
│   ├── TradingCalendarController.java    # (existing)
│   ├── InstitutionalTradingController.java   # [NEW]
│   ├── MarginTradingController.java          # [NEW]
│   ├── FinancialStatementController.java     # [NEW]
│   └── DataRepairController.java             # [NEW]
│
├── service/
│   ├── StockService.java                 # (existing)
│   ├── StockPriceSyncService.java        # (existing)
│   ├── JobManagementService.java         # (existing, 擴充)
│   ├── DataQualityService.java           # (existing, 擴充)
│   ├── MarketDataQueryService.java       # (existing, 擴充)
│   ├── TradingCalendarService.java       # (existing)
│   ├── InstitutionalTradingSyncService.java  # [NEW]
│   ├── MarginTradingSyncService.java         # [NEW]
│   ├── FinancialStatementSyncService.java    # [NEW]
│   ├── DataQualityExecutionService.java      # [NEW]
│   └── DataRepairService.java                # [NEW]
│
├── repository/
│   ├── StockRepository.java              # (existing)
│   ├── StockPriceRepository.java         # (existing)
│   ├── TradingCalendarRepository.java    # (existing)
│   ├── JobExecutionRepository.java       # (existing)
│   ├── DataQualityCheckRepository.java   # (existing)
│   ├── DataQualityIssueRepository.java   # (existing)
│   ├── InstitutionalTradingRepository.java   # [NEW]
│   ├── MarginTradingRepository.java          # [NEW]
│   └── FinancialStatementRepository.java     # [NEW]
│
├── mapper/
│   ├── StockPriceMapper.java             # (existing)
│   ├── InstitutionalTradingMapper.java   # [NEW]
│   ├── MarginTradingMapper.java          # [NEW]
│   ├── FinancialStatementMapper.java     # [NEW]
│   └── DataQualityMapper.java            # [NEW]
│
├── domain/
│   ├── Stock.java                        # (existing)
│   ├── StockPrice.java                   # (existing)
│   ├── StockPriceId.java                 # (existing)
│   ├── TradingCalendar.java              # (existing)
│   ├── DataQualityCheck.java             # (existing)
│   ├── DataQualityIssue.java             # (existing)
│   ├── FinancialStatement.java           # (existing, 擴充)
│   ├── InstitutionalTrading.java         # [NEW]
│   ├── InstitutionalTradingId.java       # [NEW]
│   ├── MarginTrading.java                # [NEW]
│   └── MarginTradingId.java              # [NEW]
│
├── dto/
│   ├── request/
│   │   ├── StockCreateRequest.java       # (existing)
│   │   ├── StockUpdateRequest.java       # (existing)
│   │   ├── StockQueryRequest.java        # (existing)
│   │   ├── DataRepairRequest.java            # [NEW]
│   │   ├── QualityCheckExecuteRequest.java   # [NEW]
│   │   └── FinancialSyncRequest.java         # [NEW]
│   ├── StockDTO.java                     # (existing)
│   ├── StockPriceDTO.java                # (existing)
│   ├── TradingCalendarDTO.java           # (existing)
│   ├── DataQualityCheckDTO.java          # (existing)
│   ├── DataQualityIssueDTO.java          # (existing)
│   ├── DataQualitySummaryDTO.java        # (existing)
│   ├── InstitutionalTradingDTO.java      # (existing, 可能需擴充)
│   ├── MarginTradingDTO.java             # (existing, 可能需擴充)
│   ├── FinancialStatementDTO.java            # [NEW]
│   ├── DataRepairResultDTO.java              # [NEW]
│   └── QualityCheckResultDTO.java            # [NEW]
│
├── converter/
│   ├── StockConverter.java               # (existing)
│   ├── StockPriceConverter.java          # (existing)
│   ├── TradingCalendarConverter.java     # (existing)
│   ├── DataQualityCheckConverter.java    # (existing)
│   ├── DataQualityIssueConverter.java    # (existing)
│   ├── JobExecutionConverter.java        # (existing)
│   ├── InstitutionalTradingConverter.java    # [NEW]
│   ├── MarginTradingConverter.java           # [NEW]
│   └── FinancialStatementConverter.java      # [NEW]
│
├── vo/
│   ├── StockPriceStatisticsVO.java       # (existing)
│   ├── MissingDataVO.java                    # [NEW]
│   └── QualityCheckExecutionVO.java          # [NEW]
│
├── job/
│   ├── StockPriceSyncJob.java            # (existing)
│   ├── InstitutionalTradingSyncJob.java      # [NEW]
│   ├── MarginTradingSyncJob.java             # [NEW]
│   ├── FinancialStatementSyncJob.java        # [NEW]
│   └── DataQualityCheckJob.java              # [NEW]
│
├── exception/
│   ├── StockNotFoundException.java       # (existing)
│   ├── DataQualityException.java         # (existing)
│   ├── FeatureNotImplementedException.java # (existing)
│   ├── DataSyncException.java                # [NEW]
│   └── DataRepairException.java              # [NEW]
│
└── enums/
    ├── M06ErrorCode.java                 # (existing, 擴充)
    ├── RepairStrategy.java                   # [NEW]
    └── QualityCheckType.java                 # [NEW]
```

### 3.2 MyBatis Mapper XML

```
src/main/resources/mapper/
├── StockPriceMapper.xml                  # (existing)
├── InstitutionalTradingMapper.xml            # [NEW]
├── MarginTradingMapper.xml                   # [NEW]
├── FinancialStatementMapper.xml              # [NEW]
└── DataQualityMapper.xml                     # [NEW]
```

### 3.3 類別關係圖

```
┌─────────────────────────────────────────────────────────────────┐
│                        Controller Layer                          │
├─────────────────────────────────────────────────────────────────┤
│  InstitutionalTradingController                                  │
│  MarginTradingController                                         │
│  FinancialStatementController                                    │
│  DataRepairController                                            │
│  JobManagementController (擴充)                                   │
│  DataQualityController (擴充)                                     │
└─────────────────────────────────────────────────────────────────┘
                              ↓ 依賴
┌─────────────────────────────────────────────────────────────────┐
│                         Service Layer                            │
├─────────────────────────────────────────────────────────────────┤
│  InstitutionalTradingSyncService ←→ TwseApiClient               │
│  MarginTradingSyncService ←→ TwseApiClient                      │
│  FinancialStatementSyncService ←→ FinMindApiClient              │
│  DataQualityExecutionService ←→ DataQualityService              │
│  DataRepairService ←→ StockPriceSyncService                     │
│                     ←→ InstitutionalTradingSyncService          │
│                     ←→ MarginTradingSyncService                 │
└─────────────────────────────────────────────────────────────────┘
                              ↓ 依賴
┌─────────────────────────────────────────────────────────────────┐
│                       Repository Layer                           │
├─────────────────────────────────────────────────────────────────┤
│  InstitutionalTradingRepository (JPA)                           │
│  MarginTradingRepository (JPA)                                  │
│  FinancialStatementRepository (JPA)                             │
│  InstitutionalTradingMapper (MyBatis) - 批次操作                │
│  MarginTradingMapper (MyBatis) - 批次操作                       │
│  FinancialStatementMapper (MyBatis) - 批次操作                  │
│  DataQualityMapper (MyBatis) - 品質檢核 SQL                     │
└─────────────────────────────────────────────────────────────────┘
                              ↓ 依賴
┌─────────────────────────────────────────────────────────────────┐
│                         Domain Layer                             │
├─────────────────────────────────────────────────────────────────┤
│  InstitutionalTrading + InstitutionalTradingId                  │
│  MarginTrading + MarginTradingId                                │
│  FinancialStatement (擴充)                                       │
└─────────────────────────────────────────────────────────────────┘
```

---

## 4. 各層責任分工

### 4.1 Controller Layer

| 職責 | 說明 |
|------|------|
| 請求接收 | 接收 HTTP 請求，解析路徑參數與查詢參數 |
| 參數驗證 | 使用 `@Valid` 驗證請求 DTO |
| 回應封裝 | 統一使用 `ApiResponse<T>` 封裝回應 |
| 例外處理 | 由 `@ControllerAdvice` 統一處理 |

**不應包含**：業務邏輯、資料存取邏輯

### 4.2 Service Layer

| 職責 | 說明 |
|------|------|
| 業務邏輯 | 實作核心業務規則 |
| 交易管理 | 使用 `@Transactional` 管理交易邊界 |
| 外部 API 呼叫 | 呼叫 client 層取得外部資料 |
| 資料轉換 | 使用 Converter 進行 Entity ↔ DTO 轉換 |
| Job 記錄 | 建立/更新 JobExecution 紀錄 |

**不應包含**：HTTP 相關邏輯、直接 SQL

### 4.3 Repository Layer

| 職責 | 說明 |
|------|------|
| JPA Repository | 標準 CRUD 操作、簡單查詢 |
| MyBatis Mapper | 批次操作、複雜查詢、Window Functions |
| 資料存取抽象 | 隱藏底層資料庫細節 |

**JPA vs MyBatis 使用原則**：

| 場景 | 使用技術 |
|------|---------|
| 單筆 CRUD | JPA Repository |
| 簡單條件查詢 | JPA Query Methods |
| 批次 INSERT/UPDATE | MyBatis `batchInsert` |
| 複雜分析查詢 | MyBatis + Window Functions |
| UPSERT (ON CONFLICT) | MyBatis |

### 4.4 Job Layer

| 職責 | 說明 |
|------|------|
| 排程觸發 | 使用 `@Scheduled` 定義執行時間 |
| 冪等性保證 | 重複執行不產生副作用 |
| 錯誤處理 | 捕捉異常，記錄錯誤訊息 |
| 手動觸發 | 提供 public 方法供 Controller 呼叫 |

**Job 執行流程**：

```
1. 檢查是否為交易日（若適用）
2. 建立 JobExecution (status=RUNNING)
3. 呼叫對應 SyncService
4. 更新 JobExecution (status=SUCCESS/FAILED)
5. 記錄統計資訊 (successItems/failedItems)
```

---

## 5. 需要新增或修改的檔案清單

### 5.1 新增檔案

#### Domain Layer

| 檔案路徑 | 說明 |
|---------|------|
| `m06/domain/InstitutionalTrading.java` | 法人買賣超實體 |
| `m06/domain/InstitutionalTradingId.java` | 法人買賣超複合主鍵 |
| `m06/domain/MarginTrading.java` | 融資融券實體 |
| `m06/domain/MarginTradingId.java` | 融資融券複合主鍵 |

#### Repository Layer

| 檔案路徑 | 說明 |
|---------|------|
| `m06/repository/InstitutionalTradingRepository.java` | 法人買賣超 JPA Repository |
| `m06/repository/MarginTradingRepository.java` | 融資融券 JPA Repository |
| `m06/repository/FinancialStatementRepository.java` | 財報 JPA Repository |

#### MyBatis Mapper

| 檔案路徑 | 說明 |
|---------|------|
| `m06/mapper/InstitutionalTradingMapper.java` | 法人買賣超 Mapper Interface |
| `m06/mapper/MarginTradingMapper.java` | 融資融券 Mapper Interface |
| `m06/mapper/FinancialStatementMapper.java` | 財報 Mapper Interface |
| `m06/mapper/DataQualityMapper.java` | 品質檢核 Mapper Interface |
| `resources/mapper/InstitutionalTradingMapper.xml` | 法人買賣超 SQL |
| `resources/mapper/MarginTradingMapper.xml` | 融資融券 SQL |
| `resources/mapper/FinancialStatementMapper.xml` | 財報 SQL |
| `resources/mapper/DataQualityMapper.xml` | 品質檢核 SQL |

#### Service Layer

| 檔案路徑 | 說明 |
|---------|------|
| `m06/service/InstitutionalTradingSyncService.java` | 法人買賣超同步服務 |
| `m06/service/MarginTradingSyncService.java` | 融資融券同步服務 |
| `m06/service/FinancialStatementSyncService.java` | 財報同步服務 |
| `m06/service/DataQualityExecutionService.java` | 品質檢核執行服務 |
| `m06/service/DataRepairService.java` | 資料補齊服務 |

#### Controller Layer

| 檔案路徑 | 說明 |
|---------|------|
| `m06/controller/InstitutionalTradingController.java` | 法人買賣超 API |
| `m06/controller/MarginTradingController.java` | 融資融券 API |
| `m06/controller/FinancialStatementController.java` | 財報 API |
| `m06/controller/DataRepairController.java` | 資料補齊 API |

#### Job Layer

| 檔案路徑 | 說明 |
|---------|------|
| `m06/job/InstitutionalTradingSyncJob.java` | 法人買賣超同步 Job |
| `m06/job/MarginTradingSyncJob.java` | 融資融券同步 Job |
| `m06/job/FinancialStatementSyncJob.java` | 財報同步 Job |
| `m06/job/DataQualityCheckJob.java` | 品質檢核 Job |

#### DTO & Converter

| 檔案路徑 | 說明 |
|---------|------|
| `m06/dto/FinancialStatementDTO.java` | 財報 DTO |
| `m06/dto/DataRepairResultDTO.java` | 補齊結果 DTO |
| `m06/dto/QualityCheckResultDTO.java` | 品質檢核結果 DTO |
| `m06/dto/request/DataRepairRequest.java` | 補齊請求 DTO |
| `m06/dto/request/QualityCheckExecuteRequest.java` | 品質檢核請求 DTO |
| `m06/dto/request/FinancialSyncRequest.java` | 財報同步請求 DTO |
| `m06/converter/InstitutionalTradingConverter.java` | 法人買賣超轉換器 |
| `m06/converter/MarginTradingConverter.java` | 融資融券轉換器 |
| `m06/converter/FinancialStatementConverter.java` | 財報轉換器 |

#### VO & Enums

| 檔案路徑 | 說明 |
|---------|------|
| `m06/vo/MissingDataVO.java` | 缺漏資料 VO |
| `m06/vo/QualityCheckExecutionVO.java` | 品質檢核執行 VO |
| `m06/enums/RepairStrategy.java` | 補齊策略列舉 |
| `m06/enums/QualityCheckType.java` | 品質檢核類型列舉 |

#### Exception

| 檔案路徑 | 說明 |
|---------|------|
| `m06/exception/DataSyncException.java` | 資料同步例外 |
| `m06/exception/DataRepairException.java` | 資料補齊例外 |

### 5.2 修改檔案

| 檔案路徑 | 修改內容 |
|---------|---------|
| `m06/enums/M06ErrorCode.java` | 新增 P1 相關錯誤碼 |
| `m06/domain/FinancialStatement.java` | 擴充欄位與 JSONB 映射 |
| `m06/dto/InstitutionalTradingDTO.java` | 確認欄位完整性 |
| `m06/dto/MarginTradingDTO.java` | 確認欄位完整性 |
| `m06/service/JobManagementService.java` | 新增 P1 Job 觸發方法 |
| `m06/service/MarketDataQueryService.java` | 實作法人/融資融券查詢 |
| `m06/service/DataQualityService.java` | 整合品質檢核執行 |
| `m06/controller/JobManagementController.java` | 新增 P1 Job 觸發端點 |
| `m06/controller/DataQualityController.java` | 新增品質檢核執行端點 |

### 5.3 新增資料表（由 DBA 或 Flyway 處理）

| 資料表 | 說明 |
|-------|------|
| `institutional_trading` | 三大法人買賣超 |
| `margin_trading` | 融資融券餘額 |

> 注意：`financial_statements` 已存在，可能需要 ALTER TABLE 擴充欄位。

---

## 6. 錯誤碼規劃

### 6.1 現有錯誤碼範圍

| 範圍 | 類別 | 說明 |
|------|------|------|
| M0601x | Stock | 股票相關錯誤 |
| M0602x | Price | 股價相關錯誤 |
| M0603x | Financial | 財報相關錯誤 |
| M0604x | Institutional | 法人籌碼相關錯誤 |
| M0605x | Margin | 融資融券相關錯誤 |
| M0606x | Calendar | 交易日曆相關錯誤 |
| M0607x | Sync | 資料同步相關錯誤 |
| M0608x | Quality | 資料品質相關錯誤 |
| M0609x | Job | 排程任務相關錯誤 |

### 6.2 P1 新增錯誤碼

#### 財報相關 (M0603x)

| 錯誤碼 | 名稱 | HTTP Status | 訊息 |
|-------|------|-------------|------|
| M06031 | FINANCIAL_PARSE_ERROR | 500 | 財報資料解析失敗 |
| M06032 | FINANCIAL_PERIOD_INVALID | 400 | 無效的財報期間 |
| M06033 | FINANCIAL_DATA_INCOMPLETE | 422 | 財報資料不完整 |

#### 法人籌碼相關 (M0604x)

| 錯誤碼 | 名稱 | HTTP Status | 訊息 |
|-------|------|-------------|------|
| M06041 | INSTITUTIONAL_NOT_FOUND | 404 | 找不到法人買賣超資料 |
| M06042 | INSTITUTIONAL_SYNC_FAILED | 500 | 法人資料同步失敗 |
| M06043 | INSTITUTIONAL_PARSE_ERROR | 500 | 法人資料解析失敗 |

#### 融資融券相關 (M0605x)

| 錯誤碼 | 名稱 | HTTP Status | 訊息 |
|-------|------|-------------|------|
| M06051 | MARGIN_NOT_FOUND | 404 | 找不到融資融券資料 |
| M06052 | MARGIN_SYNC_FAILED | 500 | 融資融券資料同步失敗 |
| M06053 | MARGIN_PARSE_ERROR | 500 | 融資融券資料解析失敗 |

#### 資料品質相關 (M0608x) - 擴充

| 錯誤碼 | 名稱 | HTTP Status | 訊息 |
|-------|------|-------------|------|
| M06084 | QUALITY_CHECK_EXECUTION_FAILED | 500 | 品質檢核執行失敗 |
| M06085 | QUALITY_RULE_INVALID | 400 | 無效的品質檢核規則 |
| M06086 | QUALITY_CHECK_TIMEOUT | 504 | 品質檢核執行逾時 |

#### 資料補齊相關 (新範圍 M0610x)

| 錯誤碼 | 名稱 | HTTP Status | 訊息 |
|-------|------|-------------|------|
| M06101 | REPAIR_DATE_RANGE_INVALID | 400 | 無效的補齊日期範圍 |
| M06102 | REPAIR_STRATEGY_INVALID | 400 | 無效的補齊策略 |
| M06103 | REPAIR_EXECUTION_FAILED | 500 | 資料補齊執行失敗 |
| M06104 | REPAIR_NO_MISSING_DATA | 200 | 沒有需要補齊的資料 |
| M06105 | REPAIR_PARTIAL_SUCCESS | 207 | 部分資料補齊成功 |

---

## 7. TODO 項目與未來擴充

### 7.1 P1 實作 TODO

#### 高優先級

- [ ] 實作 `InstitutionalTrading` 實體與 Repository
- [ ] 實作 `MarginTrading` 實體與 Repository
- [ ] 實作 `InstitutionalTradingSyncService` 同步邏輯
- [ ] 實作 `MarginTradingSyncService` 同步邏輯
- [ ] 實作 `FinancialStatementSyncService` 同步邏輯
- [ ] 建立對應的 MyBatis Mapper 與 XML
- [ ] 新增排程 Job 類別
- [ ] 擴充 `JobManagementController` 端點

#### 中優先級

- [ ] 實作 `DataQualityExecutionService` 品質檢核引擎
- [ ] 實作 `DataRepairService` 資料補齊邏輯
- [ ] 完善 `MarketDataQueryService` 中的 TODO 方法
- [ ] 建立品質檢核規則 SQL (MyBatis)

#### 低優先級

- [ ] 新增 P1 相關錯誤碼至 `M06ErrorCode.java`
- [ ] 建立 MapStruct Converter
- [ ] 撰寫單元測試與整合測試
- [ ] 更新 API 文件 (Swagger)

### 7.2 P2 未來擴充

| 功能 | 說明 | 預計範圍 |
|------|------|---------|
| F-M06-010 資料源備援 | 主資料源失敗時自動切換 | 需修改所有 SyncService |
| 即時資料推送 | WebSocket 推送最新資料 | 新增 WebSocket 模組 |
| 資料歸檔機制 | 舊資料自動歸檔至冷儲存 | 新增 Archive Service |
| 多市場支援 | 支援美股、港股等 | 擴充 Client 與 Domain |
| 資料血緣追蹤 | 追蹤資料來源與變更歷程 | 新增 Lineage 模組 |

### 7.3 技術債務

| 項目 | 說明 | 建議處理時機 |
|------|------|-------------|
| 現有 TODO 方法 | `MarketDataQueryService` 中標記 TODO 的方法 | P1 實作時一併處理 |
| FeatureNotImplementedException | 現有拋出此例外的端點 | P1 實作時移除 |
| 測試覆蓋率 | 目前測試覆蓋率不明 | P1 完成後補齊 |
| API 文件同步 | Swagger 文件可能未更新 | 每次 API 變更後更新 |

### 7.4 注意事項

1. **資料庫遷移**：新增資料表需透過 Flyway Migration，不可直接修改 Schema
2. **外部 API 限制**：證交所 API 有呼叫頻率限制，需實作 Rate Limiting
3. **Job 冪等性**：所有 Job 必須保證冪等性，避免重複執行產生重複資料
4. **交易日檢查**：同步 Job 執行前必須檢查是否為交易日
5. **錯誤重試**：外部 API 呼叫失敗時應有指數退避重試機制

---

## 附錄 A：API 端點規劃

### 法人買賣超 API

```
GET  /api/institutional/{stockId}                 # 查詢指定股票法人買賣超
GET  /api/institutional/{stockId}/range           # 查詢日期範圍
GET  /api/institutional/{stockId}/latest          # 查詢最新一筆
GET  /api/institutional/market/{date}             # 查詢全市場某日資料
```

### 融資融券 API

```
GET  /api/margin/{stockId}                        # 查詢指定股票融資融券
GET  /api/margin/{stockId}/range                  # 查詢日期範圍
GET  /api/margin/{stockId}/latest                 # 查詢最新一筆
GET  /api/margin/{stockId}/trend                  # 查詢趨勢（含券資比）
```

### 財報 API

```
GET  /api/financials/{stockId}                    # 查詢指定股票財報
GET  /api/financials/{stockId}/latest             # 查詢最新一期
GET  /api/financials/{stockId}/{year}/{quarter}   # 查詢指定期間
```

### 資料補齊 API

```
POST /api/data-repair/detect                      # 偵測缺漏資料
POST /api/data-repair/execute                     # 執行資料補齊
GET  /api/data-repair/history                     # 查詢補齊歷史
```

### Job 管理 API（擴充）

```
POST /api/jobs/trigger/institutional-sync         # 觸發法人資料同步
POST /api/jobs/trigger/margin-sync                # 觸發融資融券同步
POST /api/jobs/trigger/financial-sync             # 觸發財報同步
POST /api/jobs/trigger/quality-check              # 觸發品質檢核
```

---

## 附錄 B：資料表欄位規劃

### institutional_trading

| 欄位 | 類型 | 說明 |
|------|------|------|
| stock_id | VARCHAR(10) | 股票代碼 (PK) |
| trade_date | DATE | 交易日期 (PK) |
| foreign_buy | BIGINT | 外資買進股數 |
| foreign_sell | BIGINT | 外資賣出股數 |
| foreign_net | BIGINT | 外資買賣超 |
| trust_buy | BIGINT | 投信買進股數 |
| trust_sell | BIGINT | 投信賣出股數 |
| trust_net | BIGINT | 投信買賣超 |
| dealer_buy | BIGINT | 自營商買進股數 |
| dealer_sell | BIGINT | 自營商賣出股數 |
| dealer_net | BIGINT | 自營商買賣超 |
| total_net | BIGINT | 三大法人合計買賣超 |
| created_at | TIMESTAMP | 建立時間 |
| updated_at | TIMESTAMP | 更新時間 |

### margin_trading

| 欄位 | 類型 | 說明 |
|------|------|------|
| stock_id | VARCHAR(10) | 股票代碼 (PK) |
| trade_date | DATE | 交易日期 (PK) |
| margin_buy | BIGINT | 融資買進 |
| margin_sell | BIGINT | 融資賣出 |
| margin_cash_repay | BIGINT | 融資現償 |
| margin_balance | BIGINT | 融資餘額 |
| margin_limit | BIGINT | 融資限額 |
| short_buy | BIGINT | 融券買進 |
| short_sell | BIGINT | 融券賣出 |
| short_cash_repay | BIGINT | 融券現償 |
| short_balance | BIGINT | 融券餘額 |
| short_limit | BIGINT | 融券限額 |
| offset_shares | BIGINT | 資券互抵 |
| margin_short_ratio | DECIMAL(10,4) | 券資比 |
| created_at | TIMESTAMP | 建立時間 |
| updated_at | TIMESTAMP | 更新時間 |

---

*文件結束*
