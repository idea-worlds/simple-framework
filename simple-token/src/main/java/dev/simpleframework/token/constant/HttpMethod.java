package dev.simpleframework.token.constant;

import org.springframework.lang.Nullable;

import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public final class HttpMethod {
    public static final HttpMethod ALL = new HttpMethod("*");
    public static final HttpMethod GET = new HttpMethod("GET");
    public static final HttpMethod HEAD = new HttpMethod("HEAD");
    public static final HttpMethod POST = new HttpMethod("POST");
    public static final HttpMethod PUT = new HttpMethod("PUT");
    public static final HttpMethod PATCH = new HttpMethod("PATCH");
    public static final HttpMethod DELETE = new HttpMethod("DELETE");
    public static final HttpMethod OPTIONS = new HttpMethod("OPTIONS");
    public static final HttpMethod TRACE = new HttpMethod("TRACE");
    private final String name;

    private HttpMethod(String name) {
        this.name = name;
    }

    public String name() {
        return this.name;
    }

    public static HttpMethod valueOf(String method) {
        if (method == null) {
            return ALL;
        }
        method = method.toUpperCase();
        return switch (method) {
            case "*" -> ALL;
            case "GET" -> GET;
            case "HEAD" -> HEAD;
            case "POST" -> POST;
            case "PUT" -> PUT;
            case "PATCH" -> PATCH;
            case "DELETE" -> DELETE;
            case "OPTIONS" -> OPTIONS;
            case "TRACE" -> TRACE;
            default -> new HttpMethod(method);
        };
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof HttpMethod o) {
            return this.name.equals(o.name);
        }
        return false;
    }

    public static boolean contains(List<HttpMethod> methods, HttpMethod method) {
        if (method == null || methods.isEmpty()) {
            return false;
        }
        for (HttpMethod m : methods) {
            if (m == ALL || m.equals(method)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
