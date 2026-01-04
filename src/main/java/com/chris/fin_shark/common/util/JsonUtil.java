package com.chris.fin_shark.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JSON 序列化工具
 *
 * 提供統一的 JSON 序列化/反序列化功能
 *
 * @author chris
 * @since 2025-12-24
 */
@Slf4j
public final class JsonUtil {

    private JsonUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * ObjectMapper 單例
     * 配置:
     * - 支援 Java 8 時間類型
     * - 忽略未知屬性
     * - 不序列化 null 值
     * - 日期格式化為 ISO 8601
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())  // 支援 LocalDate, ZonedDateTime 等
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)  // 忽略未知屬性
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)  // 日期格式化為字串
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);  // 允許空物件

    /**
     * 取得 ObjectMapper 實例
     *
     * @return ObjectMapper
     */
    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

    // ========================================================================
    // 序列化（Object → JSON）
    // ========================================================================

    /**
     * 將物件序列化為 JSON 字串
     *
     * @param obj 物件
     * @return JSON 字串，失敗返回 null
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON: {}", obj.getClass().getName(), e);
            return null;
        }
    }

    /**
     * 將物件序列化為格式化的 JSON 字串（美化輸出）
     *
     * @param obj 物件
     * @return 格式化的 JSON 字串，失敗返回 null
     */
    public static String toPrettyJson(Object obj) {
        if (obj == null) {
            return null;
        }

        try {
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to pretty JSON: {}", obj.getClass().getName(), e);
            return null;
        }
    }

    // ========================================================================
    // 反序列化（JSON → Object）
    // ========================================================================

    /**
     * 將 JSON 字串反序列化為物件
     *
     * @param json JSON 字串
     * @param clazz 目標類別
     * @param <T> 泛型類型
     * @return 物件實例，失敗返回 null
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to object: {}", clazz.getName(), e);
            return null;
        }
    }

    /**
     * 將 JSON 字串反序列化為泛型物件（如 List, Map）
     *
     * @param json JSON 字串
     * @param typeReference 類型引用
     * @param <T> 泛型類型
     * @return 物件實例，失敗返回 null
     *
     * 使用範例:
     * List<StockDTO> list = fromJson(json, new TypeReference<List<StockDTO>>() {});
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        try {
            return OBJECT_MAPPER.readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to generic type", e);
            return null;
        }
    }

    /**
     * 將 JSON 字串反序列化為 Map
     *
     * @param json JSON 字串
     * @return Map<String, Object>，失敗返回空 Map
     */
    public static Map<String, Object> fromJsonToMap(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to Map", e);
            return Collections.emptyMap();
        }
    }

    /**
     * 將 JSON 字串反序列化為 List
     *
     * @param json JSON 字串
     * @param elementClass List 元素類別
     * @param <T> 元素類型
     * @return List<T>，失敗返回空 List
     */
    public static <T> List<T> fromJsonToList(String json, Class<T> elementClass) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            return OBJECT_MAPPER.readValue(
                    json,
                    OBJECT_MAPPER.getTypeFactory().constructCollectionType(List.class, elementClass)
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to List<{}>", elementClass.getName(), e);
            return Collections.emptyList();
        }
    }

    // ========================================================================
    // 物件轉換
    // ========================================================================

    /**
     * 將物件轉換為另一種類型
     *
     * 使用場景: Entity → DTO, DTO → Entity
     *
     * @param source 源物件
     * @param targetClass 目標類別
     * @param <T> 目標類型
     * @return 目標物件實例，失敗返回 null
     */
    public static <T> T convert(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }

        try {
            String json = OBJECT_MAPPER.writeValueAsString(source);
            return OBJECT_MAPPER.readValue(json, targetClass);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert {} to {}",
                    source.getClass().getName(),
                    targetClass.getName(), e);
            return null;
        }
    }

    /**
     * 驗證 JSON 字串是否合法
     *
     * @param json JSON 字串
     * @return true 合法，false 不合法
     */
    public static boolean isValidJson(String json) {
        if (json == null || json.isEmpty()) {
            return false;
        }

        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (JsonProcessingException e) {
            return false;
        }
    }
}
