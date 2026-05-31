package dev.simpleframework.token;

import dev.simpleframework.token.config.SimpleTokenConfig;
import dev.simpleframework.token.config.SimpleTokenLoginConfig;
import dev.simpleframework.token.config.SimpleTokenPathConfig;
import dev.simpleframework.token.session.SessionInfo;
import dev.simpleframework.token.session.SessionManager;
import dev.simpleframework.token.session.SessionPerson;
import dev.simpleframework.token.session.impl.DefaultSessionStore;
import dev.simpleframework.token.user.UserInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SessionStore 存储 + SessionManager CRUD 测试（无反射）
 */
public class SessionStoreTest {

    private DefaultSessionStore store;
    private long now;

    @BeforeEach
    public void setUp() {
        if (SimpleTokens.getGlobalConfig() == null) {
            SimpleTokenConfig config = new SimpleTokenConfig();
            config.setSessionName("test");
            config.setLogin(new SimpleTokenLoginConfig());
            config.setPath(new SimpleTokenPathConfig());
            SimpleTokens.setGlobalConfig(config);
        }
        store = new DefaultSessionStore();
        now = System.currentTimeMillis();
    }

    // ===== DefaultSessionStore setSession / getSession / removeSession =====

    @Test
    public void testSetAndGetSession() {
        SessionInfo session = new SessionInfo();
        session.setToken("test-token");
        session.setLoginId("user1");
        session.setExpiredTime(now + 60000);
        store.setSession(session);

        SessionInfo result = store.getSession("test-token");
        assertNotNull(result);
        assertEquals("user1", result.getLoginId());
        assertEquals("test-token", result.getToken());
    }

    @Test
    public void testGetSessionNotFoundReturnsNull() {
        assertNull(store.getSession("non-existent"));
    }

    @Test
    public void testRemoveSession() {
        SessionInfo session = new SessionInfo();
        session.setToken("to-remove");
        session.setLoginId("user1");
        session.setExpiredTime(now + 60000);
        store.setSession(session);

        store.removeSession("to-remove");
        assertNull(store.getSession("to-remove"));
    }

    @Test
    public void testGetSessionExpiredCleansUp() {
        SessionInfo expired = new SessionInfo();
        expired.setToken("expired-token");
        expired.setLoginId("user1");
        expired.setExpiredTime(now - 1000); // 已过期
        store.setSession(expired);

        SessionInfo result = store.getSession("expired-token");
        assertNull(result, "expired session should be cleaned up on get");
    }

    // ===== DefaultSessionStore setPerson / getPerson / removePerson =====

    @Test
    public void testSetAndGetPerson() {
        SessionPerson person = new SessionPerson("user1");
        person.addClient("web", "token1", now, now + 60000);
        store.setPerson(person);

        SessionPerson result = store.getPerson("user1");
        assertNotNull(result);
        assertEquals("user1", result.getLoginId());
    }

    @Test
    public void testRemovePerson() {
        SessionPerson person = new SessionPerson("user1");
        person.addClient("web", "token1", now, now + 60000);
        store.setPerson(person);

        store.removePerson("user1");
        assertNull(store.getPerson("user1"));
    }

    @Test
    public void testGetPersonExpiredCleansUp() {
        SessionPerson person = new SessionPerson("user1");
        person.addClient("web", "expired-token", now, now - 10000);
        store.setPerson(person);

        SessionPerson result = store.getPerson("user1");
        assertNull(result, "person with all tokens expired should be cleaned up");
    }

    @Test
    public void testSetPersonAllTokensExpiredRemoves() {
        SessionPerson person = new SessionPerson("user1");
        person.addClient("web", "token1", now, now - 5000);
        person.addClient("web", "token2", now, now - 3000);

        store.setPerson(person);
        assertNull(store.getPerson("user1"));
    }

    // ===== SessionManager null 安全 =====

    @Test
    public void testFindSessionNullReturnsNull() {
        assertNull(SessionManager.findSession(null));
        assertNull(SessionManager.findSession(""));
    }

    @Test
    public void testFindPersonNullReturnsNull() {
        assertNull(SessionManager.findPerson(null));
    }

    @Test
    public void testStoreSessionNullNoop() {
        assertDoesNotThrow(() -> SessionManager.storeSession(null));
    }

    @Test
    public void testStorePersonNullNoop() {
        assertDoesNotThrow(() -> SessionManager.storePerson(null));
    }

    @Test
    public void testRemoveSessionByTokenNullNoop() {
        assertDoesNotThrow(() -> SessionManager.removeSessionByToken((String) null));
        assertDoesNotThrow(() -> SessionManager.removeSessionByToken((java.util.Collection<String>) null));
    }

    @Test
    public void testRemovePersonNullNoop() {
        assertDoesNotThrow(() -> SessionManager.removePerson(null));
    }

    // ===== SessionManager createSession =====

    @Test
    public void testCreateSessionGeneratesTokenAndAttrs() {
        UserInfo user = new UserInfo("user1");
        SessionInfo session = SessionManager.createSession(user, now, now + 60000);
        assertNotNull(session);
        assertEquals("user1", session.getLoginId());
        assertEquals(now, session.getCreateTime());
        assertEquals(now + 60000, session.getExpiredTime());
        assertNotNull(session.getToken());
        assertFalse(session.getToken().isBlank());
        assertNotNull(session.getAttrs());
        assertTrue(session.getAttrs().isEmpty(),
                "DefaultSessionGenerator returns empty attrs");
    }

    // ===== SessionManager 端到端 =====

    @Test
    public void testStoreAndFindSession() {
        SessionInfo session = new SessionInfo();
        session.setToken("e2e-token-" + UUID.randomUUID());
        session.setLoginId("user1");
        session.setExpiredTime(now + 60000);
        SessionManager.storeSession(session);

        SessionInfo result = SessionManager.findSession(session.getToken());
        assertNotNull(result);
        assertEquals("user1", result.getLoginId());
    }

    @Test
    public void testStoreAndFindPerson() {
        SessionPerson person = new SessionPerson("user1");
        person.addClient("web", "token1", now, now + 60000);
        SessionManager.storePerson(person);

        SessionPerson result = SessionManager.findPerson("user1");
        assertNotNull(result);
        assertEquals("user1", result.getLoginId());
    }

}
