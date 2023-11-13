package dev.simpleframework.util;

import java.lang.invoke.SerializedLambda;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
     * 并行执行对象集的指定方法
     *
     * @return 执行过程产生的异常集合
     */
    public static <T> List<Throwable> parallelRun(Collection<T> objects, Consumer<T> action) {
        return parallelRun(objects, action, null);
    }

    public static <T> List<Throwable> parallelRun(Collection<T> objects, Consumer<T> action, Executor executor) {
        Function<T, Throwable> function = object -> {
            try {
                action.accept(object);
                return null;
            } catch (Throwable e) {
                return e;
            }
        };
        try {
            return parallelRun(objects, function, executor).stream().filter(Objects::nonNull).toList();
        } catch (Throwable e) {
            return Collections.singletonList(e);
        }
    }

    /**
     * 并行执行对象集的指定方法
     *
     * @return 执行方法的结果集合
     */
    public static <T, R> List<R> parallelRun(Collection<T> objects, Function<T, R> action) {
        return parallelRun(objects, action, null);
    }

    public static <T, R> List<R> parallelRun(Collection<T> objects, Function<T, R> action, Executor executor) {
        if (executor == null) {
            return objects.parallelStream().map(action).toList();
        }
        List<CompletableFuture<R>> futures = objects.stream()
                .map(object -> {
                    Supplier<R> supplier = () -> action.apply(object);
                    return CompletableFuture.supplyAsync(supplier, executor);
                })
                .toList();
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[]{}))
                .thenApply(v -> futures.stream().map(CompletableFuture::join).toList())
                .join();
    }

}
