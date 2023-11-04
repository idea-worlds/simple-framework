package dev.simpleframework.token.session;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class SimpleTokenAppsTest {
    public static final long now = System.currentTimeMillis();

    private static SimpleTokenApps mock() {
        SimpleTokenApps apps = new SimpleTokenApps();
        apps.addApp("1", UUID.randomUUID().toString(), now, now);
        apps.addApp("1", UUID.randomUUID().toString(), now, now - 1000);
        apps.addApp("1", UUID.randomUUID().toString(), now, now + 3000);
        apps.addApp("2", UUID.randomUUID().toString(), now, now - 2000);
        apps.addApp("2", UUID.randomUUID().toString(), now, now + 1000);
        apps.addApp("2", UUID.randomUUID().toString(), now, now + 2000);
        apps.addApp("1", UUID.randomUUID().toString(), now, now - 3000);
        return apps;
    }

    @Test
    public void removeExpired() {
        SimpleTokenApps apps = mock();
        Assertions.assertEquals(apps.getApps().get("1").size(), 4);
        Assertions.assertEquals(apps.getApps().get("2").size(), 3);
        apps.removeExpired();
        Assertions.assertEquals(apps.getApps().get("1").size(), 1);
        Assertions.assertEquals(apps.getApps().get("2").size(), 2);
    }

    @Test
    public void removeTokens() {
        long now = System.currentTimeMillis();
        SimpleTokenApps apps = new SimpleTokenApps();
        String token1 = UUID.randomUUID().toString();
        String token2 = UUID.randomUUID().toString();
        apps.addApp("1", token1, now, now);
        apps.addApp("1", token1, now, now - 1000);
        apps.addApp("1", UUID.randomUUID().toString(), now, now - 3000);
        apps.addApp("2", UUID.randomUUID().toString(), now, now - 2000);
        apps.addApp("2", token2, now, now + 1000);
        apps.addApp("2", UUID.randomUUID().toString(), now, now + 2000);
        apps.addApp("1", token2, now, now + 3000);
        apps.addApp("3", token1, now, now + 2000);
        apps.addApp("3", token2, now, now + 3000);

        Assertions.assertEquals(apps.getApps().get("1").size(), 4);
        Assertions.assertEquals(apps.getApps().get("2").size(), 3);
        Assertions.assertEquals(apps.getApps().get("3").size(), 2);
        apps.removeTokens(List.of(token1, token2));
        Assertions.assertEquals(apps.getApps().get("1").size(), 1);
        Assertions.assertEquals(apps.getApps().get("2").size(), 2);
        Assertions.assertNull(apps.getApps().get("3"));
    }

    @Test
    public void findLastExpiredTime() {
        SimpleTokenApps apps = mock();
        Assertions.assertEquals(apps.findLastExpiredTime(), now + 3000);
    }

    @Test
    public void findLastExpiredToken() {
        SimpleTokenApps apps = mock();
        Assertions.assertEquals(apps.findLastExpiredToken(), apps.getApps().get("1").get(2).getToken());
    }

}
