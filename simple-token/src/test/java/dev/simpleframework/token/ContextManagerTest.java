package dev.simpleframework.token;

import dev.simpleframework.token.context.*;
import dev.simpleframework.token.exception.InvalidContextException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ContextManager 注册和查找逻辑测试（无反射）
 * <p>
 * 注意：ContextManager 的 RPC_CONTEXTS 是 append-only 的 static 列表，
 * 测试间会累积。因此使用 @Order 控制执行顺序避免交叉影响。
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ContextManagerTest {

    @AfterEach
    public void tearDown() {
        // 恢复为 enabled 的 FW context，避免残留 disabled context 影响其他测试类
        ContextManager.registerFrameworkContext(new MockFrameworkContext(true));
    }

    // ===== register null 不抛异常 =====

    @Test
    @Order(1)
    public void testRegisterFrameworkContextNullNoop() {
        assertDoesNotThrow(() -> ContextManager.registerFrameworkContext(null));
    }

    @Test
    @Order(2)
    public void testRegisterRpcContextNullListNoop() {
        assertDoesNotThrow(() -> ContextManager.registerRpcContext((List<RpcContext>) null));
    }

    @Test
    @Order(3)
    public void testRegisterRpcContextNullSingleNoop() {
        assertDoesNotThrow(() -> ContextManager.registerRpcContext((RpcContext) null));
    }

    // ===== findContext — 先测 both disabled（需要干净的 RPC 列表） =====

    @Test
    @Order(4)
    public void testFindContextBothDisabledThrows() {
        FrameworkContext fwCtx = new MockFrameworkContext(false);
        RpcContext rpcCtx = new MockRpcContext(false);

        ContextManager.registerFrameworkContext(fwCtx);
        ContextManager.registerRpcContext(rpcCtx);

        assertThrows(InvalidContextException.class, ContextManager::findContext);
    }

    @Test
    @Order(5)
    public void testFindContextFrameworkPreferred() {
        FrameworkContext fwCtx = new MockFrameworkContext(true);
        RpcContext rpcCtx = new MockRpcContext(true);

        ContextManager.registerFrameworkContext(fwCtx);
        ContextManager.registerRpcContext(rpcCtx);

        Context ctx = ContextManager.findContext();
        assertInstanceOf(FrameworkContext.class, ctx);
    }

    @Test
    @Order(6)
    public void testFindContextFrameworkDisabledFallsBackToRpc() {
        FrameworkContext fwCtx = new MockFrameworkContext(false);
        RpcContext rpcCtx = new MockRpcContext(true);

        ContextManager.registerFrameworkContext(fwCtx);
        ContextManager.registerRpcContext(rpcCtx);

        Context ctx = ContextManager.findContext();
        assertInstanceOf(RpcContext.class, ctx, "should fall back to RPC when framework is disabled");
    }

    // ===== mock implementations =====

    private static class MockFrameworkContext implements FrameworkContext {
        private final boolean enabled;

        MockFrameworkContext(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public ContextRequest request() {
            return null;
        }

        @Override
        public ContextResponse response() {
            return null;
        }

        @Override
        public ContextStore store() {
            return null;
        }

        @Override
        public boolean enable() {
            return enabled;
        }
    }

    private static class MockRpcContext implements RpcContext {
        private final boolean enabled;

        MockRpcContext(boolean enabled) {
            this.enabled = enabled;
        }

        @Override
        public ContextRequest request() {
            return null;
        }

        @Override
        public ContextResponse response() {
            return null;
        }

        @Override
        public ContextStore store() {
            return null;
        }

        @Override
        public boolean enable() {
            return enabled;
        }
    }

}
