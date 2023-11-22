package dev.simpleframework.token.context.impl;

import dev.simpleframework.token.context.ContextResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpResponse;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@RequiredArgsConstructor
public class SpringReactorContextResponse implements ContextResponse {
    private final ServerHttpResponse response;

    @Override
    public void addHeader(String name, String value) {
        this.response.getHeaders().add(name, value);
    }

}
