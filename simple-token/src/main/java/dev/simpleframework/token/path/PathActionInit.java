package dev.simpleframework.token.path;

import java.util.Collections;
import java.util.List;

/**
 * 路径方法初始化构建器
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public interface PathActionInit {

    List<PathActionExecutor> init();

    PathActionInit DEFAULT = Collections::emptyList;

}
