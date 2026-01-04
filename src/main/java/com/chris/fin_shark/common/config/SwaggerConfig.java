package com.chris.fin_shark.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 配置
 *
 * 自動生成 API 文檔，提供互動式的 API 測試介面
 *
 * 訪問地址: http://localhost:8080/swagger-ui.html
 * API 文檔: http://localhost:8080/api-docs
 *
 * 使用說明：
 * 1. 在 Controller 類別上使用 @Tag 註解標註功能模組
 * 2. 在 API 方法上使用 @Operation 註解說明 API 功能
 * 3. 在參數上使用 @Parameter 註解說明參數用途
 *
 * @author chris
 * @since 2025-12-24
 */
@Configuration
public class SwaggerConfig {

    /**
     * 建立自訂的 OpenAPI 配置
     *
     * 配置項目：
     * - API 基本資訊（標題、版本、描述）
     * - 聯絡資訊
     * - 授權資訊
     * - 伺服器列表
     *
     * @return OpenAPI 物件
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // API 基本資訊
                .info(new Info()
                        // API 標題
                        .title("台股智能監測系統 API")
                        // API 版本（應用程式版本，非 API URL 版本）
                        .version("1.0.0")
                        // API 描述
                        .description(
                                "台股智能監測系統 RESTful API 文檔\n\n" +
                                        "## 功能模組\n" +
                                        "- M06: 資料管理 - 股票、股價資料查詢與同步\n" +
                                        "- M07: 技術分析 - 技術指標計算\n" +
                                        "- M08: 財務分析 - 財務報表分析\n" +
                                        "- M13: 信號偵測 - 買賣信號偵測\n" +
                                        "- M15: 警報通知 - 即時警報推送\n\n" +
                                        "## 資料格式\n" +
                                        "- 所有日期時間使用 ISO 8601 格式\n" +
                                        "- JSON 欄位名稱使用 snake_case\n" +
                                        "- 分頁從第 1 頁開始\n\n" +
                                        "## 錯誤處理\n" +
                                        "- 統一錯誤回應格式\n" +
                                        "- 包含錯誤碼、訊息、追蹤 ID"
                        )
                        // 聯絡資訊
                        .contact(new Contact()
                                .name("Chris")
                                .email("chris@example.com")
                                .url("https://github.com/chris"))
                        // 授權資訊
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))

                // 伺服器列表
                .servers(List.of(
                        // 本地開發環境
                        new Server()
                                .url("http://localhost:8080")
                                .description("本地開發環境"),

                        // 測試環境
                        new Server()
                                .url("https://api-test.example.com")
                                .description("測試環境"),

                        // 生產環境
                        new Server()
                                .url("https://api.example.com")
                                .description("生產環境")
                ));
    }

    // TODO: 如需自訂 Swagger UI 配置，可添加以下 Bean
    // @Bean
    // public GroupedOpenApi publicApi() {
    //     return GroupedOpenApi.builder()
    //             .group("public")
    //             .pathsToMatch("/api/**")
    //             .build();
    // }
}