package ${package}.adapter.controller;

import dev.simpleframework.core.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Slf4j
@RestControllerAdvice
public class ${sample}WebMvcExceptionResolver {

    @ExceptionHandler(Throwable.class)
    public CommonResponse<String> handler(HttpServletRequest request, HttpServletResponse response, Throwable exception) {
        response.setStatus(500);
        log.error(this.errorMsg(request, exception), exception);
        return CommonResponse.failure("1", "Unknown");
    }

    private String errorMsg(HttpServletRequest request, Throwable exception) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        if (method == null) {
            method = " ";
        }
        if (uri == null) {
            uri = " ";
        }
        return "[" + method + "-" + uri + "] " + exception.getMessage();
    }

}
