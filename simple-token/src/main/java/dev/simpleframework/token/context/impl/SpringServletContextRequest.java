package dev.simpleframework.token.context.impl;

import dev.simpleframework.token.context.ContextRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@RequiredArgsConstructor
public class SpringServletContextRequest extends AbstractContextRequest implements ContextRequest {
    private final HttpServletRequest request;

    @Override
    public String getParam(String name) {
        return this.request.getParameter(name);
    }

    @Override
    public String getHeader(String name) {
        return this.request.getHeader(name);
    }

    @Override
    public String getCookie(String name) {
        Cookie[] cookies = this.request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie != null && name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @Override
    public String getPath() {
        return this.request.getRequestURI();
    }

    @Override
    public String getMethod() {
        return this.request.getMethod();
    }

    @Override
    public String getIp() {
        return super.getRequestIp(this.request::getHeader, this.request::getRemoteAddr);
    }

    @Override
    public boolean matchPath(String pattern, String path) {
        PathPattern pathPattern = PathPatternParser.defaultInstance.parse(pattern);
        PathContainer pathContainer = PathContainer.parsePath(path);
        return pathPattern.matches(pathContainer);
    }

}
