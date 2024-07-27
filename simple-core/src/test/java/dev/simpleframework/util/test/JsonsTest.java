package dev.simpleframework.util.test;

import dev.simpleframework.util.Jsons;
import dev.simpleframework.util.Strings;
import lombok.Data;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author loyayz
 **/
public class JsonsTest {

    @Test
    public void simple() {
        User user = User.mock();
        String json = Jsons.write(user);
        System.out.println(json);
        User jsonUser = Jsons.read(json, User.class);
        assertSame(jsonUser, user);
    }

    @Test
    @SneakyThrows
    public void file() {
        Path path = Files.createTempFile("user", ".txt");
        try {
            File file = path.toFile();
            User user = User.mock();
            Jsons.write(user, file);
            Files.readAllLines(path).forEach(System.out::println);
            User jsonUser = Jsons.read(file, User.class);
            assertSame(jsonUser, user);
        } finally {
            Files.deleteIfExists(path);
        }
    }

    @Test
    public void map() {
        User user = User.mock();
        Map<String, Object> userMap = Jsons.toMap(user);
        System.out.println(userMap);
        User jsonUser = Jsons.read(userMap, User.class);
        assertSame(jsonUser, user);
    }

    static void assertSame(User expected, User actual) {
        Assertions.assertEquals(expected.name, actual.name);
        Assertions.assertEquals(expected.age, actual.age);
        Assertions.assertEquals(expected.no, actual.no);
        Assertions.assertTrue(expected.roles.containsAll(actual.roles));
        Assertions.assertTrue(expected.adds.containsAll(actual.adds));
        Assertions.assertTrue(expected.attrs.keySet().containsAll(actual.attrs.keySet()));
        Assertions.assertTrue(expected.attrs.values().containsAll(actual.attrs.values()));
        Assertions.assertNull(expected.null1);
        Assertions.assertNull(expected.null2);
        Assertions.assertEquals(expected.accounts.size(), actual.accounts.size());
        Assertions.assertTrue(() -> expected.accounts.stream().allMatch(
                jsonAccount -> actual.accounts.stream().anyMatch(jsonAccount::equals)));
    }

    @Data
    static class User {
        private String name;
        private int age;
        private Long no;
        private List<String> roles;
        private Set<String> adds;
        private Map<String, Object> attrs;
        private List<Account> accounts;
        private String null1;
        private Account null2;

        static User mock() {
            User user = new User();
            user.name = Strings.uuid32();
            user.age = ThreadLocalRandom.current().nextInt(1, 100);
            user.no = ThreadLocalRandom.current().nextLong();
            user.roles = List.of("admin", "user");
            user.adds = Set.of("china");
            user.attrs = Map.of("a", "1", "b", 2, "c", "3");
            user.accounts = List.of(Account.mock(), Account.mock());
            return user;
        }
    }

    @Data
    static class Account {
        private String name;
        private String password;
        private Date time;

        static Account mock() {
            Account account = new Account();
            account.name = Strings.uuid32();
            account.password = Strings.uuid32();
            account.time = new Date();
            return account;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Account account = (Account) o;
            return Objects.equals(name, account.name) && Objects.equals(password, account.password) && Objects.equals(time, account.time);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, password, time);
        }

    }

}
