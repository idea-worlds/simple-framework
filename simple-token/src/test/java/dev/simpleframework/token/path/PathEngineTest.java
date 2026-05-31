package dev.simpleframework.token.path;

import dev.simpleframework.token.constant.HttpMethod;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * PathInfo / PathPermission / PathActionExecutor / HttpMethod 测试
 */
public class PathEngineTest {

    // ===== PathInfo =====

    @Test
    public void pathInfo_getHttpMethods_empty_returnsALL() {
        PathInfo info = new PathInfo("/api/**");
        List<HttpMethod> methods = info.getHttpMethods();
        Assertions.assertEquals(1, methods.size());
        Assertions.assertEquals(HttpMethod.ALL, methods.get(0));
    }

    @Test
    public void pathInfo_getHttpMethods_all_returnsALL() {
        PathInfo info = new PathInfo("/api/**", HttpMethod.ALL);
        List<HttpMethod> methods = info.getHttpMethods();
        Assertions.assertEquals(1, methods.size());
        Assertions.assertEquals(HttpMethod.ALL, methods.get(0));
    }

    @Test
    public void pathInfo_getHttpMethods_specific() {
        PathInfo info = new PathInfo("/api/**", HttpMethod.GET, HttpMethod.POST);
        List<HttpMethod> methods = info.getHttpMethods();
        Assertions.assertEquals(2, methods.size());
        Assertions.assertTrue(methods.contains(HttpMethod.GET));
        Assertions.assertTrue(methods.contains(HttpMethod.POST));
    }

    @Test
    public void pathInfo_setMethods_commaSeparated() {
        PathInfo info = new PathInfo();
        info.setMethods("GET,POST,PUT");
        List<HttpMethod> methods = info.getHttpMethods();
        Assertions.assertEquals(3, methods.size());
    }

    @Test
    public void pathInfo_setMethods_null() {
        PathInfo info = new PathInfo();
        info.setMethods((String) null);
        Assertions.assertEquals(1, info.getHttpMethods().size());
        Assertions.assertEquals(HttpMethod.ALL, info.getHttpMethods().get(0));
    }

    @Test
    public void pathInfo_setMethods_emptyString() {
        PathInfo info = new PathInfo();
        info.setMethods("");
        Assertions.assertEquals(1, info.getHttpMethods().size());
    }

    // ===== PathPermission builder =====

    @Test
    public void pathPermission_builder() {
        PathPermission perm = new PathPermission("/admin/**", HttpMethod.DELETE);
        perm.addPermission("admin:delete");
        perm.addRole("super-admin");

        Assertions.assertEquals("/admin/**", perm.getPath());
        Assertions.assertEquals(1, perm.getPermissions().size());
        Assertions.assertTrue(perm.getPermissions().contains("admin:delete"));
        Assertions.assertEquals(1, perm.getRoles().size());
        Assertions.assertTrue(perm.getRoles().contains("super-admin"));
    }

    @Test
    public void pathPermission_emptyPermissionsAndRoles_byDefault() {
        PathPermission perm = new PathPermission();
        Assertions.assertNotNull(perm.getPermissions());
        Assertions.assertNotNull(perm.getRoles());
        Assertions.assertTrue(perm.getPermissions().isEmpty());
        Assertions.assertTrue(perm.getRoles().isEmpty());
    }

    // ===== PathActionExecutor =====

    @Test
    public void pathActionExecutor_of_createsNew() {
        PathActionExecutor executor = PathActionExecutor.of();
        Assertions.assertNotNull(executor);
        Assertions.assertTrue(executor.getIncludes().isEmpty());
        Assertions.assertTrue(executor.getExcludes().isEmpty());
    }

    @Test
    public void pathActionExecutor_anyMatch_addsToIncludes() {
        PathActionExecutor executor = PathActionExecutor.of()
                .anyMatch("/api/**", "/public/**");
        Assertions.assertEquals(2, executor.getIncludes().size());
    }

    @Test
    public void pathActionExecutor_notMatch_addsToExcludes() {
        PathActionExecutor executor = PathActionExecutor.of()
                .noMatch("/error/**");
        Assertions.assertEquals(1, executor.getExcludes().size());
    }

    @Test
    public void pathActionExecutor_anyMatch_null_skips() {
        PathActionExecutor executor = PathActionExecutor.of()
                .anyMatch((String[]) null);
        Assertions.assertTrue(executor.getIncludes().isEmpty());
    }

    @Test
    public void pathActionExecutor_notMatch_null_skips() {
        PathActionExecutor executor = PathActionExecutor.of()
                .noMatch((String[]) null);
        Assertions.assertTrue(executor.getExcludes().isEmpty());
    }

    @Test
    public void pathActionExecutor_action_setsHandler() {
        boolean[] called = {false};
        PathActionExecutor executor = PathActionExecutor.of()
                .action(() -> called[0] = true);
        Assertions.assertNotNull(executor.getAction());
        executor.getAction().run();
        Assertions.assertTrue(called[0]);
    }

    @Test
    public void pathActionExecutor_chainable() {
        PathActionExecutor executor = PathActionExecutor.of()
                .anyMatch("/api/**")
                .noMatch("/api/health")
                .action(() -> {});
        Assertions.assertEquals(1, executor.getIncludes().size());
        Assertions.assertEquals(1, executor.getExcludes().size());
        Assertions.assertNotNull(executor.getAction());
    }

    // ===== HttpMethod =====

    @Test
    public void httpMethod_valueOf_standard() {
        Assertions.assertEquals(HttpMethod.GET, HttpMethod.valueOf("GET"));
        Assertions.assertEquals(HttpMethod.POST, HttpMethod.valueOf("POST"));
        Assertions.assertEquals(HttpMethod.DELETE, HttpMethod.valueOf("DELETE"));
    }

    @Test
    public void httpMethod_valueOf_caseInsensitive() {
        Assertions.assertEquals(HttpMethod.GET, HttpMethod.valueOf("get"));
        Assertions.assertEquals(HttpMethod.POST, HttpMethod.valueOf("post"));
    }

    @Test
    public void httpMethod_valueOf_null_returnsALL() {
        Assertions.assertEquals(HttpMethod.ALL, HttpMethod.valueOf(null));
    }

    @Test
    public void httpMethod_valueOf_unknown_createsNew() {
        HttpMethod method = HttpMethod.valueOf("PROPFIND");
        Assertions.assertEquals("PROPFIND", method.name());
    }

    @Test
    public void httpMethod_contains_all_matchesAnything() {
        Assertions.assertTrue(HttpMethod.contains(
                List.of(HttpMethod.ALL), HttpMethod.GET));
        Assertions.assertTrue(HttpMethod.contains(
                List.of(HttpMethod.ALL), HttpMethod.POST));
    }

    @Test
    public void httpMethod_contains_exactMatch() {
        Assertions.assertTrue(HttpMethod.contains(
                List.of(HttpMethod.GET, HttpMethod.POST), HttpMethod.GET));
    }

    @Test
    public void httpMethod_contains_noMatch() {
        Assertions.assertFalse(HttpMethod.contains(
                List.of(HttpMethod.GET), HttpMethod.POST));
    }

    @Test
    public void httpMethod_contains_emptyList_returnsFalse() {
        Assertions.assertFalse(HttpMethod.contains(
                List.of(), HttpMethod.GET));
    }

    @Test
    public void httpMethod_contains_nullMethod_returnsFalse() {
        Assertions.assertFalse(HttpMethod.contains(
                List.of(HttpMethod.GET), null));
    }

    @Test
    public void httpMethod_equals_sameName_isEqual() {
        Assertions.assertEquals(HttpMethod.valueOf("GET"), HttpMethod.GET);
    }

    @Test
    public void httpMethod_equals_differentName_notEqual() {
        Assertions.assertNotEquals(HttpMethod.GET, HttpMethod.POST);
    }
}
