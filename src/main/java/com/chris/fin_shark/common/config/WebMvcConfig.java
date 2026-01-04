package com.chris.fin_shark.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置
 *
 * 配置攔截器、CORS 等 Web 相關設定
 *
 * @author chris
 * @since 2025-12-24
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 註冊攔截器
     *
     * 可在此註冊自訂的攔截器，例如：
     * - 認證攔截器
     * - 日誌攔截器
     * - 限流攔截器
     *
     * @param registry 攔截器註冊器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // TODO: M08 開發時，添加認證攔截器
        // 範例：
        // registry.addInterceptor(new AuthInterceptor())
        //         .addPathPatterns("/api/**")
        //         .excludePathPatterns("/api/auth/login", "/api/auth/register");

        // TODO: 如需限流，可添加限流攔截器
        // registry.addInterceptor(new RateLimitInterceptor())
        //         .addPathPatterns("/api/**");
    }

    /**
     * 配置 CORS（跨來源資源共用）
     *
     * 允許前端應用從不同來源訪問 API
     *
     * 注意事項：
     * - 開發環境：允許 localhost
     * - 生產環境：只允許特定域名
     *
     * @param registry CORS 註冊器
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // 允許的來源
                .allowedOrigins(
                        "http://localhost:3000",    // React 前端
                        "http://localhost:8080"     // 本地測試
                )
                // 允許的 HTTP 方法
                .allowedMethods(
                        "GET",      // 查詢
                        "POST",     // 建立
                        "PUT",      // 完整更新
                        "DELETE",   // 刪除
                        "PATCH",    // 部分更新
                        "OPTIONS"   // 預檢請求
                )
                // 允許的請求標頭
                .allowedHeaders("*")
                // 是否允許攜帶憑證（如 Cookie）
                .allowCredentials(true)
                // 預檢請求的快取時間（秒）
                .maxAge(3600);

        // TODO: 生產環境需要修改允許的來源
        // 範例：.allowedOrigins("https://www.example.com", "https://app.example.com")
    }

    // TODO: 如需自訂訊息轉換器，可覆寫此方法
    // @Override
    // public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    //     // 自訂 JSON 轉換器配置
    // }

    // TODO: 如需自訂視圖解析器，可覆寫此方法
    // @Override
    // public void configureViewResolvers(ViewResolverRegistry registry) {
    //     // 自訂視圖解析器
    // }
}

