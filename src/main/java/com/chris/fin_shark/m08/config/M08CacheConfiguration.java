package com.chris.fin_shark.m08.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * M08 快取配置
 * <p>
 * 啟用 Spring Cache
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Configuration
@EnableCaching
public class M08CacheConfiguration {
    // Redis 快取配置由全系統統一管理
    // 快取策略已在 Service 層使用 @Cacheable 標註
}
