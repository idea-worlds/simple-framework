package dev.simpleframework.token.context.impl;

import dev.simpleframework.token.context.ContextRequest;
import dev.simpleframework.token.context.ContextResponse;
import dev.simpleframework.token.context.ContextStore;
import dev.simpleframework.token.context.SimpleTokenFrameworkContext;
import dev.simpleframework.token.exception.InvalidContextException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Spring servlet 上下文处理器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public class SpringServletContext extends AbstractContext implements SimpleTokenFrameworkContext {

    /**
     * 写入上下文对象，使用完毕必须清除 {@link #clearContext}
     */
    public static void setContext(HttpServletRequest request, HttpServletResponse response) {
        if (request == null && response == null) {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) {
                throw new InvalidContextException("Can not found a valid context");
            }
            request = attrs.getRequest();
            response = attrs.getResponse();
        }

        ContextRequest contextRequest = new SpringServletContextRequest(request);
        ContextResponse contextResponse = new SpringServletContextResponse(response);
        ContextStore contextStore = new SpringServletContextStore(request);
        AbstractContext.setContextData(contextRequest, contextResponse, contextStore);
    }

    /**
     * 清除当前线程的上下文对象
     */
    public static void clearContext() {
        AbstractContext.removeContextData();
    }

    @Override
    public boolean enable() {
        return AbstractContext.getContextData(false) != null
                || RequestContextHolder.getRequestAttributes() != null;
    }

}
