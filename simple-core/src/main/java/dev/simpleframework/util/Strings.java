package dev.simpleframework.util;

import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    /**
     * 模糊匹配
     * 1. 两者都为 null     -> true
     * 2. 其中之一为 null   -> false
     * 3. 其中之一为空字符串  -> false
     * 4. 两者为同字符串     -> true
     * 5. 示例：表达式、要匹配的字符串
     * -  *foo、foo         -> true
     * -  *foo、bar         -> false
     * -  *foo、foobar      -> false
     * -  *foo、barfoo      -> true
     * -  *foo、bar.foo     -> true
     * -  foo*、foo         -> true
     * -  foo*、bar         -> false
     * -  foo*、barfoo      -> false
     * -  foo*、foobar      -> true
     * -  foo*、foo.bar     -> true
     * -  foo*bar、foo      -> false
     * -  foo*bar、bar      -> false
     * -  foo*bar、barfoo   -> false
     * -  foo*bar、bar.foo  -> false
     * -  foo*bar、foobar   -> true
     * -  foo*bar、foo.bar  -> true
     * -  foo*bar、1bar.foo -> false
     * -  foo*bar、bar.foo1 -> false
     *
     * @param likeChar 表达式中的模糊字符，如 % 或 *
     * @param pattern  表达式
     * @param str      要匹配的字符串
     * @return 表达式是否匹配字符串
     */
    public static boolean like(char likeChar, String pattern, String str) {
        // 都为 null ：相等
        if (pattern == null && str == null) {
            return true;
        }
        // 其中之一为 null ：不匹配
        if (pattern == null || str == null) {
            return false;
        }
        // 其中之一为空字符串：不匹配
        if (pattern.isEmpty() && !str.isEmpty() || !pattern.isEmpty() && str.isEmpty()) {
            return false;
        }
        // 将表达式按模糊字符分割为多个关键字
        List<String> keywords = new ArrayList<>();
        String prefixStr = "";
        String likeStr = String.valueOf(likeChar);
        String pattChar;
        for (int i = 0, len = pattern.length(); i < len; i++) {
            pattChar = String.valueOf(pattern.charAt(i));
            if (likeStr.equals(pattChar)) {
                if (prefixStr.isEmpty()) {
                    prefixStr = pattChar;
                } else if (!likeStr.equals(prefixStr)) {
                    keywords.add(prefixStr);
                    prefixStr = pattChar;
                }
            } else {
                if (likeStr.equals(prefixStr)) {
                    keywords.add(prefixStr);
                    prefixStr = "";
                }
                prefixStr = prefixStr + pattChar;
            }
        }
        if (!prefixStr.isEmpty()) {
            keywords.add(prefixStr);
        }

        int keywordSize = keywords.size();
        // 只有一个关键字时：全模糊或者全匹配
        if (keywordSize == 1) {
            String keyword = keywords.get(0);
            return likeStr.equals(keyword) || keyword.equals(str);
        }
        String matchStr = str;
        String keyword;
        for (int i = 0; i < keywordSize; i++) {
            keyword = keywords.get(i);
            // 第一个关键字
            if (i == 0) {
                // 关键字是模糊字符：继续匹配
                if (likeStr.equals(keyword)) {
                    continue;
                }
                // 关键字不是模糊字符：不是以关键字起始则匹配失败，否则截取关键字后继续匹配
                if (matchStr.startsWith(keyword)) {
                    matchStr = matchStr.substring(keyword.length());
                } else {
                    return false;
                }
            } else {
                // 是否最后一个关键字
                boolean last = i == keywordSize - 1;
                // 关键字是模糊字符
                if (likeStr.equals(keyword)) {
                    // 已是最后一个关键字：直接匹配成功
                    if (last) {
                        return true;
                    }
                    // 不是最后一个关键字：继续下一匹配
                    continue;
                }

                // 关键字不是模糊字符，前关键字必是模糊字符
                // 不包含关键字：匹配失败
                int indexOfMatch = matchStr.indexOf(keyword);
                if (indexOfMatch < 0) {
                    return false;
                }
                // 包含关键字：截取关键字后继续匹配
                matchStr = matchStr.substring(indexOfMatch + keyword.length());
                while (matchStr.startsWith(keyword)) {
                    // 有回文则一直截取到最后一个回文
                    matchStr = matchStr.substring(keyword.length());
                }

                // 已是最后一个关键字：判断匹配字符串是否截取至空
                if (last) {
                    return matchStr.isEmpty();
                }
            }
        }
        return false;
    }

}
