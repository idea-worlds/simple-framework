package dev.simpleframework.token;

import dev.simpleframework.token.config.SimpleTokenConfig;
import dev.simpleframework.token.config.SimpleTokenLoginConfig;
import dev.simpleframework.token.config.SimpleTokenPathConfig;
import dev.simpleframework.token.exception.InvalidPermissionException;
import dev.simpleframework.token.exception.InvalidRoleException;
import dev.simpleframework.token.permission.PermissionManager;
import dev.simpleframework.token.permission.PermissionQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 权限/角色校验 — 通过 SimpleTokens 门面测试
 */
public class SimpleTokenPermissionTest {

    @BeforeEach
    public void setUp() {
        if (SimpleTokens.getGlobalConfig() == null) {
            SimpleTokenConfig config = new SimpleTokenConfig();
            config.setLogin(new SimpleTokenLoginConfig());
            config.setPath(new SimpleTokenPathConfig());
            SimpleTokens.setGlobalConfig(config);
        }
    }

    @AfterEach
    public void tearDown() {
        SimpleTokens.clearThreadCache();
        PermissionManager.registerQuery(PermissionQuery.DEFAULT);
    }

    // ===== checkHasPermission =====

    @Test
    public void testCheckHasPermissionPassesWhenHasAll() {
        setPermissions("a", "b", "c");
        assertDoesNotThrow(() -> SimpleTokens.checkHasPermission("a", "b"));
    }

    @Test
    public void testCheckHasPermissionThrowsWhenMissingOne() {
        setPermissions("a", "b");
        assertThrows(InvalidPermissionException.class, () ->
                SimpleTokens.checkHasPermission("a", "c"));
    }

    @Test
    public void testCheckHasPermissionNullArrayNoop() {
        setPermissions("a");
        assertDoesNotThrow(() -> SimpleTokens.checkHasPermission((String[]) null));
    }

    @Test
    public void testCheckHasPermissionNullListNoop() {
        setPermissions("a");
        assertDoesNotThrow(() -> SimpleTokens.checkHasPermission((List<String>) null));
    }

    @Test
    public void testCheckHasPermissionEmptyListNoop() {
        setPermissions("a");
        assertDoesNotThrow(() -> SimpleTokens.checkHasPermission(List.of()));
    }

    // ===== checkAnyPermission =====

    @Test
    public void testCheckAnyPermissionPassesWhenHasOne() {
        setPermissions("a", "b");
        assertDoesNotThrow(() -> SimpleTokens.checkAnyPermission("a", "c"));
    }

    @Test
    public void testCheckAnyPermissionThrowsWhenHasNone() {
        setPermissions("a", "b");
        assertThrows(InvalidPermissionException.class, () ->
                SimpleTokens.checkAnyPermission("c", "d"));
    }

    @Test
    public void testCheckAnyPermissionNullArrayNoop() {
        setPermissions("a");
        assertDoesNotThrow(() -> SimpleTokens.checkAnyPermission((String[]) null));
    }

    @Test
    public void testCheckAnyPermissionNullListNoop() {
        setPermissions("a");
        assertDoesNotThrow(() -> SimpleTokens.checkAnyPermission((List<String>) null));
    }

    // ===== checkNoPermission =====

    @Test
    public void testCheckNoPermissionPassesWhenHasNone() {
        setPermissions("a", "b");
        assertDoesNotThrow(() -> SimpleTokens.checkNoPermission("c", "d"));
    }

    @Test
    public void testCheckNoPermissionThrowsWhenHasOne() {
        setPermissions("a", "b");
        assertThrows(InvalidPermissionException.class, () ->
                SimpleTokens.checkNoPermission("a", "c"));
    }

    @Test
    public void testCheckNoPermissionNullArrayNoop() {
        setPermissions("a");
        assertDoesNotThrow(() -> SimpleTokens.checkNoPermission((String[]) null));
    }

    @Test
    public void testCheckNoPermissionNullListNoop() {
        setPermissions("a");
        assertDoesNotThrow(() -> SimpleTokens.checkNoPermission((List<String>) null));
    }

    // ===== checkHasRole =====

    @Test
    public void testCheckHasRolePassesWhenHasAll() {
        setRoles("admin", "user");
        assertDoesNotThrow(() -> SimpleTokens.checkHasRole("admin", "user"));
    }

    @Test
    public void testCheckHasRoleThrowsWhenMissingOne() {
        setRoles("admin");
        assertThrows(InvalidRoleException.class, () ->
                SimpleTokens.checkHasRole("admin", "guest"));
    }

    @Test
    public void testCheckHasRoleNullArrayNoop() {
        setRoles("admin");
        assertDoesNotThrow(() -> SimpleTokens.checkHasRole((String[]) null));
    }

    @Test
    public void testCheckHasRoleNullListNoop() {
        setRoles("admin");
        assertDoesNotThrow(() -> SimpleTokens.checkHasRole((List<String>) null));
    }

    // ===== checkAnyRole =====

    @Test
    public void testCheckAnyRolePassesWhenHasOne() {
        setRoles("admin", "user");
        assertDoesNotThrow(() -> SimpleTokens.checkAnyRole("admin", "guest"));
    }

    @Test
    public void testCheckAnyRoleThrowsWhenHasNone() {
        setRoles("admin");
        assertThrows(InvalidRoleException.class, () ->
                SimpleTokens.checkAnyRole("guest", "visitor"));
    }

    @Test
    public void testCheckAnyRoleNullArrayNoop() {
        setRoles("admin");
        assertDoesNotThrow(() -> SimpleTokens.checkAnyRole((String[]) null));
    }

    // ===== checkNotRole =====

    @Test
    public void testCheckNotRolePassesWhenHasNone() {
        setRoles("admin", "user");
        assertDoesNotThrow(() -> SimpleTokens.checkNotRole("guest", "visitor"));
    }

    @Test
    public void testCheckNotRoleThrowsWhenHasOne() {
        setRoles("admin");
        assertThrows(InvalidRoleException.class, () ->
                SimpleTokens.checkNotRole("admin", "guest"));
    }

    @Test
    public void testCheckNotRoleNullArrayNoop() {
        setRoles("admin");
        assertDoesNotThrow(() -> SimpleTokens.checkNotRole((String[]) null));
    }

    // ===== hasPermission / hasRole =====

    @Test
    public void testHasPermissionReturnsTrueWhenMatched() {
        setPermissions("admin:read", "admin:write");
        assertTrue(SimpleTokens.hasPermission("admin:read"));
        assertFalse(SimpleTokens.hasPermission("user:read"));
    }

    @Test
    public void testHasRoleReturnsTrueWhenMatched() {
        setRoles("admin", "user");
        assertTrue(SimpleTokens.hasRole("admin"));
        assertFalse(SimpleTokens.hasRole("guest"));
    }

    // ===== getPermissions / getRoles =====

    @Test
    public void testGetPermissionsReturnsCurrentUserPermissions() {
        setPermissions("p1", "p2");
        List<String> perms = SimpleTokens.getPermissions();
        assertEquals(List.of("p1", "p2"), perms);
    }

    @Test
    public void testGetRolesReturnsCurrentUserRoles() {
        setRoles("admin", "user");
        List<String> roles = SimpleTokens.getRoles();
        assertEquals(List.of("admin", "user"), roles);
    }

    // ===== List 空值和 null 安全（覆盖剩余 check 方法） =====

    @Test
    public void testCheckAnyPermissionEmptyListNoop() {
        setPermissions("a");
        assertDoesNotThrow(() -> SimpleTokens.checkAnyPermission(List.of()));
    }

    @Test
    public void testCheckNoPermissionEmptyListNoop() {
        setPermissions("a");
        assertDoesNotThrow(() -> SimpleTokens.checkNoPermission(List.of()));
    }

    @Test
    public void testCheckHasRoleEmptyListNoop() {
        setRoles("admin");
        assertDoesNotThrow(() -> SimpleTokens.checkHasRole(List.of()));
    }

    @Test
    public void testCheckAnyRoleNullListNoop() {
        setRoles("admin");
        assertDoesNotThrow(() -> SimpleTokens.checkAnyRole((List<String>) null));
    }

    @Test
    public void testCheckAnyRoleEmptyListNoop() {
        setRoles("admin");
        assertDoesNotThrow(() -> SimpleTokens.checkAnyRole(List.of()));
    }

    @Test
    public void testCheckNotRoleNullListNoop() {
        setRoles("admin");
        assertDoesNotThrow(() -> SimpleTokens.checkNotRole((List<String>) null));
    }

    @Test
    public void testCheckNotRoleEmptyListNoop() {
        setRoles("admin");
        assertDoesNotThrow(() -> SimpleTokens.checkNotRole(List.of()));
    }

    // ===== helpers =====

    private static void setPermissions(String... permissions) {
        SimpleTokens.clearThreadCache();
        PermissionManager.registerQuery(new PermissionQuery() {
            @Override
            public List<String> listPermissions() {
                return List.of(permissions);
            }

            @Override
            public List<String> listRoles() {
                return List.of();
            }
        });
    }

    private static void setRoles(String... roles) {
        SimpleTokens.clearThreadCache();
        PermissionManager.registerQuery(new PermissionQuery() {
            @Override
            public List<String> listPermissions() {
                return List.of();
            }

            @Override
            public List<String> listRoles() {
                return List.of(roles);
            }
        });
    }

}
