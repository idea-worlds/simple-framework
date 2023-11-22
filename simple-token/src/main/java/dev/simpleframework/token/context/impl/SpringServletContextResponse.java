package dev.simpleframework.token.context.impl;

import dev.simpleframework.token.context.ContextResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@RequiredArgsConstructor
public class SpringServletContextResponse implements ContextResponse {
    private final HttpServletResponse response;

    @Override
    public void addHeader(String name, String value) {
        this.response.addHeader(name, value);
    }

}
