package com.chris.fin_shark.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * 外部 API 配置
 * <p>
 * 配置用於呼叫外部 API 的 RestTemplate Bean
 * 包含連線逾時、讀取逾時等設定
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Configuration
public class ExternalApiConfig {

    /**
     * 建立 RestTemplate Bean
     * <p>
     * 設定：
     * - 連線逾時：5 秒
     * - 讀取逾時：10 秒
     * </p>
     *
     * @return 配置好的 RestTemplate 實例
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();

        // 連線逾時（毫秒）
        factory.setConnectTimeout(5000);

        // 讀取逾時（毫秒）
        factory.setReadTimeout(10000);

        return new RestTemplate(factory);
    }
}
