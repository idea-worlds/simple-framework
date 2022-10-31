package dev.simpleframework.crud.util;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public final class Constants {

    public static boolean jpaPresent;
    public static boolean pageHelperPresent;

    static {
        try {
            Class.forName("javax.persistence.Table");
            jpaPresent = true;
        } catch (Throwable e) {
            jpaPresent = false;
        }
        try {
            Class.forName("com.github.pagehelper.PageHelper");
            pageHelperPresent = true;
        } catch (Throwable e) {
            pageHelperPresent = false;
        }
    }

}
