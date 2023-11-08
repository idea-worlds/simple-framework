package dev.simpleframework.token.path;

import dev.simpleframework.token.constant.HttpMethod;
import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class PathInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String path;
    private List<String> methods;

    public PathInfo() {
    }

    public PathInfo(String path, HttpMethod... methods) {
        this.path = path;
        if (methods != null) {
            this.methods = Arrays.stream(methods).filter(Objects::nonNull).map(HttpMethod::name).toList();
        }
    }

    public PathInfo(String path, List<HttpMethod> methods) {
        this.path = path;
        if (methods != null) {
            this.methods = methods.stream().filter(Objects::nonNull).map(HttpMethod::name).toList();
        }
    }

    public void setMethods(String methods) {
        List<String> others = null;
        if (methods != null) {
            others = Arrays.stream(methods.split(","))
                    .filter(m -> m != null && !m.isBlank())
                    .toList();
        }
        this.setMethods(others);
    }

    public void setMethods(List<String> methods) {
        this.methods = methods;
    }

    public List<HttpMethod> getHttpMethods() {
        if (this.methods == null || this.methods.isEmpty() || this.methods.contains(HttpMethod.ALL.name())) {
            return Collections.singletonList(HttpMethod.ALL);
        }
        return this.methods.stream().map(HttpMethod::valueOf).filter(Objects::nonNull).toList();
    }

}
