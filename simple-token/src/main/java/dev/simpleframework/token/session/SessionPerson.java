package dev.simpleframework.token.session;

import dev.simpleframework.token.config.SimpleTokenLoginConfig;
import dev.simpleframework.token.constant.LoginMaxStrategy;
import dev.simpleframework.token.exception.LoginRejectException;
import lombok.Data;

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
    private static final long serialVersionUID = 1L;

    private String loginId;
    private Map<String, List<Client>> clients;

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
        List<Client> clients = this.clients.get(client);
        if (clients == null) {
            clients = new ArrayList<>();
        }
        Client c = new Client();
        c.setToken(token);
        c.setCreateTime(createTime);
        c.setExpiredTime(expiredTime);
        clients.add(c);
        this.clients.put(client, clients);
    }

    /**
     * 清除过期的数据，延迟 0.5 秒避免网络抖动之类的问题导致缓存查无数据
     */
    public void removeExpired() {
        long now = System.currentTimeMillis() + 500;
        Map<String, List<String>> expired = new HashMap<>();
        this.clients.forEach((k, v) -> {
            List<String> expiredTokens = v.stream()
                    .filter(client -> client.getExpiredTime() <= now)
                    .map(Client::getToken)
                    .toList();
            expired.put(k, expiredTokens);
        });
        expired.forEach((client, tokens) -> {
            List<Client> clients = this.clients.get(client)
                    .stream()
                    .filter(a -> !tokens.contains(a.getToken()))
                    .collect(Collectors.toList());
            if (!clients.isEmpty()) {
                this.clients.put(client, clients);
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
        for (Map.Entry<String, List<Client>> entry : this.clients.entrySet()) {
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
        for (Map.Entry<String, List<Client>> entry : this.clients.entrySet()) {
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
                .map(Client::getToken)
                .distinct()
                .toList();
    }

    /**
     * 查询指定客户端的所有 token
     *
     * @return token 集
     */
    public List<String> findAllTokens(String client) {
        List<Client> clients = this.clients.get(client);
        if (clients == null) {
            return Collections.emptyList();
        }
        return clients.stream()
                .map(Client::getToken)
                .distinct()
                .toList();
    }

    /**
     * 获取最大过期时间
     */
    public Long findLastExpiredTime() {
        long expiredTime = 0L;
        for (List<Client> clientList : this.clients.values()) {
            for (Client client : clientList) {
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
        for (List<Client> clientList : this.clients.values()) {
            for (Client client : clientList) {
                if (client.getExpiredTime() >= expiredTime) {
                    expiredTime = client.getExpiredTime();
                    expiredToken = client.getToken();
                }
            }
        }
        return expiredToken;
    }

    /**
     * 根据配置清除应该淘汰的数据
     *
     * @param config        登录配置
     * @param currentClient 当前客户端
     * @param currentToken  当前 token
     * @return 应该淘汰的 token
     */
    public List<String> removeExpiredByConfig(SimpleTokenLoginConfig config, String currentClient, String currentToken) {
        Set<String> result = new HashSet<>();
        // 先淘汰同客户端的数据
        List<Client> clients = this.clients.get(currentClient);
        SimpleTokenLoginConfig.ClientConfig clientConfig = config.findClientConfig(currentClient);
        List<String> expiredTokens = findExpiredTokensByStrategy(clients, currentToken, clientConfig.getMaxStrategy(), clientConfig.getMaxNum());
        result.addAll(expiredTokens);
        this.removeTokens(expiredTokens);

        // 再淘汰所有客户端的数据
        clients = new ArrayList<>();
        for (List<Client> clientList : this.clients.values()) {
            clients.addAll(clientList);
        }
        expiredTokens = findExpiredTokensByStrategy(clients, currentToken, config.getMaxStrategy(), config.getMaxNum());
        result.addAll(expiredTokens);
        this.removeTokens(expiredTokens);
        return result.stream().toList();
    }

    private static List<String> findExpiredTokensByStrategy(List<Client> clients, String currentToken, LoginMaxStrategy strategy, int max) {
        if (clients == null || clients.isEmpty()) {
            return Collections.emptyList();
        }
        if (max < 0) {
            // 不限数量
            return Collections.emptyList();
        }
        if (max == 0) {
            // 不许登录
            throw new LoginRejectException("number limit");
        }
        if (max == 1) {
            List<String> result = new ArrayList<>();
            // 只许单个登陆：非当前登录的都设为过期
            clients.forEach(client -> {
                if (!currentToken.equals(client.getToken())) {
                    result.add(client.getToken());
                }
            });
            return result;
        }
        // 允许多地登录：超过允许的最大数时，根据策略查找数据
        int outNum = clients.size() - max;
        if (outNum <= 0) {
            return Collections.emptyList();
        }
        List<String> result = Collections.emptyList();
        if (strategy == LoginMaxStrategy.KICK_OUT_FIRST_CREATE) {
            result = clients.stream()
                    .sorted(Comparator.comparing(Client::getCreateTime))
                    .map(Client::getToken)
                    .filter(token -> !currentToken.equals(token))
                    .toList();
            if (outNum < result.size()) {
                result = result.subList(0, outNum);
            }
        } else if (strategy == LoginMaxStrategy.KICK_OUT_FIRST_EXPIRE) {
            result = clients.stream()
                    .sorted(Comparator.comparing(Client::getExpiredTime))
                    .map(Client::getToken)
                    .filter(token -> !currentToken.equals(token))
                    .toList();
            if (outNum < result.size()) {
                result = result.subList(0, outNum);
            }
        } else if (strategy == LoginMaxStrategy.KICK_OUT_ALL) {
            result = clients.stream()
                    .map(Client::getToken)
                    .filter(token -> !currentToken.equals(token))
                    .toList();
        } else if (strategy == LoginMaxStrategy.REJECT) {
            throw new LoginRejectException("number exceeded");
        }
        return result;
    }

    @Data
    public static class Client {
        private String token;
        private Long createTime;
        private Long expiredTime;
    }

}
