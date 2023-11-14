package dev.simpleframework.token.filter.impl;

import dev.simpleframework.core.CommonResponse;
import dev.simpleframework.token.context.impl.SpringReactorContext;
import dev.simpleframework.token.exception.ExceptionManager;
import dev.simpleframework.token.exception.ExceptionResponse;
import dev.simpleframework.token.filter.SimpleTokenFilter;
import dev.simpleframework.token.path.PathManager;
import dev.simpleframework.util.Jsons;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class SimpleTokenSpringReactorFilter implements SimpleTokenFilter, WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        try {
            // 写入上下文
            SpringReactorContext.setContext(exchange);
            // 执行路径匹配器
            PathManager.execMatchers();
        } catch (Throwable e) {
            return this.writeExceptionResponse(exchange.getResponse(), e);
        } finally {
            // 清除上下文
            SpringReactorContext.clearContext();
        }

        // 写入上下文
        SpringReactorContext.setContext(exchange);
        return chain.filter(exchange)
                .doFinally(r -> {
                    // 清除上下文
                    SpringReactorContext.clearContext();
                });
    }

    protected Mono<Void> writeExceptionResponse(ServerHttpResponse response, Throwable exception) {
        // 获取异常处理返回值
        ExceptionResponse responseData = ExceptionManager.getResponseData(exception);
        CommonResponse<String> responseBody = CommonResponse.failure(responseData.errCode(), responseData.errMsg());

        // 写入输出流
        response.setRawStatusCode(responseData.status());
        response.getHeaders().set("Content-Type", "application/json;charset=UTF-8");
        DataBuffer buffer = response.bufferFactory().wrap(Jsons.write(responseBody).getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer))
                .doOnError(error -> DataBufferUtils.release(buffer));
    }

}
