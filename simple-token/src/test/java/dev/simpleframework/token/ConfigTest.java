package dev.simpleframework.token;

import dev.simpleframework.token.config.*;
import dev.simpleframework.token.path.PathInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 配置默认值和 token 前缀处理测试
 */
public class ConfigTest {

    // ===== SimpleTokenConfig 默认值 =====

    @Test
    public void testDefaultSessionName() {
        SimpleTokenConfig config = new SimpleTokenConfig();
        assertEquals("simple-token", config.getSessionName());
    }

    @Test
    public void testDefaultTokenName() {
        SimpleTokenConfig config = new SimpleTokenConfig();
        assertEquals("x-acs-token", config.getTokenName());
    }

    @Test
    public void testDefaultTokenPrefix() {
        SimpleTokenConfig config = new SimpleTokenConfig();
        assertEquals("", config.getTokenPrefix());
    }

    // ===== SimpleTokenLoginConfig 默认值 =====

    @Test
    public void testLoginConfigDefaultShareToken() {
        SimpleTokenLoginConfig config = new SimpleTokenLoginConfig();
        assertFalse(config.getShareToken());
    }

    @Test
    public void testLoginConfigDefaultTokenTimeout() {
        SimpleTokenLoginConfig config = new SimpleTokenLoginConfig();
        assertNotNull(config.getTokenTimeout());
    }

    @Test
    public void testLoginConfigDefaultMaxNum() {
        SimpleTokenLoginConfig config = new SimpleTokenLoginConfig();
        assertEquals(-1, config.getMaxNum());
    }

    @Test
    public void testLoginConfigDefaultMaxStrategy() {
        SimpleTokenLoginConfig config = new SimpleTokenLoginConfig();
        assertEquals(dev.simpleframework.token.constant.LoginMaxStrategy.KICK_OUT_FIRST_CREATE,
                config.getMaxStrategy());
    }

    // ===== TokenClientConfig 默认值 =====

    @Test
    public void testClientConfigDefaultMaxNum() {
        SimpleTokenLoginConfig.TokenClientConfig config =
                new SimpleTokenLoginConfig.TokenClientConfig();
        assertEquals(1, config.getMaxNum());
    }

    @Test
    public void testClientConfigDefaultMaxStrategy() {
        SimpleTokenLoginConfig.TokenClientConfig config =
                new SimpleTokenLoginConfig.TokenClientConfig();
        assertEquals(dev.simpleframework.token.constant.LoginMaxStrategy.KICK_OUT_FIRST_CREATE,
                config.getMaxStrategy());
    }

    // ===== findClientConfig =====

    @Test
    public void testFindClientConfigUnknownReturnsDefault() {
        SimpleTokenLoginConfig config = new SimpleTokenLoginConfig();
        SimpleTokenLoginConfig.TokenClientConfig result = config.findClientConfig("unknown");
        assertNotNull(result);
        assertEquals(1, result.getMaxNum());
    }

    @Test
    public void testFindClientConfigCustomReturnsConfigured() {
        SimpleTokenLoginConfig config = new SimpleTokenLoginConfig();
        SimpleTokenLoginConfig.TokenClientConfig custom = new SimpleTokenLoginConfig.TokenClientConfig();
        custom.setMaxNum(5);
        config.getClients().put("web", custom);

        SimpleTokenLoginConfig.TokenClientConfig result = config.findClientConfig("web");
        assertEquals(5, result.getMaxNum());
    }

    // ===== SimpleTokenCookieConfig 默认值 =====

    @Test
    public void testCookieConfigDefaultPath() {
        SimpleTokenConfig config = new SimpleTokenConfig();
        assertEquals("/", config.getCookie().getPath());
    }

    @Test
    public void testCookieConfigDefaultSecure() {
        SimpleTokenConfig config = new SimpleTokenConfig();
        assertTrue(config.getCookie().getSecure());
    }

    @Test
    public void testCookieConfigDefaultHttpOnly() {
        SimpleTokenConfig config = new SimpleTokenConfig();
        assertTrue(config.getCookie().getHttpOnly());
    }

    @Test
    public void testCookieConfigDefaultSameSite() {
        SimpleTokenConfig config = new SimpleTokenConfig();
        assertEquals("Lax", config.getCookie().getSameSite());
    }

    // ===== SimpleTokenPathConfig =====

    @Test
    public void testPathConfigGetAllPermitPathsIncludesPublicAndStatic() {
        SimpleTokenPathConfig config = new SimpleTokenPathConfig();
        config.setPermitPublic(true);
        config.setPermitStatic(true);

        List<PathInfo> paths = config.getAllPermitPaths();
        assertFalse(paths.isEmpty());
    }

    @Test
    public void testPathConfigGetAllPermitPathsDisabled() {
        SimpleTokenPathConfig config = new SimpleTokenPathConfig();
        config.setPermitPublic(false);
        config.setPermitStatic(false);

        List<PathInfo> paths = config.getAllPermitPaths();
        assertTrue(paths.isEmpty());
    }

    // ===== token 前缀处理 =====

    @Test
    public void testSplicingTokenForStoreNullReturnsEmpty() {
        SimpleTokenConfig config = new SimpleTokenConfig();
        assertEquals("", config.splicingTokenForStore(null));
    }

    @Test
    public void testSplicingTokenForStoreEmptyPrefixReturnsToken() {
        SimpleTokenConfig config = new SimpleTokenConfig();
        assertEquals("abc", config.splicingTokenForStore("abc"));
    }

    @Test
    public void testSplicingTokenForStoreWithPrefix() {
        SimpleTokenConfig config = new SimpleTokenConfig();
        config.setTokenPrefix("Bearer");
        assertEquals("Bearer abc", config.splicingTokenForStore("abc"));
    }

    @Test
    public void testParseTokenNullReturnsEmpty() {
        SimpleTokenConfig config = new SimpleTokenConfig();
        assertEquals("", config.parseToken(null));
    }

    @Test
    public void testParseTokenEmptyPrefixReturnsToken() {
        SimpleTokenConfig config = new SimpleTokenConfig();
        assertEquals("abc", config.parseToken("abc"));
    }

    @Test
    public void testParseTokenWithPrefix() {
        SimpleTokenConfig config = new SimpleTokenConfig();
        config.setTokenPrefix("Bearer");
        assertEquals("abc", config.parseToken("Bearer abc"));
    }

    @Test
    public void testParseTokenMissingPrefixThrows() {
        SimpleTokenConfig config = new SimpleTokenConfig();
        config.setTokenPrefix("Bearer");
        assertThrows(dev.simpleframework.token.exception.InvalidTokenException.class, () ->
                config.parseToken("abc"));
    }

    @Test
    public void testParseTokenPrefixNoSpaceThrows() {
        SimpleTokenConfig config = new SimpleTokenConfig();
        config.setTokenPrefix("Bearer");
        assertThrows(dev.simpleframework.token.exception.InvalidTokenException.class, () ->
                config.parseToken("Bearerabc"));
    }

    @Test
    public void testParseTokenWithTrailingSpacePrefix() {
        SimpleTokenConfig config = new SimpleTokenConfig();
        config.setTokenPrefix("Bearer ");
        assertEquals("abc", config.parseToken("Bearer abc"));
    }

}
