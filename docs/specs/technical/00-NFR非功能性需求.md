# ⚙️ NFR 非功能性需求

> **文件編號**: DOC-SPEC-TECH-03  
> **文件名稱**: 非功能性需求 (Non-Functional Requirements)  
> **版本**: v2.0  
> **最後更新**: 2025-12-31  
> **狀態**: Draft

---

## 📑 目錄

1. [效能需求](#1-效能需求-performance)
2. [可用性需求](#2-可用性需求-availability)
3. [可擴展性需求](#3-可擴展性需求-scalability)
4. [安全性需求](#4-安全性需求-security)
5. [可觀測性需求](#5-可觀測性需求-observability)
6. [可維護性需求](#6-可維護性需求-maintainability)
7. [可測試性需求](#7-可測試性需求-testability)
8. [可部署性需求](#8-可部署性需求-deployability)

---

## 1. 效能需求 (Performance)

### 1.1 回應時間目標

| API 類型 | P50 | P95 | P99 | 說明 |
|---------|-----|-----|-----|------|
| **簡單查詢 API** | < 100ms | < 200ms | < 500ms | 單表查詢，有索引<br>例：查詢單一股票資訊 |
| **一般查詢 API** | < 200ms | < 500ms | < 1s | 多表 JOIN，複雜條件<br>例：查詢股票 + 指標 + 信號 |
| **複雜計算 API** | < 1s | < 2s | < 5s | 回測、多條件選股<br>例：策略回測、多維度篩選 |
| **批次導出 API** | < 5s | < 10s | < 30s | 大量資料導出<br>例：匯出歷史資料 CSV |

**說明**:
- **P50**: 50% 的請求回應時間（中位數）
- **P95**: 95% 的請求回應時間
- **P99**: 99% 的請求回應時間

---

### 1.2 吞吐量目標

| 指標 | 目標值 | 說明 |
|-----|-------|------|
| **API 併發處理能力** | > 500 req/s | 單機支援併發請求數 |
| **資料庫連線數** | 50 - 100 | 連線池大小 |
| **快取命中率** | > 80% | 熱資料快取於 Redis |
| **Job 執行時間** | 股價同步: < 5分鐘<br>指標計算: < 10分鐘 | 批次任務執行時長 |

---

### 1.3 資源使用限制

| 資源 | 限制 | 說明 |
|-----|------|------|
| **JVM Heap Memory** | 2GB - 4GB | 依據伺服器規格調整 |
| **單次查詢結果數** | < 1000 筆 | 使用分頁避免記憶體溢位 |
| **檔案大小** | < 50MB | 上傳檔案大小限制 |
| **WebSocket 連線數** | < 10,000 | 同時 WebSocket 連線數 |

---

### 1.4 效能優化策略

#### 1.4.1 資料庫優化

```java
// ✅ 正確：只查詢需要的欄位
@Query("SELECT s.stockId, s.stockName, s.currentPrice FROM Stock s WHERE s.market = :market")
List<StockSummaryDTO> findStockSummaries(@Param("market") String market);

// ❌ 錯誤：SELECT * 查詢所有欄位
@Query("SELECT s FROM Stock s WHERE s.market = :market")
List<Stock> findAllStocks(@Param("market") String market);
```

**優化要點**:
- ✅ 適當索引設計
- ✅ 避免 SELECT *，只查詢需要的欄位
- ✅ 使用分頁查詢
- ✅ 讀寫分離（Master-Slave）

---

#### 1.4.2 快取策略

**熱資料快取於 Redis**:
```java
@Service
public class StockService {
    
    @Cacheable(value = "stock", key = "#stockId", unless = "#result == null")
    public Stock getStock(String stockId) {
        return stockRepository.findByStockId(stockId)
            .orElseThrow(() -> new StockNotFoundException(stockId));
    }
    
    @CacheEvict(value = "stock", key = "#stockId")
    public void updateStock(String stockId, Stock stock) {
        stockRepository.save(stock);
    }
}
```

**快取設定**:
```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1 小時
      cache-null-values: false
```

**快取策略要點**:
- ✅ 熱資料快取於 Redis（股價、指標、信號）
- ✅ 設定適當的 TTL（Time To Live）
- ✅ 快取預熱（系統啟動時載入常用資料）
- ✅ Cache-Aside 模式

---

#### 1.4.3 程式碼優化

**使用非同步處理**:
```java
@Service
public class SignalService {
    
    @Async
    public CompletableFuture<List<Signal>> generateSignalsAsync(String stockId) {
        List<Signal> signals = generateSignals(stockId);
        return CompletableFuture.completedFuture(signals);
    }
}
```

**批次處理替代逐筆處理**:
```java
// ✅ 正確：批次插入
signalMapper.batchInsertSignals(signals);

// ❌ 錯誤：逐筆插入
for (Signal signal : signals) {
    signalRepository.save(signal);
}
```

**避免 N+1 查詢問題**:
```java
// ✅ 正確：使用 JOIN FETCH
@Query("SELECT s FROM Signal s JOIN FETCH s.stock WHERE s.stockId = :stockId")
List<Signal> findWithStock(@Param("stockId") String stockId);

// ❌ 錯誤：N+1 查詢
List<Signal> signals = signalRepository.findByStockId(stockId);
for (Signal signal : signals) {
    Stock stock = stockRepository.findById(signal.getStockId()); // N+1 問題
}
```

---

#### 1.4.4 架構優化

- ✅ 負載均衡（多實例部署）
- ✅ CDN 加速靜態資源
- ✅ API 回應壓縮（Gzip）
- ✅ 資料庫讀寫分離

---

## 2. 可用性需求 (Availability)

### 2.1 可用性目標

| 指標 | 目標值 | 計算說明 |
|-----|-------|---------|
| **系統可用性** | 99.5% | 每月停機時間 < 3.6 小時 |
| **計畫性維護時間** | < 2小時/月 | 選在低流量時段（如週日凌晨） |
| **非計畫性停機** | < 1小時/月 | 故障快速恢復 |

**可用性計算**:
```
可用性 = (總時間 - 停機時間) / 總時間 × 100%

99.5% 可用性:
- 每月停機時間 < 3.6 小時
- 每週停機時間 < 50 分鐘
- 每天停機時間 < 7 分鐘
```

---

### 2.2 備份與恢復

| 項目 | 策略 | 頻率 | 保留期限 |
|-----|------|------|---------|
| **資料庫完整備份** | pg_dump | 每日 02:00 | 30 天 |
| **資料庫增量備份** | WAL (Write-Ahead Logging) | 實時 | 7 天 |
| **應用程式碼** | Git Repository | 每次 Commit | 永久 |
| **系統配置檔** | 版本控制 + 備份 | 每次修改 | 永久 |

**災難恢復目標**:
- **RTO** (Recovery Time Objective): < 4 小時
- **RPO** (Recovery Point Objective): < 24 小時

**備份腳本範例**:
```bash
#!/bin/bash
# PostgreSQL 備份腳本

BACKUP_DIR="/backup/postgres"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
DATABASE="stock_monitor"

# 完整備份
pg_dump -U postgres -Fc $DATABASE > $BACKUP_DIR/backup_$TIMESTAMP.dump

# 刪除 30 天前的備份
find $BACKUP_DIR -name "backup_*.dump" -mtime +30 -delete
```

---

### 2.3 高可用策略

#### 2.3.1 應用層高可用

```yaml
# Docker Compose 多實例部署範例
version: '3.8'
services:
  app1:
    image: stock-monitor:latest
    ports:
      - "8081:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
  
  app2:
    image: stock-monitor:latest
    ports:
      - "8082:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
  
  nginx:
    image: nginx:latest
    ports:
      - "80:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
    depends_on:
      - app1
      - app2
```

**要點**:
- ✅ 無狀態設計（Session 存於 Redis）
- ✅ 多實例部署 + 負載均衡
- ✅ Docker 容器化，快速重啟
- ✅ 健康檢查與自動重啟

---

#### 2.3.2 資料層高可用

**PostgreSQL 主從複製**:
```sql
-- Master 配置（postgresql.conf）
wal_level = replica
max_wal_senders = 3
wal_keep_size = 64

-- Slave 配置
primary_conninfo = 'host=master_host port=5432 user=replicator'
```

**要點**:
- ✅ PostgreSQL 主從複製（Master-Slave）
- ✅ Redis 哨兵模式（可選）
- ✅ 定期備份與驗證恢復流程

---

#### 2.3.3 監控與告警

**健康檢查**:
```java
@RestController
public class HealthController {
    
    @Autowired
    private DataSource dataSource;
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        
        // 檢查資料庫連線
        try {
            dataSource.getConnection().close();
            status.put("database", "UP");
        } catch (Exception e) {
            status.put("database", "DOWN");
            return ResponseEntity.status(503).body(status);
        }
        
        status.put("status", "UP");
        return ResponseEntity.ok(status);
    }
}
```

**要點**:
- ✅ 健康檢查 API (`/actuator/health`)
- ✅ 關鍵指標監控（CPU、記憶體、磁碟）
- ✅ 異常自動告警（Email + Slack）

---

## 3. 可擴展性需求 (Scalability)

### 3.1 擴展方式

#### 3.1.1 水平擴展 (Scale-Out)

```
          ┌─────────────┐
          │ Load Balancer│
          └──────┬───────┘
                 │
      ┌──────────┼──────────┐
      ↓          ↓          ↓
  ┌──────┐  ┌──────┐  ┌──────┐
  │ App1 │  │ App2 │  │ AppN │
  └──────┘  └──────┘  └──────┘
      │          │          │
      └──────────┼──────────┘
                 ↓
          ┌─────────────┐
          │  Database   │
          └─────────────┘
```

**優點**:
- ✅ 增加應用實例（Docker 容器）
- ✅ Redis Cluster
- ✅ 分庫分表（未來規劃）

---

#### 3.1.2 垂直擴展 (Scale-Up)

**優點**:
- ✅ 提升伺服器硬體規格（CPU、記憶體、磁碟）
- ✅ 優化資料庫配置（連線數、快取大小）

---

### 3.2 擴展指標

| 面向 | 當前支援 | 擴展後目標 |
|-----|---------|----------|
| **併發使用者數** | 1,000 | 10,000+ |
| **股票數量** | 2,000（台股全市場） | 10,000+（跨市場） |
| **歷史資料** | 5年 | 20年+ |
| **API 請求量** | 500 req/s | 5,000 req/s |

---

### 3.3 模組化設計

**當前架構**: Monolithic（單體應用）  
**未來規劃**: Microservices（微服務）

**可拆分的模組**:
1. 資料管理服務
2. 分析計算服務
3. 信號引擎服務
4. 通知服務
5. 使用者服務

**優點**:
- ✅ 獨立部署與擴展
- ✅ 故障隔離
- ✅ 技術棧彈性

**缺點**:
- ⚠️ 系統複雜度提高
- ⚠️ 需要服務治理（Service Mesh）

---

## 4. 安全性需求 (Security)

### 4.1 認證與授權

#### 4.1.1 認證機制

**JWT Token 認證**:
```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

**Token 有效期**:
- Access Token: 2 小時
- Refresh Token: 7 天

**密碼加密**: BCrypt (強度 10)

---

#### 4.1.2 授權機制

**RBAC（基於角色的存取控制）**:

| 角色 | 權限 |
|-----|------|
| **ADMIN** | 系統管理、使用者管理、所有資料存取 |
| **ANALYST** | 資料查詢、指標計算、信號查看 |
| **USER** | 基本查詢、自選股管理 |
| **GUEST** | 僅公開資料查看 |

```java
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/api/users/{userId}")
public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
    userService.deleteUser(userId);
    return ResponseEntity.noContent().build();
}
```

---

### 4.2 資料傳輸安全

| 面向 | 要求 | 實作方式 |
|-----|------|---------|
| **HTTPS** | 必須 | TLS 1.2+ |
| **API Token** | 加密傳輸 | HTTPS + JWT |
| **WebSocket** | 加密連線 | WSS (WebSocket Secure) |
| **敏感資料** | 額外加密 | AES-256（如密碼、API Key） |

---

### 4.3 資料儲存安全

| 資料類型 | 加密方式 | 說明 |
|---------|---------|------|
| **使用者密碼** | BCrypt Hash | 單向加密，不可逆 |
| **API Key** | AES-256 | 對稱加密，可解密 |
| **JWT Secret** | 環境變數 | 不寫入程式碼，不進版控 |
| **資料庫連線密碼** | 環境變數 | 不寫入程式碼 |

**範例**:
```yaml
# application.yml - 不包含敏感資訊
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

# 環境變數設定（.env 檔案，不進版控）
DB_HOST=localhost
DB_PORT=5432
DB_NAME=stock_monitor
DB_USERNAME=postgres
DB_PASSWORD=your_secure_password
```

---

### 4.4 安全防護

#### 4.4.1 SQL 注入防護

```java
// ✅ 正確：使用參數化查詢
@Query("SELECT s FROM Stock s WHERE s.stockId = :stockId")
Optional<Stock> findByStockId(@Param("stockId") String stockId);

// ❌ 錯誤：拼接 SQL 字串
@Query(value = "SELECT * FROM stocks WHERE stock_id = '" + stockId + "'", nativeQuery = true)
Optional<Stock> findByStockIdUnsafe(String stockId);
```

---

#### 4.4.2 XSS 防護

```java
// 前端輸出編碼
<div th:text="${userInput}"></div>  <!-- Thymeleaf 自動編碼 -->

// Content Security Policy (CSP)
@Configuration
public class SecurityHeadersConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.headers()
            .contentSecurityPolicy("default-src 'self'");
        return http.build();
    }
}
```

---

#### 4.4.3 CSRF 防護

```java
// Spring Security CSRF Token
http.csrf()
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
```

---

#### 4.4.4 DDoS 防護

**API Rate Limiting**:
```java
@Configuration
public class RateLimitConfig {
    
    @Bean
    public RateLimiter rateLimiter() {
        return RateLimiter.create(100.0);  // 每秒 100 個請求
    }
}

@RestController
public class StockController {
    
    @Autowired
    private RateLimiter rateLimiter;
    
    @GetMapping("/api/stocks/{stockId}")
    public ResponseEntity<?> getStock(@PathVariable String stockId) {
        if (!rateLimiter.tryAcquire()) {
            return ResponseEntity.status(429).body("Too Many Requests");
        }
        // ... 處理請求
    }
}
```

---

### 4.5 安全審計

**操作日誌記錄**:
```
時間戳 | 使用者ID | IP位址 | 操作類型 | 資源 | 結果 | 詳細資訊
```

**需記錄的操作**:
- ✅ 使用者登入/登出
- ✅ 重要資料修改（信號、警報、交易）
- ✅ 權限變更
- ✅ 異常操作

**日誌保留**: 90 天

---

## 5. 可觀測性需求 (Observability)

### 5.1 日誌 (Logging)

#### 5.1.1 日誌級別

| 級別 | 用途 | 範例 |
|-----|------|------|
| **DEBUG** | 開發環境詳細資訊 | 變數值、方法呼叫 |
| **INFO** | 正常操作資訊 | Job 執行、API 呼叫 |
| **WARN** | 警告訊息 | 快取未命中、重試 |
| **ERROR** | 錯誤訊息 | 異常、失敗操作 |

---

#### 5.1.2 日誌格式

```
[時間戳] [級別] [Thread] [類別] [trace_id] [user_id] - 訊息內容
```

**範例**:
```
2025-12-31 15:30:05.123 INFO [http-nio-8080-exec-1] [StockController] 
[trace_id=abc123] [user_id=user456] - Fetching stock data for 2330
```

---

#### 5.1.3 日誌管理

```yaml
logging:
  level:
    root: INFO
    com.stockmonitor: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/stock-monitor.log
    max-size: 100MB
    max-history: 30
```

**日誌管理要點**:
- 滾動策略: 每日或 100MB
- 保留期限: 30 天
- 日誌格式: JSON（便於解析）

---

### 5.2 指標 (Metrics)

使用 **Micrometer + Spring Actuator**

#### 5.2.1 系統指標

- JVM Heap Memory 使用率
- JVM GC 頻率與時間
- CPU 使用率
- 執行緒數量

#### 5.2.2 應用指標

- API 請求數（總數、成功、失敗）
- API 回應時間（P50, P95, P99）
- 資料庫連線池使用率
- Redis 連線數與命中率

#### 5.2.3 業務指標

- 每日新增信號數
- 警報觸發次數
- Job 執行成功率
- 使用者活躍數

---

### 5.3 追蹤 (Tracing)

**Request Tracing**:
```java
@Component
public class TraceIdFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   FilterChain filterChain) {
        String traceId = UUID.randomUUID().toString();
        MDC.put("trace_id", traceId);
        response.setHeader("X-Trace-Id", traceId);
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
```

**要點**:
- ✅ 每個 API 請求產生唯一 `trace_id`
- ✅ trace_id 記錄於 Response Header 與 Log
- ✅ 方便問題追蹤與除錯

---

### 5.4 健康檢查

**Spring Actuator Endpoints**:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
```

**健康檢查回應**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "SELECT 1"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0.5"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "free": 10737418240,
        "threshold": 10485760
      }
    }
  }
}
```

---

### 5.5 告警策略

#### 5.5.1 告警級別

| 級別 | 說明 | 處理時間 |
|-----|------|---------|
| **CRITICAL** | 系統不可用 | 立即處理 |
| **HIGH** | 影響核心功能 | 1小時內 |
| **MEDIUM** | 影響部分功能 | 4小時內 |
| **LOW** | 資訊通知 | 定期查看 |

---

#### 5.5.2 告警條件

| 告警項目 | 條件 | 級別 | 通知方式 |
|---------|------|------|---------|
| **應用停止運行** | Health Check 失敗 | CRITICAL | Email + Slack + SMS |
| **資料庫連線失敗** | 連續失敗 3 次 | CRITICAL | Email + Slack |
| **API 錯誤率過高** | 錯誤率 > 5% | HIGH | Email + Slack |
| **JVM Memory 過高** | 使用率 > 90% | HIGH | Slack |
| **關鍵 Job 失敗** | 連續失敗 3 次 | HIGH | Email |
| **磁碟空間不足** | 剩餘空間 < 10% | MEDIUM | Email |
| **Redis 命中率低** | 命中率 < 60% | LOW | 每日彙整 |

---

## 6. 可維護性需求 (Maintainability)

### 6.1 程式碼品質要求

**程式碼規範**:
- ✅ 遵循 Google Java Style Guide
- ✅ 使用 Checkstyle / SonarLint 檢查
- ✅ Pull Request 前必須通過 Code Review

**測試覆蓋率**:
- Service 層: > 70%
- Repository 層: > 60%
- Controller 層: > 50%

**程式碼複雜度**:
- 單一方法不超過 50 行
- 圈複雜度 (Cyclomatic Complexity) < 10
- 避免過深的巢狀 (< 3層)

---

### 6.2 文檔要求

**必要文檔**:
- ✅ README.md: 專案說明與快速開始
- ✅ API 文檔: Swagger/OpenAPI（自動生成）
- ✅ 架構文檔: 本系列 SA/SD 文件
- ✅ 資料庫文檔: ERD + 資料字典
- ✅ 部署文檔: 環境配置與部署步驟

---

## 7. 可測試性需求 (Testability)

### 7.1 測試策略

**測試金字塔**:
```
        /\
       /E2E\      (少量端到端測試)
      /------\
     /整合測試 \   (中量整合測試)
    /----------\
   /  單元測試   \  (大量單元測試)
  /--------------\
```

---

### 7.2 測試類型

| 測試類型 | 說明 | 工具 |
|---------|------|------|
| **單元測試** | 測試單一方法或類別 | JUnit 5 + Mockito |
| **整合測試** | 測試模組間整合 | Spring Boot Test + Testcontainers |
| **API 測試** | 測試 REST API 端點 | MockMvc / RestAssured |
| **效能測試** | 測試效能與吞吐量 | JMeter / Gatling |

---

## 8. 可部署性需求 (Deployability)

### 8.1 部署方式

**容器化部署**:
```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/stock-monitor.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

### 8.2 CI/CD 流程

**持續整合 (CI)**:
```
程式碼提交 → 編譯 → 單元測試 → 整合測試 → 程式碼檢查 → 建置 Docker Image
```

**持續部署 (CD)**:
```
Docker Image → 推送到 Registry → 部署到測試環境 → 自動化測試 → 部署到生產環境
```

---

### 8.3 回滾策略

**回滾觸發條件**:
- 部署後健康檢查失敗
- 錯誤率突然升高
- 使用者回報嚴重問題

**回滾步驟**:
1. 停止新版本服務
2. 啟動舊版本服務
3. 驗證舊版本運作正常
4. 調查問題原因

---

## 📚 相關文檔

- [技術架構](00-技術架構.md)
- [開發準則](00-開發準則.md)
- [全系統契約](00-全系統契約.md)

---

**文件維護者**: 系統架構師  
**最後更新**: 2025-12-31  
**下次審核**: 2026-01-31
