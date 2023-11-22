package dev.simpleframework.token.context.impl;

import dev.simpleframework.token.context.ContextRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.PathContainer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.net.InetSocketAddress;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@RequiredArgsConstructor
public class SpringReactorContextRequest extends AbstractContextRequest implements ContextRequest {
    private final ServerHttpRequest request;

    @Override
    public String getParam(String name) {
        return this.request.getQueryParams().getFirst(name);
    }

    @Override
    public String getHeader(String name) {
        return this.request.getHeaders().getFirst(name);
    }

    @Override
    public String getCookie(String name) {
        HttpCookie cookie = this.request.getCookies().getFirst(name);
        if (cookie == null) {
            return null;
        }
        return cookie.getValue();
    }

    @Override
    public String getPath() {
        return this.request.getPath().toString();
    }

    @Override
    public String getMethod() {
        return this.request.getMethod().name();
    }

    @Override
    public String getIp() {
        HttpHeaders headers = this.request.getHeaders();
        return super.getRequestIp(headers::getFirst,
                () -> {
                    InetSocketAddress address = this.request.getRemoteAddress();
                    return address == null ? null : address.getAddress().getHostAddress();
                });
    }

    @Override
    public boolean matchPath(String pattern, String path) {
        PathPattern pathPattern = PathPatternParser.defaultInstance.parse(pattern);
        PathContainer pathContainer = PathContainer.parsePath(path);
        return pathPattern.matches(pathContainer);
    }

}
