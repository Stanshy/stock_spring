package com.chris.fin_shark.common.util;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 字串工具
 *
 * 提供常用的字串處理功能
 *
 * @author chris
 * @since 2025-12-24
 */
public final class StringUtil {

    private StringUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ========================================================================
    // 字串判斷
    // ========================================================================

    /**
     * 判斷字串是否為空
     *
     * @param str 字串
     * @return true 為空，false 不為空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判斷字串是否不為空
     *
     * @param str 字串
     * @return true 不為空，false 為空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判斷字串是否為空白（包括只有空格）
     *
     * @param str 字串
     * @return true 為空白，false 不為空白
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判斷字串是否不為空白
     *
     * @param str 字串
     * @return true 不為空白，false 為空白
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    // ========================================================================
    // 字串轉換
    // ========================================================================

    /**
     * 轉換為大寫，null 安全
     *
     * @param str 字串
     * @return 大寫字串，null 返回 null
     */
    public static String toUpperCase(String str) {
        return str == null ? null : str.toUpperCase();
    }

    /**
     * 轉換為小寫，null 安全
     *
     * @param str 字串
     * @return 小寫字串，null 返回 null
     */
    public static String toLowerCase(String str) {
        return str == null ? null : str.toLowerCase();
    }

    /**
     * 移除前後空白，null 安全
     *
     * @param str 字串
     * @return 移除空白後的字串，null 返回 null
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 移除所有空白字元
     *
     * @param str 字串
     * @return 移除空白後的字串，null 返回 null
     */
    public static String removeAllWhitespace(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("\\s+", "");
    }

    // ========================================================================
    // 字串填充
    // ========================================================================

    /**
     * 左側填充字元到指定長度
     *
     * @param str 原始字串
     * @param length 目標長度
     * @param padChar 填充字元
     * @return 填充後的字串
     */
    public static String leftPad(String str, int length, char padChar) {
        if (str == null) {
            str = "";
        }

        if (str.length() >= length) {
            return str;
        }

        StringBuilder sb = new StringBuilder(length);
        for (int i = str.length(); i < length; i++) {
            sb.append(padChar);
        }
        sb.append(str);
        return sb.toString();
    }

    /**
     * 右側填充字元到指定長度
     *
     * @param str 原始字串
     * @param length 目標長度
     * @param padChar 填充字元
     * @return 填充後的字串
     */
    public static String rightPad(String str, int length, char padChar) {
        if (str == null) {
            str = "";
        }

        if (str.length() >= length) {
            return str;
        }

        StringBuilder sb = new StringBuilder(length);
        sb.append(str);
        for (int i = str.length(); i < length; i++) {
            sb.append(padChar);
        }
        return sb.toString();
    }

    // ========================================================================
    // 字串拼接
    // ========================================================================

    /**
     * 使用分隔符拼接字串集合
     *
     * @param collection 字串集合
     * @param separator 分隔符
     * @return 拼接後的字串
     */
    public static String join(Collection<String> collection, String separator) {
        if (collection == null || collection.isEmpty()) {
            return "";
        }
        return collection.stream().collect(Collectors.joining(separator));
    }

    /**
     * 使用分隔符拼接字串陣列
     *
     * @param array 字串陣列
     * @param separator 分隔符
     * @return 拼接後的字串
     */
    public static String join(String[] array, String separator) {
        if (array == null || array.length == 0) {
            return "";
        }
        return String.join(separator, array);
    }

    // ========================================================================
    // 預設值處理
    // ========================================================================

    /**
     * 如果字串為空，返回預設值
     *
     * @param str 字串
     * @param defaultValue 預設值
     * @return 字串或預設值
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }

    /**
     * 如果字串為空白，返回預設值
     *
     * @param str 字串
     * @param defaultValue 預設值
     * @return 字串或預設值
     */
    public static String defaultIfBlank(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str;
    }

    // ========================================================================
    // 遮罩處理（用於敏感資料）
    // ========================================================================

    /**
     * 遮罩手機號碼（中間 4 位）
     *
     * @param phone 手機號碼
     * @return 遮罩後的手機號碼，例如: 0912****678
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 8) {
            return phone;
        }
        return phone.substring(0, 4) + "****" + phone.substring(phone.length() - 3);
    }

    /**
     * 遮罩電子郵件（@ 前只保留前 2 位）
     *
     * @param email 電子郵件
     * @return 遮罩後的電子郵件，例如: ab****@example.com
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }

        int atIndex = email.indexOf('@');
        if (atIndex <= 2) {
            return "**" + email.substring(atIndex);
        }

        return email.substring(0, 2) + "****" + email.substring(atIndex);
    }
}
