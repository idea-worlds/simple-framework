package dev.simpleframework.token.filter.impl;

import dev.simpleframework.core.CommonResponse;
import dev.simpleframework.token.context.impl.SpringServletContext;
import dev.simpleframework.token.exception.ExceptionManager;
import dev.simpleframework.token.exception.ExceptionResponse;
import dev.simpleframework.token.filter.SimpleTokenFilter;
import dev.simpleframework.token.path.PathManager;
import dev.simpleframework.util.Jsons;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.io.IOException;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class SimpleTokenSpringServletFilter implements SimpleTokenFilter, Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            // 写入上下文
            SpringServletContext.setContext((HttpServletRequest) request, (HttpServletResponse) response);
            // 执行路径匹配器
            PathManager.execMatchers();
        } catch (Throwable e) {
            // 获取异常处理返回值
            ExceptionResponse responseData = ExceptionManager.getResponseData(e);
            CommonResponse<String> responseBody = CommonResponse.failure(responseData.errCode(), responseData.errMsg());

            // 写入输出流
            ((HttpServletResponse) response).setStatus(responseData.status());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print(Jsons.write(responseBody));
            return;
        } finally {
            SpringServletContext.clearContext();
        }

        try {
            SpringServletContext.setContext((HttpServletRequest) request, (HttpServletResponse) response);
            chain.doFilter(request, response);
        } finally {
            SpringServletContext.clearContext();
        }
    }

}
