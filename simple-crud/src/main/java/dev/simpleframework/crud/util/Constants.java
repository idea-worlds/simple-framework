package dev.simpleframework.crud.util;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public final class Constants {

    public static final boolean pageHelperPresent = isPageHelperPresent();

    private static boolean isPageHelperPresent() {
        try {
            Class.forName("com.github.pagehelper.PageHelper");
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

}
