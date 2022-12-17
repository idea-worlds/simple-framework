package dev.simpleframework.crud.helper;

import dev.simpleframework.crud.core.DatasourceType;

/**
 * 数据源提供者
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public interface DatasourceProvider<T> {

    /**
     * 获取数据源
     *
     * @param name 数据源名称
     * @return 数据源类实例
     */
    T get(String name);

    /**
     * 数据源类型
     */
    DatasourceType support();

    /**
     * 执行 crud 后是否需要自动关闭
     *
     * @param name 数据源名称
     * @return 是否关闭
     */
    default boolean closeable(String name) {
        return true;
    }

    /**
     * 序号（越小约优先）
     */
    default int order() {
        return Integer.MAX_VALUE;
    }

}
