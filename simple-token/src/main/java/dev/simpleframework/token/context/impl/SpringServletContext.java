package dev.simpleframework.token.context.impl;

import dev.simpleframework.token.context.SimpleTokenContextForFramework;
import dev.simpleframework.token.exception.InvalidContextException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.server.PathContainer;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

/**
 * Spring servlet 上下文处理器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public class SpringServletContext
        extends AbstractSimpleTokenContext<HttpServletRequest, HttpServletResponse, HttpServletRequest>
        implements SimpleTokenContextForFramework {

    @Override
    protected HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new InvalidContextException("Can not found HttpServletRequest");
        }
        return attrs.getRequest();
    }

    @Override
    protected HttpServletResponse getResponse() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new InvalidContextException("Can not found HttpServletRequest");
        }
        return attrs.getResponse();
    }

    @Override
    protected HttpServletRequest getStore() {
        return this.getRequest();
    }

    @Override
    protected String getParam(HttpServletRequest request, String key) {
        return request.getParameter(key);
    }

    @Override
    protected String getHeader(HttpServletRequest request, String key) {
        return request.getHeader(key);
    }

    @Override
    protected String getCookie(HttpServletRequest request, String key) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie != null && key.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @Override
    protected void addHeader(HttpServletResponse response, String key, String value) {
        response.addHeader(key, value);
    }

    @Override
    protected Object get(HttpServletRequest store, String key) {
        return store.getAttribute(key);
    }

    @Override
    protected void set(HttpServletRequest store, String key, Object value) {
        store.setAttribute(key, value);
    }

    @Override
    protected void remove(HttpServletRequest store, String key) {
        store.removeAttribute(key);
    }

    @Override
    public boolean matchPath(String pattern, String path) {
        PathPattern pathPattern = PathPatternParser.defaultInstance.parse(pattern);
        PathContainer pathContainer = PathContainer.parsePath(path);
        return pathPattern.matches(pathContainer);
    }

    @Override
    public boolean enable() {
        return RequestContextHolder.getRequestAttributes() != null;
    }

}
