package com.chris.fin_shark.common.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 序列化配置
 *
 * 遵守總綱 4.4.6 日期時間格式規範
 * 配置 JSON 序列化/反序列化行為
 *
 * @author chris
 * @since 2025-12-24
 */
@Configuration
public class JacksonConfig {

    /**
     * 日期格式: YYYY-MM-DD
     */
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 日期時間格式: YYYY-MM-DDTHH:mm:ss
     */
    private static final String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * 日期時間格式（含時區）: YYYY-MM-DDTHH:mm:ss+08:00
     */
    private static final String DATETIME_WITH_ZONE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssXXX";

    /**
     * 配置全局 ObjectMapper
     *
     * @param builder Jackson2ObjectMapperBuilder
     * @return ObjectMapper
     */
    @Bean
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        ObjectMapper objectMapper = builder.createXmlMapper(false).build();

        // ============================================================
        // 日期時間模組配置（遵守總綱 4.4.6 規範）
        // ============================================================
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // LocalDate 序列化/反序列化
        javaTimeModule.addSerializer(LocalDate.class,
                new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));
        javaTimeModule.addDeserializer(LocalDate.class,
                new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT)));

        // LocalDateTime 序列化/反序列化
        javaTimeModule.addSerializer(LocalDateTime.class,
                new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT)));
        javaTimeModule.addDeserializer(LocalDateTime.class,
                new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT)));

        // ZonedDateTime 序列化（含時區）
        javaTimeModule.addSerializer(ZonedDateTime.class,
                new ZonedDateTimeSerializer(DateTimeFormatter.ofPattern(DATETIME_WITH_ZONE_FORMAT)));

        objectMapper.registerModule(javaTimeModule);

        // ============================================================
        // 序列化配置
        // ============================================================

        // 日期格式化為字串，而非時間戳
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        // 允許序列化空物件
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // 美化輸出（開發環境可開啟，生產環境建議關閉）
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, false);

        // ============================================================
        // 反序列化配置
        // ============================================================

        // 忽略未知屬性（向後相容）
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 允許單引號
        objectMapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);

        // ============================================================
        // 命名策略：Java camelCase → JSON snake_case
        // 遵守總綱 4.4.2 API Response 統一格式規範
        // 
        // 注意：這會全局轉換所有欄位名稱
        // 如果只想在特定 DTO 使用 snake_case，應該在 DTO 類別上使用
        // @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        // ============================================================
        // objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        return objectMapper;
    }
}
