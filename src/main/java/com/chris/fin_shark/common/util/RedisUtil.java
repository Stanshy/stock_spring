package com.chris.fin_shark.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 操作工具
 *
 * 提供常用的 Redis 操作封裝
 *
 * 注意: 這是一個 Spring Bean，不是純靜態工具類別
 *
 * @author chris
 * @since 2025-12-24
 */
@Component
@Slf4j
public class RedisUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // ========================================================================
    // 基本操作
    // ========================================================================

    /**
     * 設定鍵值
     *
     * @param key 鍵
     * @param value 值
     * @return true 成功，false 失敗
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis set failed: key={}", key, e);
            return false;
        }
    }

    /**
     * 設定鍵值（含過期時間）
     *
     * @param key 鍵
     * @param value 值
     * @param timeout 過期時間
     * @param unit 時間單位
     * @return true 成功，false 失敗
     */
    public boolean set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            return true;
        } catch (Exception e) {
            log.error("Redis set with timeout failed: key={}, timeout={}", key, timeout, e);
            return false;
        }
    }

    /**
     * 取得值
     *
     * @param key 鍵
     * @return 值，不存在返回 null
     */
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("Redis get failed: key={}", key, e);
            return null;
        }
    }

    /**
     * 刪除鍵
     *
     * @param key 鍵
     * @return true 成功，false 失敗
     */
    public boolean delete(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Exception e) {
            log.error("Redis delete failed: key={}", key, e);
            return false;
        }
    }

    /**
     * 批次刪除鍵
     *
     * @param keys 鍵集合
     * @return 刪除的數量
     */
    public long delete(Collection<String> keys) {
        try {
            Long count = redisTemplate.delete(keys);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Redis batch delete failed: keys={}", keys, e);
            return 0;
        }
    }

    /**
     * 判斷鍵是否存在
     *
     * @param key 鍵
     * @return true 存在，false 不存在
     */
    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Redis exists check failed: key={}", key, e);
            return false;
        }
    }

    /**
     * 設定過期時間
     *
     * @param key 鍵
     * @param timeout 過期時間
     * @param unit 時間單位
     * @return true 成功，false 失敗
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
        } catch (Exception e) {
            log.error("Redis expire failed: key={}, timeout={}", key, timeout, e);
            return false;
        }
    }

    /**
     * 取得剩餘過期時間（秒）
     *
     * @param key 鍵
     * @return 剩餘秒數，-1 表示永不過期，-2 表示鍵不存在
     */
    public long getExpire(String key) {
        try {
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            return expire != null ? expire : -2;
        } catch (Exception e) {
            log.error("Redis getExpire failed: key={}", key, e);
            return -2;
        }
    }

    // ========================================================================
    // Hash 操作
    // ========================================================================

    /**
     * Hash 設定欄位值
     *
     * @param key 鍵
     * @param field 欄位
     * @param value 值
     * @return true 成功，false 失敗
     */
    public boolean hSet(String key, String field, Object value) {
        try {
            redisTemplate.opsForHash().put(key, field, value);
            return true;
        } catch (Exception e) {
            log.error("Redis hSet failed: key={}, field={}", key, field, e);
            return false;
        }
    }

    /**
     * Hash 取得欄位值
     *
     * @param key 鍵
     * @param field 欄位
     * @return 值，不存在返回 null
     */
    public Object hGet(String key, String field) {
        try {
            return redisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            log.error("Redis hGet failed: key={}, field={}", key, field, e);
            return null;
        }
    }

    /**
     * Hash 取得所有欄位值
     *
     * @param key 鍵
     * @return Map
     */
    public Map<Object, Object> hGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("Redis hGetAll failed: key={}", key, e);
            return Map.of();
        }
    }

    /**
     * Hash 刪除欄位
     *
     * @param key 鍵
     * @param fields 欄位陣列
     * @return 刪除的數量
     */
    public long hDelete(String key, Object... fields) {
        try {
            return redisTemplate.opsForHash().delete(key, fields);
        } catch (Exception e) {
            log.error("Redis hDelete failed: key={}", key, e);
            return 0;
        }
    }

    // ========================================================================
    // List 操作
    // ========================================================================

    /**
     * List 左側推入
     *
     * @param key 鍵
     * @param value 值
     * @return true 成功，false 失敗
     */
    public boolean lPush(String key, Object value) {
        try {
            redisTemplate.opsForList().leftPush(key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis lPush failed: key={}", key, e);
            return false;
        }
    }

    /**
     * List 右側推入
     *
     * @param key 鍵
     * @param value 值
     * @return true 成功，false 失敗
     */
    public boolean rPush(String key, Object value) {
        try {
            redisTemplate.opsForList().rightPush(key, value);
            return true;
        } catch (Exception e) {
            log.error("Redis rPush failed: key={}", key, e);
            return false;
        }
    }

    /**
     * List 取得範圍內的元素
     *
     * @param key 鍵
     * @param start 開始索引
     * @param end 結束索引
     * @return 元素列表
     */
    public List<Object> lRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("Redis lRange failed: key={}", key, e);
            return List.of();
        }
    }

    // ========================================================================
    // Set 操作
    // ========================================================================

    /**
     * Set 添加元素
     *
     * @param key 鍵
     * @param values 值陣列
     * @return 添加的數量
     */
    public long sAdd(String key, Object... values) {
        try {
            Long count = redisTemplate.opsForSet().add(key, values);
            return count != null ? count : 0;
        } catch (Exception e) {
            log.error("Redis sAdd failed: key={}", key, e);
            return 0;
        }
    }

    /**
     * Set 取得所有元素
     *
     * @param key 鍵
     * @return 元素集合
     */
    public Set<Object> sMembers(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("Redis sMembers failed: key={}", key, e);
            return Set.of();
        }
    }

    /**
     * Set 判斷元素是否存在
     *
     * @param key 鍵
     * @param value 值
     * @return true 存在，false 不存在
     */
    public boolean sIsMember(String key, Object value) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
        } catch (Exception e) {
            log.error("Redis sIsMember failed: key={}", key, e);
            return false;
        }
    }

    // TODO: 各模組開發時，可以在此補充更多 Redis 操作方法
    // 範例:
    // - Sorted Set 操作
    // - Geo 操作
    // - HyperLogLog 操作
    // - 分散式鎖
}
