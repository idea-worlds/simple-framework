package dev.simpleframework.token.context;

import dev.simpleframework.token.config.SimpleTokenConfig;
import dev.simpleframework.token.exception.InvalidTokenException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * ContextCookie + SimpleTokenConfig token 处理 测试
 */
public class ContextCookieTest {

    // ===== ContextCookie 构造 =====

    @Test
    public void constructor_nullName_throws() {
        Assertions.assertThrows(InvalidTokenException.class, () ->
                new ContextCookie(null, "value"));
    }

    @Test
    public void constructor_blankName_throws() {
        Assertions.assertThrows(InvalidTokenException.class, () ->
                new ContextCookie("  ", "value"));
    }

    @Test
    public void constructor_valueWithSemicolon_throws() {
        Assertions.assertThrows(InvalidTokenException.class, () ->
                new ContextCookie("name", "val;ue"));
    }

    @Test
    public void constructor_nullValue_valid() {
        ContextCookie cookie = new ContextCookie("name", null);
        Assertions.assertEquals("name", cookie.getName());
        Assertions.assertNull(cookie.getValue());
    }

    // ===== ContextCookie toString =====

    @Test
    public void toString_basic() {
        ContextCookie cookie = new ContextCookie("token", "abc123");
        String result = cookie.toString();
        Assertions.assertTrue(result.startsWith("token=abc123"));
        Assertions.assertTrue(result.contains("Path=/"));
    }

    @Test
    public void toString_maxAgeZero_deletesCookie() {
        ContextCookie cookie = new ContextCookie("token", "abc123");
        cookie.setMaxAge(0);
        String result = cookie.toString();
        Assertions.assertTrue(result.contains("Max-Age=0"));
        Assertions.assertTrue(result.contains("Expires=Thu, 1 Jan 1970"));
    }

    @Test
    public void toString_withDomain() {
        ContextCookie cookie = new ContextCookie("token", "abc123");
        cookie.setDomain("example.com");
        String result = cookie.toString();
        Assertions.assertTrue(result.contains("Domain=example.com"));
    }

    @Test
    public void toString_secureAndHttpOnly() {
        ContextCookie cookie = new ContextCookie("token", "abc123");
        cookie.setSecure(true);
        cookie.setHttpOnly(true);
        String result = cookie.toString();
        Assertions.assertTrue(result.contains("Secure"));
        Assertions.assertTrue(result.contains("HttpOnly"));
    }

    @Test
    public void toString_sameSite() {
        ContextCookie cookie = new ContextCookie("token", "abc123");
        cookie.setSameSite("Lax");
        String result = cookie.toString();
        Assertions.assertTrue(result.contains("SameSite=Lax"));
    }

    @Test
    public void toString_noExpires_whenMaxAgeNegative() {
        ContextCookie cookie = new ContextCookie("token", "abc123");
        cookie.setMaxAge(-1);
        String result = cookie.toString();
        // Max-Age为负时不应出现在输出中
        Assertions.assertFalse(result.contains("Max-Age"));
        Assertions.assertFalse(result.contains("Expires"));
    }

    // ===== ContextCookie getter 默认值 =====

    @Test
    public void getPath_defaultIsSlash() {
        ContextCookie cookie = new ContextCookie("token", "abc123");
        Assertions.assertEquals("/", cookie.getPath());
    }

    @Test
    public void getMaxAge_defaultIsNegative() {
        ContextCookie cookie = new ContextCookie("token", "abc123");
        Assertions.assertEquals(-1, cookie.getMaxAge());
    }

    @Test
    public void getSecure_defaultIsFalse() {
        ContextCookie cookie = new ContextCookie("token", "abc123");
        Assertions.assertFalse(cookie.getSecure());
    }

    @Test
    public void isHttpOnly_defaultIsFalse() {
        ContextCookie cookie = new ContextCookie("token", "abc123");
        Assertions.assertFalse(cookie.isHttpOnly());
    }

    @Test
    public void getDomain_defaultIsNull() {
        ContextCookie cookie = new ContextCookie("token", "abc123");
        Assertions.assertNull(cookie.getDomain());
    }

    @Test
    public void getSameSite_defaultIsNull() {
        ContextCookie cookie = new ContextCookie("token", "abc123");
        Assertions.assertNull(cookie.getSameSite());
    }

    // ===== ContextCookie 属性 set/get =====

    @Test
    public void setMaxAge_negative_removesAttribute() {
        ContextCookie cookie = new ContextCookie("token", "abc123");
        cookie.setMaxAge(3600);
        Assertions.assertEquals(3600, cookie.getMaxAge());
        cookie.setMaxAge(-1);
        Assertions.assertEquals(-1, cookie.getMaxAge());
    }

    @Test
    public void setDomain_null_removesDomain() {
        ContextCookie cookie = new ContextCookie("token", "abc123");
        cookie.setDomain("example.com");
        Assertions.assertEquals("example.com", cookie.getDomain());
        cookie.setDomain(null);
        Assertions.assertNull(cookie.getDomain());
    }

    // ===== SimpleTokenConfig token 前缀处理 =====

    @Test
    public void splicingTokenForStore_nullToken_returnsEmpty() {
        SimpleTokenConfig config = new SimpleTokenConfig();
        Assertions.assertEquals("", config.splicingTokenForStore(null));
    }

    @Test
    public void splicingTokenForStore_emptyPrefix_returnsToken() {
        SimpleTokenConfig config = new SimpleTokenConfig();
        config.setTokenPrefix("");
        Assertions.assertEquals("abc", config.splicingTokenForStore("abc"));
    }

    @Test
    public void splicingTokenForStore_withPrefix() {
        SimpleTokenConfig config = new SimpleTokenConfig();
        config.setTokenPrefix("Bearer");
        Assertions.assertEquals("Bearer abc", config.splicingTokenForStore("abc"));
    }

    @Test
    public void parseToken_null_returnsEmpty() {
        SimpleTokenConfig config = new SimpleTokenConfig();
        Assertions.assertEquals("", config.parseToken(null));
    }

    @Test
    public void parseToken_emptyPrefix_returnsToken() {
        SimpleTokenConfig config = new SimpleTokenConfig();
        Assertions.assertEquals("abc", config.parseToken("abc"));
    }

    @Test
    public void parseToken_withPrefix() {
        SimpleTokenConfig config = new SimpleTokenConfig();
        config.setTokenPrefix("Bearer");
        Assertions.assertEquals("abc", config.parseToken("Bearer abc"));
    }

    @Test
    public void parseToken_missingPrefix_throws() {
        SimpleTokenConfig config = new SimpleTokenConfig();
        config.setTokenPrefix("Bearer");
        Assertions.assertThrows(InvalidTokenException.class, () ->
                config.parseToken("abc"));
    }
}
