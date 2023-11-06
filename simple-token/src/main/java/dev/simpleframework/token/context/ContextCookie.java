package dev.simpleframework.token.context;

import dev.simpleframework.token.exception.InvalidTokenException;
import lombok.Getter;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class ContextCookie {

    /**
     * 写入响应头时使用的key
     */
    public static final String HEADER_NAME = "Set-Cookie";

    private static final String DOMAIN = "Domain";
    private static final String MAX_AGE = "Max-Age";
    private static final String PATH = "Path";
    private static final String SECURE = "Secure";
    private static final String HTTP_ONLY = "HttpOnly";
    private static final String SAME_SITE = "SameSite";

    @Getter
    private final String name;
    @Getter
    private final String value;
    private final Map<String, String> attributes = new HashMap<>();

    public ContextCookie(String name, String value) {
        if (name == null || name.isBlank()) {
            throw new InvalidTokenException("cookie name is blank");
        }
        if (value != null && value.contains(";")) {
            throw new InvalidTokenException("cookie value is " + value);
        }
        this.name = name;
        this.value = value;
    }

    public ContextCookie setDomain(String domain) {
        this.putAttribute(DOMAIN, domain != null ? domain.toLowerCase() : null);
        return this;
    }

    public String getDomain() {
        return this.getAttribute(DOMAIN);
    }

    public ContextCookie setMaxAge(int expiry) {
        this.putAttribute(MAX_AGE, expiry < 0 ? null : String.valueOf(expiry));
        return this;
    }

    public int getMaxAge() {
        String maxAge = this.getAttribute(MAX_AGE);
        return maxAge == null ? -1 : Integer.parseInt(maxAge);
    }

    public ContextCookie setPath(String uri) {
        this.putAttribute(PATH, uri);
        return this;
    }

    public String getPath() {
        String path = this.getAttribute(PATH);
        if (path == null) {
            path = "/";
        }
        return path;
    }

    public ContextCookie setSecure(boolean flag) {
        this.putAttribute(SECURE, String.valueOf(flag));
        return this;
    }

    public boolean getSecure() {
        return Boolean.parseBoolean(this.getAttribute(SECURE));
    }

    public ContextCookie setHttpOnly(boolean httpOnly) {
        this.putAttribute(HTTP_ONLY, String.valueOf(httpOnly));
        return this;
    }

    public boolean isHttpOnly() {
        return Boolean.parseBoolean(this.getAttribute(HTTP_ONLY));
    }

    public ContextCookie setSameSite(String sameSite) {
        this.putAttribute(SAME_SITE, sameSite);
        return this;
    }

    public String getSameSite() {
        return this.getAttribute(SAME_SITE);
    }

    private void putAttribute(String name, String value) {
        if (value == null) {
            this.attributes.remove(name);
        } else {
            this.attributes.put(name, value);
        }
    }

    public String getAttribute(String name) {
        return this.attributes.get(name);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(this.name).append("=").append(this.value);

        int maxAge = this.getMaxAge();
        if (maxAge >= 0) {
            result.append("; ").append(MAX_AGE).append("=").append(maxAge);
            String expires;
            if (maxAge == 0) {
                expires = Instant.EPOCH.atOffset(ZoneOffset.UTC).format(DateTimeFormatter.RFC_1123_DATE_TIME);
            } else {
                expires = OffsetDateTime.now().plusSeconds(maxAge).format(DateTimeFormatter.RFC_1123_DATE_TIME);
            }
            result.append("; Expires=").append(expires);
        }

        String domain = this.getDomain();
        if (domain != null) {
            result.append("; ").append(DOMAIN).append("=").append(domain);
        }

        String path = this.getPath();
        if (path != null) {
            result.append("; ").append(PATH).append("=").append(path);
        }

        if (this.getSecure()) {
            result.append("; ").append(SECURE);
        }

        if (this.isHttpOnly()) {
            result.append("; ").append(HTTP_ONLY);
        }

        String sameSite = this.getSameSite();
        if (sameSite != null) {
            result.append("; ").append(SAME_SITE).append("=").append(sameSite);
        }

        return result.toString();
    }

}
