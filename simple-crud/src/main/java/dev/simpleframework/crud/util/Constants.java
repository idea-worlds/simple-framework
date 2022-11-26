package dev.simpleframework.crud.util;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public final class Constants {

    public static boolean pageHelperPresent;

    static {
        try {
            Class.forName("com.github.pagehelper.PageHelper");
            pageHelperPresent = true;
        } catch (Throwable e) {
            pageHelperPresent = false;
        }
    }

}
