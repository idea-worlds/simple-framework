package dev.simpleframework.util;

import java.lang.invoke.SerializedLambda;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 函数工具类
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public final class Functions {
    private static final Map<Class<?>, WeakReference<SerializedLambda>> LAMBDA_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取表达式方法的字段名
     * 去除方法名前的 is/get/set 并将首字符转为小写
     *
     * @param function 表达式
     * @return 类字段名
     */
    public static String getLambdaFieldName(SerializedFunction<?, ?> function) {
        Class<?> funcClass = function.getClass();
        SerializedLambda lambda = Optional.ofNullable(LAMBDA_CACHE.get(funcClass))
                .map(WeakReference::get)
                .orElseGet(() -> {
                    SerializedLambda temp;
                    try {
                        Method method = funcClass.getDeclaredMethod("writeReplace");
                        method.setAccessible(Boolean.TRUE);
                        temp = (SerializedLambda) method.invoke(function);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    LAMBDA_CACHE.put(funcClass, new WeakReference<>(temp));
                    return temp;
                });
        String result = lambda.getImplMethodName();
        if (result.startsWith("is")) {
            result = result.substring(2);
        } else if (result.startsWith("get") || result.startsWith("set")) {
            result = result.substring(3);
        }
        boolean firstCharNeedToLower = result.length() == 1 ||
                result.length() > 1 && !Character.isUpperCase(result.charAt(1));
        if (firstCharNeedToLower) {
            result = result.substring(0, 1).toLowerCase(Locale.ENGLISH) + result.substring(1);
        }
        return result;
    }

    /**
     * 异步执行对象集的指定方法
     *
     * @return 执行过程产生的异常集合
     */
    public static <T> List<Throwable> runAsync(Collection<T> objects, Consumer<T> action) {
        try {
            List<CompletableFuture<Throwable>> threads = objects.stream()
                    .map(object ->
                            CompletableFuture.supplyAsync(() -> {
                                try {
                                    action.accept(object);
                                    return null;
                                } catch (Throwable e) {
                                    return e;
                                }
                            })
                    )
                    .toList();
            return CompletableFuture.allOf(threads.toArray(new CompletableFuture[]{}))
                    .thenApply(v ->
                            threads.stream().map(CompletableFuture::join).filter(Objects::nonNull).toList()
                    )
                    .get();
        } catch (Throwable e) {
            return Collections.singletonList(e);
        }
    }

}
