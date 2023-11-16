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
    private Map<String, List<App>> apps;

    public SessionPerson() {
        this.apps = new HashMap<>();
    }

    public SessionPerson(String loginId) {
        this.loginId = loginId;
        this.apps = new HashMap<>();
    }

    /**
     * 添加应用信息
     */
    public void addApp(String app, String token, Long createTime, Long expiredTime) {
        List<App> apps = this.apps.get(app);
        if (apps == null) {
            apps = new ArrayList<>();
        }
        App a = new App();
        a.setToken(token);
        a.setCreateTime(createTime);
        a.setExpiredTime(expiredTime);
        apps.add(a);
        this.apps.put(app, apps);
    }

    /**
     * 清除过期的数据，延迟 0.5 秒避免网络抖动之类的问题导致缓存查无数据
     */
    public void removeExpired() {
        long now = System.currentTimeMillis() + 500;
        Map<String, List<String>> expired = new HashMap<>();
        this.apps.forEach((k, apps) -> {
            List<String> expiredTokens = apps.stream()
                    .filter(app -> app.getExpiredTime() <= now)
                    .map(App::getToken)
                    .toList();
            expired.put(k, expiredTokens);
        });
        expired.forEach((app, tokens) -> {
            List<App> apps = this.apps.get(app)
                    .stream()
                    .filter(a -> !tokens.contains(a.getToken()))
                    .collect(Collectors.toList());
            if (!apps.isEmpty()) {
                this.apps.put(app, apps);
            }
        });
    }

    /**
     * 清除 token 对应的数据
     */
    public void removeTokens(Collection<String> tokens) {
        for (Map.Entry<String, List<App>> entry : this.apps.entrySet()) {
            entry.getValue().removeIf(app -> tokens.contains(app.getToken()));
        }
        List<String> emptyKeys = new ArrayList<>();
        this.apps.forEach((k, v) -> {
            if (v.isEmpty()) {
                emptyKeys.add(k);
            }
        });
        emptyKeys.forEach(k -> this.apps.remove(k));
    }

    /**
     * 查询所有 token
     *
     * @return token 集
     */
    public List<String> findAllTokens() {
        return this.apps.values().stream()
                .flatMap(Collection::stream)
                .map(App::getToken)
                .distinct()
                .toList();
    }

    /**
     * 查询应用的所有 token
     *
     * @return token 集
     */
    public List<String> findAllTokens(String app) {
        List<App> apps = this.apps.get(app);
        if (apps == null) {
            return Collections.emptyList();
        }
        return apps.stream()
                .map(App::getToken)
                .distinct()
                .toList();
    }

    /**
     * 获取最大过期时间
     */
    public Long findLastExpiredTime() {
        long expiredTime = 0L;
        for (List<App> appList : this.apps.values()) {
            for (App app : appList) {
                if (app.getExpiredTime() >= expiredTime) {
                    expiredTime = app.getExpiredTime();
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
        for (List<App> appList : this.apps.values()) {
            for (App app : appList) {
                if (app.getExpiredTime() >= expiredTime) {
                    expiredTime = app.getExpiredTime();
                    expiredToken = app.getToken();
                }
            }
        }
        return expiredToken;
    }

    /**
     * 根据配置清除应该淘汰的数据
     *
     * @param config       登录配置
     * @param currentApp   当前应用
     * @param currentToken 当前 token
     * @return 应该淘汰的 token
     */
    public List<String> removeExpiredByConfig(SimpleTokenLoginConfig config, String currentApp, String currentToken) {
        Set<String> result = new HashSet<>();
        // 先淘汰同应用的数据
        List<App> apps = this.apps.get(currentApp);
        SimpleTokenLoginConfig.AppConfig appConfig = config.findAppConfig(currentApp);
        List<String> expiredTokens = findByStrategy(apps, currentToken, appConfig.getMaxStrategy(), appConfig.getMaxNum());
        result.addAll(expiredTokens);
        this.removeTokens(expiredTokens);

        // 再淘汰所有应用的数据
        apps = new ArrayList<>();
        for (List<App> appList : this.apps.values()) {
            apps.addAll(appList);
        }
        expiredTokens = findByStrategy(apps, currentToken, config.getMaxStrategy(), config.getMaxNum());
        result.addAll(expiredTokens);
        this.removeTokens(expiredTokens);
        return result.stream().toList();
    }

    private static List<String> findByStrategy(List<App> apps, String currentToken, LoginMaxStrategy strategy, int max) {
        if (apps == null || apps.isEmpty()) {
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
            apps.forEach(app -> {
                if (!currentToken.equals(app.getToken())) {
                    result.add(app.getToken());
                }
            });
            return result;
        }
        // 允许多地登录：超过允许的最大数时，根据策略查找数据
        int outNum = apps.size() - max;
        if (outNum <= 0) {
            return Collections.emptyList();
        }
        List<String> result = Collections.emptyList();
        if (strategy == LoginMaxStrategy.KICK_OUT_FIRST_CREATE) {
            result = apps.stream()
                    .sorted(Comparator.comparing(App::getCreateTime))
                    .map(App::getToken)
                    .filter(token -> !currentToken.equals(token))
                    .toList();
            if (outNum < result.size()) {
                result = result.subList(0, outNum);
            }
        } else if (strategy == LoginMaxStrategy.KICK_OUT_FIRST_EXPIRE) {
            result = apps.stream()
                    .sorted(Comparator.comparing(App::getExpiredTime))
                    .map(App::getToken)
                    .filter(token -> !currentToken.equals(token))
                    .toList();
            if (outNum < result.size()) {
                result = result.subList(0, outNum);
            }
        } else if (strategy == LoginMaxStrategy.KICK_OUT_ALL) {
            result = apps.stream()
                    .map(App::getToken)
                    .filter(token -> !currentToken.equals(token))
                    .toList();
        } else if (strategy == LoginMaxStrategy.REJECT) {
            throw new LoginRejectException("number exceeded");
        }
        return result;
    }

    @Data
    public static class App {
        private String token;
        private Long createTime;
        private Long expiredTime;
    }

}
