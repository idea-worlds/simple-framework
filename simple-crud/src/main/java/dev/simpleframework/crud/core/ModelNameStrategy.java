package dev.simpleframework.crud.core;

import dev.simpleframework.util.Strings;

import java.util.Locale;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public enum ModelNameStrategy {
    /**
     * 不处理
     */
    NOOP {
        @Override
        public String trans(String name) {
            return name;
        }
    },
    /**
     * 下划线大写
     */
    UNDERLINE_UPPER_CASE {
        @Override
        public String trans(String name) {
            return Strings.camelToUnderline(name).toUpperCase(Locale.ENGLISH);
        }
    },
    /**
     * 下划线小写
     */
    UNDERLINE_LOWER_CASE {
        @Override
        public String trans(String name) {
            return Strings.camelToUnderline(name).toLowerCase(Locale.ENGLISH);
        }
    };

    public abstract String trans(String name);

}
