package dev.simpleframework.token.session;

import dev.simpleframework.token.config.SimpleTokenLoginConfig;
import dev.simpleframework.token.constant.LoginMaxStrategy;
import dev.simpleframework.token.exception.LoginRejectException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class SimpleTokenPersonTest {
    public static final long now = System.currentTimeMillis();

    private static SessionPerson mock() {
        SessionPerson person = new SessionPerson();
        person.addClient("1", UUID.randomUUID().toString(), now, now);
        person.addClient("1", UUID.randomUUID().toString(), now, now - 1000);
        person.addClient("1", UUID.randomUUID().toString(), now, now + 3000);
        person.addClient("2", UUID.randomUUID().toString(), now, now - 2000);
        person.addClient("2", UUID.randomUUID().toString(), now, now + 1000);
        person.addClient("2", UUID.randomUUID().toString(), now, now + 2000);
        person.addClient("1", UUID.randomUUID().toString(), now, now - 3000);
        return person;
    }

    @Test
    public void removeExpired() {
        SessionPerson person = mock();
        Assertions.assertEquals(person.getClients().get("1").size(), 4);
        Assertions.assertEquals(person.getClients().get("2").size(), 3);
        person.removeExpired();
        Assertions.assertEquals(person.getClients().get("1").size(), 1);
        Assertions.assertEquals(person.getClients().get("2").size(), 2);
    }

    @Test
    public void removeExpiredAllTokensExpired() {
        long now = System.currentTimeMillis();
        SessionPerson person = new SessionPerson();
        person.addClient("1", UUID.randomUUID().toString(), now, now - 5000);
        person.addClient("1", UUID.randomUUID().toString(), now, now - 3000);
        person.addClient("2", UUID.randomUUID().toString(), now, now + 5000);
        Assertions.assertEquals(person.getClients().size(), 2);

        person.removeExpired();
        Assertions.assertNull(person.getClients().get("1"), "client with all expired tokens should be removed");
        Assertions.assertEquals(person.getClients().size(), 1);
        Assertions.assertEquals(person.getClients().get("2").size(), 1);
    }

    @Test
    public void removeTokens() {
        long now = System.currentTimeMillis();
        SessionPerson person = new SessionPerson();
        String token1 = UUID.randomUUID().toString();
        String token2 = UUID.randomUUID().toString();
        person.addClient("1", token1, now, now);
        person.addClient("1", UUID.randomUUID().toString(), now, now - 3000);
        person.addClient("2", UUID.randomUUID().toString(), now, now - 2000);
        person.addClient("2", token2, now, now + 1000);
        person.addClient("2", UUID.randomUUID().toString(), now, now + 2000);
        person.addClient("1", token2, now, now + 3000);
        person.addClient("3", token1, now, now + 2000);
        person.addClient("3", token2, now, now + 3000);

        Assertions.assertEquals(person.getClients().get("1").size(), 3);
        Assertions.assertEquals(person.getClients().get("2").size(), 3);
        Assertions.assertEquals(person.getClients().get("3").size(), 2);
        person.removeTokens(List.of(token1, token2));
        Assertions.assertEquals(person.getClients().get("1").size(), 1);
        Assertions.assertEquals(person.getClients().get("2").size(), 2);
        Assertions.assertNull(person.getClients().get("3"));
    }

    @Test
    public void findLastExpiredTime() {
        SessionPerson person = mock();
        Assertions.assertEquals(person.findLastExpiredTime(), now + 3000);
    }

    @Test
    public void findLastExpiredToken() {
        SessionPerson person = mock();
        Assertions.assertEquals(person.findLastExpiredToken(), person.getClients().get("1").get(2).getToken());
    }

    // ========== removeExpiredByConfig tests ==========

    @Test
    public void removeExpiredByConfig_kickOutFirstCreate_clientQuota() {
        long now = System.currentTimeMillis();
        SessionPerson person = new SessionPerson("user1");
        String token1 = UUID.randomUUID().toString();
        String token2 = UUID.randomUUID().toString();
        String token3 = UUID.randomUUID().toString();
        // 按创建时间排序: token1(now), token2(now+1), token3(now+2)
        person.addClient("web", token1, now, now + 10000);
        person.addClient("web", token2, now + 1, now + 9000);
        person.addClient("web", token3, now + 2, now + 8000); // current

        SimpleTokenLoginConfig config = new SimpleTokenLoginConfig();
        config.setMaxNum(-1); // 全局不限
        SimpleTokenLoginConfig.TokenClientConfig clientConfig = new SimpleTokenLoginConfig.TokenClientConfig();
        clientConfig.setMaxNum(2);
        clientConfig.setMaxStrategy(LoginMaxStrategy.KICK_OUT_FIRST_CREATE);
        config.getClients().put("web", clientConfig);

        List<String> removed = person.removeExpiredByConfig(config, "web", token3);
        // client quota=2，3个token需淘汰1个。KICK_OUT_FIRST_CREATE淘汰最早创建的token1
        Assertions.assertEquals(1, removed.size());
        Assertions.assertTrue(removed.contains(token1));
        Assertions.assertFalse(removed.contains(token3), "currentToken should not be removed");
    }

    @Test
    public void removeExpiredByConfig_kickOutFirstExpire_clientQuota() {
        long now = System.currentTimeMillis();
        SessionPerson person = new SessionPerson("user1");
        String token1 = UUID.randomUUID().toString();
        String token2 = UUID.randomUUID().toString();
        String token3 = UUID.randomUUID().toString();
        // 按过期时间排序: token3(8000最早), token2(9000), token1(10000最晚)
        // KICK_OUT_FIRST_EXPIRE: 淘汰最早过期的非当前token → 应淘汰token2
        person.addClient("web", token1, now, now + 10000);
        person.addClient("web", token2, now, now + 9000);
        person.addClient("web", token3, now, now + 8000); // current (8000最早但受保护)

        SimpleTokenLoginConfig config = new SimpleTokenLoginConfig();
        config.setMaxNum(-1);
        SimpleTokenLoginConfig.TokenClientConfig clientConfig = new SimpleTokenLoginConfig.TokenClientConfig();
        clientConfig.setMaxNum(2);
        clientConfig.setMaxStrategy(LoginMaxStrategy.KICK_OUT_FIRST_EXPIRE);
        config.getClients().put("web", clientConfig);

        List<String> removed = person.removeExpiredByConfig(config, "web", token3);
        // 业务规则：淘汰最早过期的非当前token
        Assertions.assertEquals(1, removed.size());
        Assertions.assertTrue(removed.contains(token2), "should remove the earliest expiring non-current token (token2)");
        Assertions.assertFalse(removed.contains(token1), "token1 expires latest, should not be removed");
        Assertions.assertFalse(removed.contains(token3), "currentToken should not be removed");
    }

    @Test
    public void removeExpiredByConfig_kickOutAll_clientQuota() {
        long now = System.currentTimeMillis();
        SessionPerson person = new SessionPerson("user1");
        String token1 = UUID.randomUUID().toString();
        String token2 = UUID.randomUUID().toString();
        String token3 = UUID.randomUUID().toString(); // current
        person.addClient("web", token1, now, now + 10000);
        person.addClient("web", token2, now, now + 9000);
        person.addClient("web", token3, now, now + 8000);

        SimpleTokenLoginConfig config = new SimpleTokenLoginConfig();
        config.setMaxNum(-1);
        SimpleTokenLoginConfig.TokenClientConfig clientConfig = new SimpleTokenLoginConfig.TokenClientConfig();
        clientConfig.setMaxNum(1); // 只保留1个
        clientConfig.setMaxStrategy(LoginMaxStrategy.KICK_OUT_ALL);
        config.getClients().put("web", clientConfig);

        List<String> removed = person.removeExpiredByConfig(config, "web", token3);
        // quota=1，3个token需淘汰2个。KICK_OUT_ALL淘汰所有非当前token
        Assertions.assertEquals(2, removed.size());
        Assertions.assertTrue(removed.contains(token1));
        Assertions.assertTrue(removed.contains(token2));
        Assertions.assertFalse(removed.contains(token3), "currentToken should not be removed");
    }

    @Test
    public void removeExpiredByConfig_globalQuotaAfterClientQuota() {
        long now = System.currentTimeMillis();
        SessionPerson person = new SessionPerson("user1");
        String token1 = UUID.randomUUID().toString();
        String token2 = UUID.randomUUID().toString();
        String token3 = UUID.randomUUID().toString(); // current
        person.addClient("web", token1, now, now + 10000);
        person.addClient("web", token2, now, now + 9000);
        person.addClient("app", token3, now, now + 8000);

        SimpleTokenLoginConfig config = new SimpleTokenLoginConfig();
        config.setMaxNum(1); // 全局：总共只保留1个
        config.setMaxStrategy(LoginMaxStrategy.KICK_OUT_FIRST_CREATE);
        // 客户端不限
        SimpleTokenLoginConfig.TokenClientConfig clientConfig = new SimpleTokenLoginConfig.TokenClientConfig();
        clientConfig.setMaxNum(-1);
        config.getClients().put("app", clientConfig);

        List<String> removed = person.removeExpiredByConfig(config, "app", token3);
        // 全局 quota=1，总共3个token需淘汰2个，按创建时间最早的先淘汰
        Assertions.assertEquals(2, removed.size());
        Assertions.assertFalse(removed.contains(token3), "currentToken should not be removed");
    }

    @Test
    public void removeExpiredByConfig_noLimit_whenMaxIsNegative() {
        long now = System.currentTimeMillis();
        SessionPerson person = new SessionPerson("user1");
        String token = UUID.randomUUID().toString();
        person.addClient("web", token, now, now + 10000);

        SimpleTokenLoginConfig config = new SimpleTokenLoginConfig();
        config.setMaxNum(-1); // 全局不限
        SimpleTokenLoginConfig.TokenClientConfig clientConfig = new SimpleTokenLoginConfig.TokenClientConfig();
        clientConfig.setMaxNum(-1); // 客户端不限
        config.getClients().put("web", clientConfig);

        List<String> removed = person.removeExpiredByConfig(config, "web", token);
        Assertions.assertTrue(removed.isEmpty());
    }

    @Test
    public void removeExpiredByConfig_maxZero_noEvictionAtThisLayer() {
        // 业务规则：maxNum=0 的含义是"禁止登录"，此检查在 SessionLogin.exec() 入口层完成
        // removeExpiredByConfig 只负责"超出上限时淘汰"；maxNum=0 不触发淘汰逻辑
        long now = System.currentTimeMillis();
        SessionPerson person = new SessionPerson("user1");
        String token = UUID.randomUUID().toString();
        person.addClient("web", token, now, now + 10000);

        SimpleTokenLoginConfig config = new SimpleTokenLoginConfig();
        config.setMaxNum(0);
        SimpleTokenLoginConfig.TokenClientConfig clientConfig = new SimpleTokenLoginConfig.TokenClientConfig();
        clientConfig.setMaxNum(0);
        config.getClients().put("web", clientConfig);

        List<String> removed = person.removeExpiredByConfig(config, "web", token);
        Assertions.assertTrue(removed.isEmpty(),
                "maxNum=0 means no login allowed, no eviction needed at this layer");
    }

    @Test
    public void removeExpiredByConfig_notExceedMax() {
        long now = System.currentTimeMillis();
        SessionPerson person = new SessionPerson("user1");
        String token = UUID.randomUUID().toString();
        person.addClient("web", token, now, now + 10000);

        SimpleTokenLoginConfig config = new SimpleTokenLoginConfig();
        config.setMaxNum(3); // 全局上限3
        SimpleTokenLoginConfig.TokenClientConfig clientConfig = new SimpleTokenLoginConfig.TokenClientConfig();
        clientConfig.setMaxNum(2); // 客户端上限2
        config.getClients().put("web", clientConfig);

        List<String> removed = person.removeExpiredByConfig(config, "web", token);
        // 只有1个token，不超限
        Assertions.assertTrue(removed.isEmpty());
    }

    @Test
    public void removeExpiredByConfig_returnsDistinctTokens() {
        long now = System.currentTimeMillis();
        SessionPerson person = new SessionPerson("user1");
        String token1 = UUID.randomUUID().toString();
        String token2 = UUID.randomUUID().toString();
        // 同一token出现在两个客户端
        person.addClient("web", token1, now, now + 10000);
        person.addClient("app", token1, now, now + 10000);
        person.addClient("web", token2, now, now + 9000);

        SimpleTokenLoginConfig config = new SimpleTokenLoginConfig();
        config.setMaxNum(1);
        config.setMaxStrategy(LoginMaxStrategy.KICK_OUT_ALL);

        List<String> removed = person.removeExpiredByConfig(config, "web", token2);
        // 结果应去重
        long token1Count = removed.stream().filter(t -> t.equals(token1)).count();
        Assertions.assertEquals(1, token1Count, "duplicate tokens should be deduplicated via Set");
    }

    // ========== findTokensToRemove logic tests ==========

    @Test
    public void findTokensToRemove_emptyClients_returnsEmpty() {
        long now = System.currentTimeMillis();
        SessionPerson person = new SessionPerson("user1");
        String token = UUID.randomUUID().toString();
        person.addClient("web", token, now, now + 10000);

        SimpleTokenLoginConfig config = new SimpleTokenLoginConfig();
        config.setMaxNum(1);
        // "app"客户端在clients中不存在
        List<String> removed = person.removeExpiredByConfig(config, "app", token);
        Assertions.assertTrue(removed.isEmpty());
    }

    @Test
    public void removeExpiredByConfig_currentTokenProtectedInGlobalQuota() {
        long now = System.currentTimeMillis();
        SessionPerson person = new SessionPerson("user1");
        String token1 = UUID.randomUUID().toString();
        String token2 = UUID.randomUUID().toString(); // current
        person.addClient("web", token1, now, now + 5000);
        person.addClient("app", token2, now, now + 10000);

        SimpleTokenLoginConfig config = new SimpleTokenLoginConfig();
        config.setMaxNum(1);
        config.setMaxStrategy(LoginMaxStrategy.KICK_OUT_FIRST_CREATE);

        List<String> removed = person.removeExpiredByConfig(config, "app", token2);
        Assertions.assertFalse(removed.contains(token2), "currentToken must not be removed in global quota phase");
    }

    // ========== findAllTokens tests ==========

    @Test
    public void findAllTokens_returnsAllDistinct() {
        long now = System.currentTimeMillis();
        SessionPerson person = new SessionPerson("user1");
        String sharedToken = UUID.randomUUID().toString();
        person.addClient("web", sharedToken, now, now + 10000);
        person.addClient("app", sharedToken, now, now + 10000);
        person.addClient("mobile", UUID.randomUUID().toString(), now, now + 5000);

        List<String> tokens = person.findAllTokens();
        // sharedToken 出现两次，但 findAllTokens 做了 distinct
        Assertions.assertEquals(2, tokens.size());
    }

    @Test
    public void findAllTokens_emptyPerson_returnsEmpty() {
        SessionPerson person = new SessionPerson("user1");
        List<String> tokens = person.findAllTokens();
        Assertions.assertTrue(tokens.isEmpty());
    }

    @Test
    public void findAllTokens_byClient() {
        long now = System.currentTimeMillis();
        SessionPerson person = new SessionPerson("user1");
        person.addClient("web", UUID.randomUUID().toString(), now, now + 10000);
        person.addClient("web", UUID.randomUUID().toString(), now, now + 5000);
        person.addClient("app", UUID.randomUUID().toString(), now, now + 8000);

        List<String> webTokens = person.findAllTokens("web");
        Assertions.assertEquals(2, webTokens.size());
    }

    @Test
    public void findAllTokens_unknownClient_returnsEmpty() {
        SessionPerson person = new SessionPerson("user1");
        List<String> tokens = person.findAllTokens("unknown");
        Assertions.assertTrue(tokens.isEmpty());
    }

    // ========== updateTokenExpiredTime tests ==========

    @Test
    public void updateTokenExpiredTime_updatesMatchingToken() {
        long now = System.currentTimeMillis();
        SessionPerson person = new SessionPerson("user1");
        String targetToken = UUID.randomUUID().toString();
        String otherToken = UUID.randomUUID().toString();
        person.addClient("web", targetToken, now, now + 10000);
        person.addClient("web", otherToken, now, now + 5000);

        long newExpiredTime = now + 99999;
        person.updateTokenExpiredTime(targetToken, newExpiredTime);

        // 验证 targetToken 的过期时间已更新
        boolean found = false;
        for (SessionPerson.TokenClient client : person.getClients().get("web")) {
            if (client.getToken().equals(targetToken)) {
                Assertions.assertEquals(newExpiredTime, (long) client.getExpiredTime());
                found = true;
            }
            if (client.getToken().equals(otherToken)) {
                Assertions.assertEquals(now + 5000, (long) client.getExpiredTime(), "other tokens should not be affected");
            }
        }
        Assertions.assertTrue(found, "target token should be found");
    }

    @Test
    public void updateTokenExpiredTime_tokenNotFound_doesNothing() {
        long now = System.currentTimeMillis();
        SessionPerson person = new SessionPerson("user1");
        person.addClient("web", UUID.randomUUID().toString(), now, now + 10000);

        // 不应抛异常
        person.updateTokenExpiredTime("non-existent-token", now + 99999);
    }

    // ========== removeToken null safety ==========

    @Test
    public void removeToken_null_doesNothing() {
        SessionPerson person = new SessionPerson("user1");
        person.removeToken(null);
        // 不抛异常
    }

    @Test
    public void addClient_duplicateToken_shouldBeIdempotent() {
        // 业务规则：同一客户端不应存在重复的token记录
        // 每次登录生成唯一 token（UUID），shareToken=true 时复用已有 token 且不调用 addClient
        // 因此重复 token 在正常流程中不应出现；如果出现，addClient 应当幂等（替换而非新增）
        long now = System.currentTimeMillis();
        SessionPerson person = new SessionPerson("user1");
        String token = UUID.randomUUID().toString();
        person.addClient("web", token, now, now + 10000);
        person.addClient("web", token, now + 1000, now + 20000);

        List<SessionPerson.TokenClient> clients = person.getClients().get("web");
        // 正确行为：重复添加同一token应替换旧记录（幂等），clients.size() == 1
        // 当前代码允许重复添加 → 此断言预期失败
        Assertions.assertEquals(1, clients.size(),
                "BUG: addClient should be idempotent for duplicate tokens, but currently adds duplicates");
    }

    // ========== findLastExpiredTime/Token edge cases ==========

    @Test
    public void findLastExpiredTime_emptyPerson_returnsZero() {
        SessionPerson person = new SessionPerson("user1");
        Assertions.assertEquals(0L, (long) person.findLastExpiredTime());
    }

    @Test
    public void findLastExpiredToken_emptyPerson_returnsNull() {
        SessionPerson person = new SessionPerson("user1");
        Assertions.assertNull(person.findLastExpiredToken());
    }

    @Test
    public void removeExpiredByConfig_globalMaxZero_noEviction() {
        // 业务规则：全局 maxNum=0 禁止登录（SessionLogin入口层已拦截）
        // 即使客户端不限制，全局禁止时不触发淘汰
        long now = System.currentTimeMillis();
        SessionPerson person = new SessionPerson("user1");
        String token = UUID.randomUUID().toString();
        person.addClient("web", token, now, now + 10000);

        SimpleTokenLoginConfig config = new SimpleTokenLoginConfig();
        config.setMaxNum(0);
        SimpleTokenLoginConfig.TokenClientConfig clientConfig = new SimpleTokenLoginConfig.TokenClientConfig();
        clientConfig.setMaxNum(-1);
        config.getClients().put("web", clientConfig);

        List<String> removed = person.removeExpiredByConfig(config, "web", token);
        Assertions.assertTrue(removed.isEmpty(),
                "global maxNum=0 means login is forbidden, no eviction at this layer");
    }

    @Test
    public void removeExpiredByConfig_rejectStrategy_throwsException() {
        long now = System.currentTimeMillis();
        SessionPerson person = new SessionPerson("user1");
        String token1 = UUID.randomUUID().toString();
        String token2 = UUID.randomUUID().toString();
        String token3 = UUID.randomUUID().toString(); // current
        person.addClient("web", token1, now, now + 10000);
        person.addClient("web", token2, now, now + 9000);
        person.addClient("web", token3, now, now + 8000);

        SimpleTokenLoginConfig config = new SimpleTokenLoginConfig();
        config.setMaxNum(-1);
        SimpleTokenLoginConfig.TokenClientConfig clientConfig = new SimpleTokenLoginConfig.TokenClientConfig();
        clientConfig.setMaxNum(1); // 超过上限
        clientConfig.setMaxStrategy(LoginMaxStrategy.REJECT);
        config.getClients().put("web", clientConfig);

        Assertions.assertThrows(LoginRejectException.class, () ->
                person.removeExpiredByConfig(config, "web", token3));
    }
}
