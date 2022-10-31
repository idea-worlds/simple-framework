package dev.simpleframework.crud;

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
     * 执行 crud 后是否需要自动关闭
     *
     * @param name 数据源名称
     * @return 是否关闭
     */
    default boolean closeable(String name) {
        return true;
    }

    /**
     * 序号
     * 注册时较小序号的数据源提供者将替换同类型的数据源提供者
     */
    default int order() {
        return Integer.MAX_VALUE;
    }

}
