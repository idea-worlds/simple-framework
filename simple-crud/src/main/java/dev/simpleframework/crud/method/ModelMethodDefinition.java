package dev.simpleframework.crud.method;

import dev.simpleframework.crud.ModelInfo;

/**
 * 定义模型方法
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public interface ModelMethodDefinition {

    /**
     * 注册
     *
     * @param info 模型信息
     */
    void register(ModelInfo<?> info) ;

    static String methodId(ModelInfo<?> info, String name) {
        return info.methodNamespace() + "#" + name;
    }

}
