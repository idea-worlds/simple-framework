package dev.simpleframework.token.context;

import dev.simpleframework.token.exception.InvalidContextException;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public interface SimpleTokenRpcContext extends SimpleTokenContext {

    @Override
    default boolean matchPath(String pattern, String path) {
        throw new InvalidContextException("Method Not support: matchPath");
    }

}
