package dev.simpleframework.token;

import dev.simpleframework.token.config.SimpleTokenConfig;
import dev.simpleframework.token.config.SimpleTokenLoginConfig;
import dev.simpleframework.token.config.SimpleTokenPathConfig;
import dev.simpleframework.token.constant.LoginMaxStrategy;
import dev.simpleframework.token.context.*;
import dev.simpleframework.token.exception.InvalidTokenException;
import dev.simpleframework.token.exception.LoginAccountNotFoundException;
import dev.simpleframework.token.exception.LoginPasswordInvalidException;
import dev.simpleframework.token.exception.LoginRejectException;
import dev.simpleframework.token.exception.LoginUserNotFoundException;
import dev.simpleframework.token.exception.SimpleTokenException;
import dev.simpleframework.token.session.LoginSetting;
import dev.simpleframework.token.session.SessionInfo;
import dev.simpleframework.token.session.SessionManager;
import dev.simpleframework.token.session.SessionPerson;
import dev.simpleframework.token.user.UserAccount;
import dev.simpleframework.token.user.UserInfo;
import dev.simpleframework.token.user.UserManager;
import dev.simpleframework.token.user.UserQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * simple-token 核心端到端测试：登录/登出/踢人/刷新
 */
public class SimpleTokenCoreTest {

    private final Map<String, Object> storeData = new HashMap<>();

    @BeforeEach
    public void setUp() {
        // 设置全局配置
        SimpleTokenConfig config = new SimpleTokenConfig();
        config.setLogin(new SimpleTokenLoginConfig());
        config.setPath(new SimpleTokenPathConfig());
        SimpleTokens.setGlobalConfig(config);

        // 注册 UserQuery
        UserManager.registerQuery(new UserQuery() {
            @Override
            public UserInfo getInfoById(String loginId) {
                return new UserInfo(loginId);
            }

            @Override
            public UserAccount getAccountByName(String accountType, String accountName) {
                UserAccount account = new UserAccount();
                account.setUserId("1");
                account.setName(accountName);
                account.setPassword(accountName);
                return account;
            }
        });

        // 注册 FrameworkContext
        storeData.clear();
        ContextManager.registerFrameworkContext(new FrameworkContext() {
            @Override
            public ContextRequest request() {
                return new ContextRequest() {
                    @Override
                    public String getParam(String name) {
                        return null;
                    }

                    @Override
                    public String getHeader(String name) {
                        return null;
                    }

                    @Override
                    public String getCookie(String name) {
                        return null;
                    }

                    @Override
                    public String getPath() {
                        return "/";
                    }

                    @Override
                    public String getMethod() {
                        return "GET";
                    }

                    @Override
                    public String getIp() {
                        return "127.0.0.1";
                    }

                    @Override
                    public boolean matchPath(String pattern, String path) {
                        return false;
                    }
                };
            }

            @Override
            public ContextResponse response() {
                return (name, value) -> {
                    // no-op: 不需要真实写入响应头
                };
            }

            @Override
            public ContextStore store() {
                return new ContextStore() {
                    @Override
                    public void set(String key, Object value) {
                        storeData.put(key, value);
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public <T> T get(String key) {
                        return (T) storeData.get(key);
                    }

                    @Override
                    public void remove(String key) {
                        storeData.remove(key);
                    }
                };
            }

            @Override
            public boolean enable() {
                return true;
            }
        });
    }

    @AfterEach
    public void tearDown() {
        SimpleTokens.clearThreadCache();
    }

    // ===== tracer bullet: login → getSession → getLoginId → checkLogin → logout =====

    @Test
    public void testLoginAndGetSession() {
        SessionInfo session = SimpleTokens.login("default", "user1");
        assertNotNull(session);
        assertEquals("user1", session.getLoginId());
        assertNotNull(session.getToken());

        // 可以通过 getSession() 获取当前会话
        SessionInfo current = SimpleTokens.getSession();
        assertEquals("user1", current.getLoginId());

        // 可以通过 getLoginId() 获取
        assertEquals("user1", SimpleTokens.getLoginId());

        // isLogin / checkLogin
        assertTrue(SimpleTokens.isLogin());
        SimpleTokens.checkLogin(); // 不抛异常

        // logout
        SimpleTokens.logout();
        SimpleTokens.clearThreadCache();

        // 登出后 isLogin 返回 false
        assertFalse(SimpleTokens.isLogin());

        // 登出后 getSession 抛异常
        assertThrows(InvalidTokenException.class, SimpleTokens::getSession);
    }

    @Test
    public void testLoginWithTimeout() {
        SessionInfo session = SimpleTokens.login("default", "user1", Duration.ofHours(1));
        assertNotNull(session);
        assertTrue(session.getExpiredTime() > session.getCreateTime());
        assertTrue(session.ttlByNow(java.util.concurrent.TimeUnit.MILLISECONDS) > 0);
    }

    @Test
    public void testLoginWithLongId() {
        SessionInfo session = SimpleTokens.login("default", 10001L);
        assertEquals("10001", session.getLoginId());
    }

    @Test
    public void testLoginWithLoginSetting() {
        LoginSetting setting = new LoginSetting();
        setting.setClient("mobile");
        setting.setTimeout(Duration.ofMinutes(30));
        SessionInfo session = SimpleTokens.login("default", "user1", setting);
        assertNotNull(session);
        assertEquals("user1", session.getLoginId());
    }

    @Test
    public void testLoginWithNullIdThrows() {
        assertThrows(SimpleTokenException.class, () -> SimpleTokens.login("default", (String) null));
        assertThrows(SimpleTokenException.class, () -> SimpleTokens.login("default", (Long) null));
    }

    @Test
    public void testGetLoginIdAsLong() {
        SimpleTokens.login("default", "12345");
        assertEquals(12345L, SimpleTokens.getLoginIdAsLong());
    }

    @Test
    public void testIsLoginByIdReturnsTrueWhenLoggedIn() {
        SimpleTokens.login("default", "user2");
        assertTrue(SimpleTokens.isLogin("user2"));
        assertFalse(SimpleTokens.isLogin("non-existent-user"));
    }

    @Test
    public void testGetSessionWithoutLoginThrows() {
        assertThrows(InvalidTokenException.class, SimpleTokens::getSession);
    }

    @Test
    public void testLogoutById() {
        SimpleTokens.login("default", "user1");
        assertTrue(SimpleTokens.isLogin("user1"));

        SimpleTokens.logout("user1");
        assertFalse(SimpleTokens.isLogin("user1"));
    }

    @Test
    public void testLogoutByIdAndClient() {
        SimpleTokens.login("default", "user1", new LoginSetting("web"));
        assertTrue(SimpleTokens.isLogin("user1"));

        SimpleTokens.logout("user1", "web");
        assertFalse(SimpleTokens.isLogin("user1"));
    }

    @Test
    public void testLogoutWithNullLongIdThrows() {
        assertThrows(SimpleTokenException.class, () -> SimpleTokens.logout((Long) null));
    }

    @Test
    public void testLogoutWithNullStringIdNoop() {
        // String overload silently returns (SessionLogout finds null person)
        assertDoesNotThrow(() -> SimpleTokens.logout((String) null));
    }

    @Test
    public void testKickById() {
        SimpleTokens.login("default", "user1");
        assertTrue(SimpleTokens.isLogin("user1"));

        SimpleTokens.kick("user1");
        assertFalse(SimpleTokens.isLogin("user1"));
    }

    @Test
    public void testKickByToken() {
        SessionInfo session = SimpleTokens.login("default", "user1");
        assertTrue(SimpleTokens.isLogin());

        SimpleTokens.kickByToken(session.getToken());
        SimpleTokens.clearThreadCache();
        assertFalse(SimpleTokens.isLogin());
    }

    @Test
    public void testKickWithNullLongIdThrows() {
        assertThrows(SimpleTokenException.class, () -> SimpleTokens.kick((Long) null));
    }

    @Test
    public void testKickWithNullStringIdNoop() {
        // String overload silently returns (SessionKick finds null person)
        assertDoesNotThrow(() -> SimpleTokens.kick((String) null));
    }

    @Test
    public void testRefreshSession() {
        SimpleTokens.login("default", "user1");
        SessionInfo before = SimpleTokens.getSession();
        long beforeExpired = before.getExpiredTime();

        SimpleTokens.refreshSession();
        SessionInfo after = SimpleTokens.getSession();
        assertTrue(after.getExpiredTime() >= beforeExpired,
                "refresh should extend or keep expired time");
    }

    @Test
    public void testLoginWithShareToken() {
        SimpleTokenLoginConfig loginConfig = new SimpleTokenLoginConfig();
        loginConfig.setShareToken(true);
        applyLoginConfig(loginConfig);

        SessionInfo s1 = SimpleTokens.login("default", "user1");
        String token1 = s1.getToken();

        SimpleTokens.clearThreadCache();

        SessionInfo s2 = SimpleTokens.login("default", "user1");
        assertEquals(token1, s2.getToken(), "share token should reuse the same token");
    }

    @Test
    public void testLoginWithMaxNumKickOutFirstCreate() {
        SimpleTokenLoginConfig loginConfig = new SimpleTokenLoginConfig();
        loginConfig.setMaxNum(2);
        loginConfig.setMaxStrategy(LoginMaxStrategy.KICK_OUT_FIRST_CREATE);
        applyLoginConfig(loginConfig);

        // 登录 3 次，第一次登录的应该被踢出
        SessionInfo s1 = SimpleTokens.login("default", "user1");
        SessionInfo s2 = SimpleTokens.login("default", "user1");
        SessionInfo s3 = SimpleTokens.login("default", "user1");

        // s1 应该被踢出（最早创建）
        assertNull(SessionManager.findSession(s1.getToken()), "first created should be kicked");
        assertNotNull(SessionManager.findSession(s3.getToken()), "latest should still be active");
    }

    @Test
    public void testLoginDisabledWhenMaxNumIsZero() {
        SimpleTokenLoginConfig loginConfig = new SimpleTokenLoginConfig();
        loginConfig.setMaxNum(0);
        applyLoginConfig(loginConfig);

        assertThrows(LoginRejectException.class, () -> SimpleTokens.login("default", "user1"));
    }

    @Test
    public void testLoginClientDisabledWhenMaxNumIsZero() {
        SimpleTokenLoginConfig loginConfig = new SimpleTokenLoginConfig();
        SimpleTokenLoginConfig.TokenClientConfig clientConfig = new SimpleTokenLoginConfig.TokenClientConfig();
        clientConfig.setMaxNum(0);
        loginConfig.getClients().put("blocked", clientConfig);
        applyLoginConfig(loginConfig);

        assertThrows(LoginRejectException.class, () ->
                SimpleTokens.login("default", "user1", new LoginSetting("blocked")));
    }

    @Test
    public void testLoginWithRejectStrategy() {
        SimpleTokenLoginConfig loginConfig = new SimpleTokenLoginConfig();
        SimpleTokenLoginConfig.TokenClientConfig clientConfig = new SimpleTokenLoginConfig.TokenClientConfig();
        clientConfig.setMaxNum(1);
        clientConfig.setMaxStrategy(LoginMaxStrategy.REJECT);
        loginConfig.getClients().put("web", clientConfig);
        applyLoginConfig(loginConfig);

        // 第一次登录成功
        SimpleTokens.login("default", "user1", new LoginSetting("web"));
        // 第二次应被拒绝
        assertThrows(LoginRejectException.class, () ->
                SimpleTokens.login("default", "user1", new LoginSetting("web")));
    }

    private static void applyLoginConfig(SimpleTokenLoginConfig loginConfig) {
        SimpleTokenConfig config = new SimpleTokenConfig();
        config.setLogin(loginConfig);
        config.setPath(new SimpleTokenPathConfig());
        SimpleTokens.setGlobalConfig(config);
    }

    @Test
    public void testIsLoginWithoutLoginReturnsFalse() {
        assertFalse(SimpleTokens.isLogin());
    }

    // ===== loginByAccount =====

    @Test
    public void testLoginByAccount() {
        SessionInfo session = SimpleTokens.loginByAccount("default", "admin", "admin");
        assertNotNull(session);
        assertEquals("1", session.getLoginId(),
                "UserAccount.userId=1 from mock UserQuery");
    }

    // ===== kick by id + client =====

    @Test
    public void testKickByIdAndClient() {
        SimpleTokens.login("default", "user1", new LoginSetting("web"));
        SimpleTokens.login("default", "user1", new LoginSetting("mobile"));
        assertTrue(SimpleTokens.isLogin("user1"));

        SimpleTokens.kick("user1", "web");

        // 检查 SessionPerson 中 "web" 客户端的 token 已清除
        SessionPerson person = SessionManager.findPerson("user1");
        assertNotNull(person);
        assertTrue(person.findAllTokens("web").isEmpty(),
                "web client tokens should be removed");
        assertFalse(person.findAllTokens("mobile").isEmpty(),
                "mobile client tokens should remain");
    }

    // ===== logout (current session) =====

    @Test
    public void testLogoutCurrentSessionRemovesToken() {
        SessionInfo session = SimpleTokens.login("default", "user1");
        assertTrue(SimpleTokens.isLogin());

        SimpleTokens.logout();
        SimpleTokens.clearThreadCache();

        assertFalse(SimpleTokens.isLogin());
        // token 对应的 session 应该被清除
        assertNull(SessionManager.findSession(session.getToken()));
    }

    // ===== SessionPerson client 隔离 =====

    @Test
    public void testLoginMultipleClientsCreatesSeparateSessions() {
        String loginId = "multi-client-user-" + System.currentTimeMillis();

        SessionInfo web = SimpleTokens.login("default", loginId, new LoginSetting("web"));
        SimpleTokens.clearThreadCache();
        SessionInfo mobile = SimpleTokens.login("default", loginId, new LoginSetting("mobile"));

        assertNotEquals(web.getToken(), mobile.getToken(),
                "different clients should get different tokens");

        SessionPerson person = SessionManager.findPerson(loginId);
        assertNotNull(person);
        assertEquals(2, person.findAllTokens().size(),
                "should have 2 tokens for 2 clients");
    }

    // ===== UserManager 异常路径 =====

    @Test
    public void testLoginByAccountWithWrongPassword() {
        assertThrows(LoginPasswordInvalidException.class, () ->
                SimpleTokens.loginByAccount("default", "admin", "wrong-password"));
    }

    @Test
    public void testLoginByAccountNotFound() {
        // 需要一个对所有 getAccountByName 都返回 null 的 UserQuery
        UserManager.registerQuery(new UserQuery() {
            @Override
            public UserInfo getInfoById(String loginId) {
                return new UserInfo(loginId);
            }

            @Override
            public UserAccount getAccountByName(String accountType, String accountName) {
                return null;
            }
        });
        assertThrows(LoginAccountNotFoundException.class, () ->
                SimpleTokens.loginByAccount("default", "ghost", "any"));
    }

    @Test
    public void testLoginWithDisabledUserThrows() {
        UserManager.registerQuery(new UserQuery() {
            @Override
            public UserInfo getInfoById(String loginId) {
                return new UserInfo(loginId, false);
            }

            @Override
            public UserAccount getAccountByName(String accountType, String accountName) {
                return null;
            }
        });
        assertThrows(dev.simpleframework.token.exception.LoginUserDisabledException.class, () ->
                SimpleTokens.login("default", "disabled-user"));
    }

    @Test
    public void testLoginWithUnknownUserThrows() {
        UserManager.registerQuery(new UserQuery() {
            @Override
            public UserInfo getInfoById(String loginId) {
                return null;
            }

            @Override
            public UserAccount getAccountByName(String accountType, String accountName) {
                return null;
            }
        });
        assertThrows(LoginUserNotFoundException.class, () ->
                SimpleTokens.login("default", "ghost"));
    }

    // ===== shareToken + session 过期 =====

    @Test
    public void testShareTokenWhenSharedSessionExpired() {
        SimpleTokenLoginConfig loginConfig = new SimpleTokenLoginConfig();
        loginConfig.setShareToken(true);
        applyLoginConfig(loginConfig);

        SessionInfo s1 = SimpleTokens.login("default", "share-user");
        String token = s1.getToken();

        // 手动删除 SessionInfo 模拟过期
        SessionManager.removeSessionByToken(token);
        SimpleTokens.clearThreadCache();

        // 再次登录，应清理脏引用并创建新 session
        SessionInfo s2 = SimpleTokens.login("default", "share-user");
        assertNotNull(s2);
        assertNotEquals(token, s2.getToken(), "should create new token when shared session is gone");
    }

}
