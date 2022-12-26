package dev.simpleframework.util;

import lombok.SneakyThrows;

import java.util.Date;
import java.util.UUID;

/**
 * 字符串工具类
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@SuppressWarnings("all")
public final class Strings {

    /**
     * UUID
     */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * UUID
     */
    public static String uuid32() {
        return uuid().replace("-", "");
    }

    /**
     * 字符串是否有值
     * Alias for org.springframework.util.StringUtils.hasText
     *
     * @param str 字符串
     * @return 是否有值
     */
    public static boolean hasText(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (int i = 0, len = str.length(); i < len; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否空白字符串
     *
     * @param str 字符串
     * @return 是否空白
     */
    public static boolean isBlank(String str) {
        return !hasText(str);
    }

    /**
     * 驼峰转下划线格式
     *
     * @param str 需要转换的字符串
     * @return 转换好的字符串
     */
    public static String camelToUnderline(String str) {
        boolean preIsUnderline = false;
        boolean currentIsUnderline = false;
        int len = str.length();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            currentIsUnderline = '_' == c;
            if (i > 0 && Character.isUpperCase(c) && !preIsUnderline) {
                sb.append("_");
            }
            sb.append(Character.toLowerCase(c));
            preIsUnderline = currentIsUnderline;
        }
        return sb.toString();
    }

    /**
     * 解析字符串，转为目标类实例
     * 1. String：原样
     * 2. Number 子类：调用参数为 string 的构造函数，例 new Integer(defaultValue)
     * 3. Boolean：调用 Boolean.valueOf(str)
     * 4. Date 及其子类：调用 new Date(long date)，即 new Date(Long.parseLong(defaultValue))
     * 5. Class：调用 Class.forName(str)
     * 6. 其他类型：不支持
     *
     * @param str   字符串
     * @param clazz 目标类
     * @return 目标类实例
     */
    @SneakyThrows
    public static <T> T cast(String str, Class<T> clazz) {
        if (String.class == clazz) {
            return (T) str;
        }
        if (Number.class.isAssignableFrom(clazz)) {
            return clazz.getConstructor(String.class).newInstance(str);
        }
        if (Boolean.class == clazz || boolean.class == clazz) {
            return (T) Boolean.valueOf(str);
        }
        if (Date.class.isAssignableFrom(clazz)) {
            long date = Long.parseLong(str);
            return clazz.getConstructor(long.class).newInstance(date);
        }
        if (Class.class == clazz) {
            return (T) Class.forName(str);
        }
        if (Jsons.present()) {
            try {
                return Jsons.read(str, clazz);
            } catch (Exception e) {
                throw new ClassCastException("Can not cast [" + str + "] to " + clazz);
            }
        }
        throw new ClassCastException("Can not cast [" + str + "] to " + clazz);
    }

}
