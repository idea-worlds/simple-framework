package dev.simpleframework.token.context;

/**
 * 上下文请求对象
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public interface ContextRequest {

    /**
     * 获取请求参数值
     *
     * @param name 名
     * @return 值
     */
    String getParam(String name);

    /**
     * 获取请求头部值
     *
     * @param name 名
     * @return 值
     */
    String getHeader(String name);

    /**
     * 获取 cookie 的值
     *
     * @param name 名
     * @return 值
     */
    String getCookie(String name);

    /**
     * 获取请求的路径
     *
     * @return 路径
     */
    String getPath();

    /**
     * 获取请求的方法
     *
     * @return 方法
     */
    String getMethod();

    /**
     * 获取请求的 ip
     *
     * @return ip
     */
    String getIp();

    /**
     * 指定路由匹配符是否匹配指定路径
     *
     * @param pattern 路由匹配符
     * @param path    需要匹配的路径
     * @return 是否匹配
     */
    boolean matchPath(String pattern, String path);

}
