package dev.simpleframework.token.session;

import dev.simpleframework.token.config.SimpleTokenLoginConfig;
import dev.simpleframework.token.constant.LoginMaxStrategy;
import dev.simpleframework.token.exception.LoginRejectException;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户的所有会话值
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class SessionPerson implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String loginId;
    private Map<String, List<TokenClient>> clients;

    public SessionPerson() {
        this.clients = new HashMap<>();
    }

    public SessionPerson(String loginId) {
        this.loginId = loginId;
        this.clients = new HashMap<>();
    }

    /**
     * 添加客户端信息
     */
    public void addClient(String client, String token, Long createTime, Long expiredTime) {
        List<TokenClient> clients = this.clients.computeIfAbsent(client, k -> new ArrayList<>());
        clients.removeIf(c -> token.equals(c.getToken()));
        TokenClient c = new TokenClient();
        c.setToken(token);
        c.setCreateTime(createTime);
        c.setExpiredTime(expiredTime);
        clients.add(c);
    }

    /**
     * 清除过期的数据
     */
    public void removeExpired() {
        long now = System.currentTimeMillis();
        Map<String, List<String>> expired = new HashMap<>();
        this.clients.forEach((k, v) -> {
            List<String> expiredTokens = v.stream()
                    .filter(client -> client.getExpiredTime() < now)
                    .map(TokenClient::getToken)
                    .toList();
            expired.put(k, expiredTokens);
        });
        expired.forEach((client, tokens) -> {
            List<TokenClient> clients = this.clients.get(client)
                    .stream()
                    .filter(a -> !tokens.contains(a.getToken()))
                    .collect(Collectors.toList());
            if (!clients.isEmpty()) {
                this.clients.put(client, clients);
            } else {
                this.clients.remove(client);
            }
        });
    }

    /**
     * 清除 token 对应的数据
     */
    public void removeToken(String token) {
        if (token == null) {
            return;
        }
        for (Map.Entry<String, List<TokenClient>> entry : this.clients.entrySet()) {
            entry.getValue().removeIf(client -> token.equals(client.getToken()));
        }
        List<String> emptyKeys = new ArrayList<>();
        this.clients.forEach((k, v) -> {
            if (v.isEmpty()) {
                emptyKeys.add(k);
            }
        });
        emptyKeys.forEach(k -> this.clients.remove(k));
    }

    /**
     * 清除 token 对应的数据
     */
    public void removeTokens(Collection<String> tokens) {
        for (Map.Entry<String, List<TokenClient>> entry : this.clients.entrySet()) {
            entry.getValue().removeIf(client -> tokens.contains(client.getToken()));
        }
        List<String> emptyKeys = new ArrayList<>();
        this.clients.forEach((k, v) -> {
            if (v.isEmpty()) {
                emptyKeys.add(k);
            }
        });
        emptyKeys.forEach(k -> this.clients.remove(k));
    }

    /**
     * 查询所有 token
     *
     * @return token 集
     */
    public List<String> findAllTokens() {
        return this.clients.values().stream()
                .flatMap(Collection::stream)
                .map(TokenClient::getToken)
                .distinct()
                .toList();
    }

    /**
     * 查询指定客户端的所有 token
     *
     * @return token 集
     */
    public List<String> findAllTokens(String client) {
        List<TokenClient> clients = this.clients.get(client);
        if (clients == null) {
            return Collections.emptyList();
        }
        return clients.stream()
                .map(TokenClient::getToken)
                .distinct()
                .toList();
    }

    /**
     * 获取最大过期时间
     */
    public Long findLastExpiredTime() {
        long expiredTime = 0L;
        for (List<TokenClient> clientList : this.clients.values()) {
            for (TokenClient client : clientList) {
                if (client.getExpiredTime() >= expiredTime) {
                    expiredTime = client.getExpiredTime();
                }
            }
        }
        return expiredTime;
    }

    /**
     * 获取最大过期时间对应的 token
     */
    public String findLastExpiredToken() {
        String expiredToken = null;
        long expiredTime = 0L;
        for (List<TokenClient> clientList : this.clients.values()) {
            for (TokenClient client : clientList) {
                if (client.getExpiredTime() >= expiredTime) {
                    expiredTime = client.getExpiredTime();
                    expiredToken = client.getToken();
                }
            }
        }
        return expiredToken;
    }

    /**
     * 按配置淘汰超出上限的会话，用于登录时的并发控制。
     *
     * <p>分两个阶段执行：
     * <ol>
     * <li>客户端配额：在同客户端（{@code currentClient}）内按客户端配置淘汰，
     *     确保该客户端 token 数 ≤ clientMax。</li>
     * <li>全局配额：在阶段 1 清理后的所有客户端 token 上按全局配置淘汰，
     *     确保总数 ≤ globalMax。全局上限是最终约束，可覆盖客户端配额的结果。</li>
     * </ol>
     * {@code maxNum <= 0} 时跳过对应阶段（≤ 0 的检查已在 {@code SessionLogin} 入口完成）。
     * 当前登录的 token 不会被淘汰。
     *
     * @param config        登录配置
     * @param currentClient 当前登录的客户端
     * @param currentToken  当前登录的 token
     * @return 本次淘汰的 token 列表（用于后续清除对应的 SessionInfo）
     */
    public List<String> removeExpiredByConfig(SimpleTokenLoginConfig config, String currentClient, String currentToken) {
        Set<String> result = new HashSet<>();

        // 阶段 1：客户端配额
        SimpleTokenLoginConfig.TokenClientConfig clientConfig = config.findClientConfig(currentClient);
        int clientMax = clientConfig.getMaxNum();
        if (clientMax > 0) {
            List<TokenClient> clients = this.clients.get(currentClient);
            List<String> toRemove = findTokensToRemove(clients, currentToken, clientMax, clientConfig.getMaxStrategy());
            result.addAll(toRemove);
            this.removeTokens(toRemove);
        }

        // 阶段 2：全局配额
        int globalMax = config.getMaxNum();
        if (globalMax > 0) {
            List<TokenClient> clients = new ArrayList<>();
            for (List<TokenClient> clientList : this.clients.values()) {
                clients.addAll(clientList);
            }
            List<String> toRemove = findTokensToRemove(clients, currentToken, globalMax, config.getMaxStrategy());
            result.addAll(toRemove);
            this.removeTokens(toRemove);
        }
        return result.stream().toList();
    }

    private static List<String> findTokensToRemove(List<TokenClient> clients, String currentToken, int max, LoginMaxStrategy strategy) {
        if (clients == null || clients.isEmpty()) {
            return Collections.emptyList();
        }
        int outNum = clients.size() - max;
        if (outNum <= 0) {
            return Collections.emptyList();
        }
        List<String> result;
        if (strategy == LoginMaxStrategy.KICK_OUT_FIRST_CREATE) {
            result = clients.stream()
                    .sorted(Comparator.comparing(TokenClient::getCreateTime))
                    .map(TokenClient::getToken)
                    .filter(token -> !currentToken.equals(token))
                    .toList();
        } else if (strategy == LoginMaxStrategy.KICK_OUT_FIRST_EXPIRE) {
            result = clients.stream()
                    .sorted(Comparator.comparing(TokenClient::getExpiredTime))
                    .map(TokenClient::getToken)
                    .filter(token -> !currentToken.equals(token))
                    .toList();
        } else if (strategy == LoginMaxStrategy.KICK_OUT_ALL) {
            result = clients.stream()
                    .map(TokenClient::getToken)
                    .filter(token -> !currentToken.equals(token))
                    .toList();
        } else {
            throw new LoginRejectException("Maximum number of logins reached");
        }
        if (outNum < result.size()) {
            result = result.subList(0, outNum);
        }
        return result;
    }

    public void updateTokenExpiredTime(String token, long expiredTime) {
        for (List<TokenClient> clientList : this.clients.values()) {
            for (TokenClient client : clientList) {
                if (client.getToken().equals(token)) {
                    client.setExpiredTime(expiredTime);
                }
            }
        }
    }

    @Data
    public static class TokenClient implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        private String token;
        private Long createTime;
        private Long expiredTime;
    }

}
