package dev.simpleframework.token.session;

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
    public void removeTokens() {
        long now = System.currentTimeMillis();
        SessionPerson person = new SessionPerson();
        String token1 = UUID.randomUUID().toString();
        String token2 = UUID.randomUUID().toString();
        person.addClient("1", token1, now, now);
        person.addClient("1", token1, now, now - 1000);
        person.addClient("1", UUID.randomUUID().toString(), now, now - 3000);
        person.addClient("2", UUID.randomUUID().toString(), now, now - 2000);
        person.addClient("2", token2, now, now + 1000);
        person.addClient("2", UUID.randomUUID().toString(), now, now + 2000);
        person.addClient("1", token2, now, now + 3000);
        person.addClient("3", token1, now, now + 2000);
        person.addClient("3", token2, now, now + 3000);

        Assertions.assertEquals(person.getClients().get("1").size(), 4);
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

}
