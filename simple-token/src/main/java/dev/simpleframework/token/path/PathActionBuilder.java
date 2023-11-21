package dev.simpleframework.token.path;

import java.util.Collections;
import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public interface PathActionBuilder {

    List<PathActionExecutor> init();

    PathActionBuilder DEFAULT = Collections::emptyList;

}
