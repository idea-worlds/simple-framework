package dev.simpleframework.token.context.impl;

import dev.simpleframework.token.context.ContextRequest;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public abstract class AbstractContextRequest implements ContextRequest {

    protected String getRequestIp(Function<String, String> getIp, Supplier<String> defaultIp) {
        List<String> headerNames = List.of(
                "X-Forwarded-For", "X-Real-IP",
                "Proxy-Client-IP", "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR");
        String ip = null;
        for (String headerName : headerNames) {
            ip = getIp.apply(headerName);
            if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
                ip = null;
                continue;
            }
            break;
        }
        if (ip == null) {
            ip = defaultIp.get();
        }
        if (ip != null && ip.indexOf(",") > 0) {
            String[] ips = ip.split(",");
            for (String sub : ips) {
                if (sub == null || sub.isBlank() || "unknown".equalsIgnoreCase(sub)) {
                    continue;
                }
                ip = sub;
                break;
            }
        }
        return ip;
    }

}
