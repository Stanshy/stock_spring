package com.chris.fin_shark.common.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置
 *
 * 配置 RedisTemplate 和序列化策略
 *
 * @author chris
 * @since 2025-12-24
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 配置 RedisTemplate
     *
     * Key: String 序列化
     * Value: JSON 序列化
     *
     * @param connectionFactory Redis 連線工廠
     * @return RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // ============================================================
        // 序列化器配置
        // ============================================================

        // String 序列化器（用於 Key）
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // JSON 序列化器（用於 Value）
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);

        // 配置 ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        jsonSerializer.setObjectMapper(objectMapper);

        // ============================================================
        // 設定序列化器
        // ============================================================

        // Key 序列化
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value 序列化
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();

        return template;
    }
}
