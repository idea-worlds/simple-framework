package dev.simpleframework.token.session;

/**
 * 会话值生成器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public interface SessionGenerator {

    /**
     * 生成一个新的会话值
     *
     * @param args 参数
     * @return 会话值
     */
    SessionInfo generate(SessionInfo args);

}
