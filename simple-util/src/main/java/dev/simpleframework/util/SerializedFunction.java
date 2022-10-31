package dev.simpleframework.util;

import java.io.Serializable;

/**
 * Alias for {@link java.util.function.Function} extends Serializable
 *
 * @author loyayz (loyayz@foxmail.com)
 */
@FunctionalInterface
public interface SerializedFunction<T, R> extends Serializable {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    R apply(T t);

}
