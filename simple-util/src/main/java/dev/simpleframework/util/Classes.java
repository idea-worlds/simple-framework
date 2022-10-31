package dev.simpleframework.util;

import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * 反射工具类
 */
@SuppressWarnings("all")
public final class Classes {
    private static final Map<Class<?>, WeakReference<Constructor>> CONSTRUCTORS = new ConcurrentHashMap<>();
    private static final List<String> PROXY_CLASSES = Arrays.asList(
            "net.sf.cglib.proxy.Factory",
            "org.springframework.cglib.proxy.Factory",
            "javassist.util.proxy.ProxyObject",
            "org.apache.ibatis.javassist.util.proxy.ProxyObject");

    /**
     * 基于空构造函数创建实例
     *
     * @param clazz 要创建实例的类
     * @param <T>   要创建实例的类
     * @return 类实例
     */
    @SneakyThrows
    public static <T> T newInstance(Class<T> clazz) {
        Constructor<T> constructor = getConstructor(clazz);
        return constructor.newInstance();
    }

    /**
     * 获取空构造函数
     *
     * @param clazz 要查询的类
     * @param <T>   要查询的类
     * @return 构造函数
     */
    public static <T> Constructor<T> getConstructor(Class<T> clazz) {
        return Optional.ofNullable(CONSTRUCTORS.get(clazz))
                .map(WeakReference::get)
                .orElseGet(() -> {
                    Constructor<T> c = null;
                    try {
                        c = clazz.getDeclaredConstructor();
                    } catch (Exception ignore) {
                    }
                    if (c != null) {
                        c.setAccessible(true);
                        CONSTRUCTORS.put(clazz, new WeakReference<>(c));
                    }
                    return c;
                });
    }

    /**
     * 获取子类及父类的所有字段列表
     * 若覆写了父类字段，则取子类的字段
     *
     * @param clazz       类
     * @param fieldFilter 字段过滤器
     * @return 字段列表
     */
    public static List<Field> getFields(Class<?> clazz, Predicate<Field> fieldFilter) {
        List<Field> superFields = new ArrayList<>();
        Class<?> currentClass = clazz.getSuperclass();
        while (currentClass != null && currentClass != Object.class) {
            Field[] declaredFields = currentClass.getDeclaredFields();
            Collections.addAll(superFields, declaredFields);
            currentClass = currentClass.getSuperclass();
        }
        Map<String, Field> fields = Stream.of(clazz.getDeclaredFields())
                .collect(toMap(Field::getName, f -> f, (o1, o2) -> o1, LinkedHashMap::new));
        for (Field field : superFields) {
            String fieldName = field.getName();
            if (fields.containsKey(fieldName)) {
                continue;
            }
            fields.put(fieldName, field);
        }
        return fields.values().stream()
                .filter(fieldFilter)
                .collect(toList());
    }

    public static List<Field> getFieldsByAnnotations(Class<?> clazz, Class<? extends Annotation>... annotations) {
        if (annotations == null) {
            return Collections.emptyList();
        }
        Predicate<Field> fieldFilter = f -> {
            for (Class<? extends Annotation> annotation : annotations) {
                if (f.isAnnotationPresent(annotation)) {
                    return true;
                }
            }
            return false;
        };
        return getFields(clazz, fieldFilter);
    }

    /**
     * 获取字段的泛型
     *
     * @param field        类字段
     * @param defaultClass 默认结果
     * @return 泛型
     */
    public static Class<?> getGenericClass(Field field, Class<?> defaultClass) {
        Class<?> result = defaultClass;
        Type genericType = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
        try {
            result = (Class<?>) genericType;
        } catch (Exception ignore) {
            try {
                result = (Class<?>) ((ParameterizedType) genericType).getRawType();
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    /**
     * 获取被代理的实际类
     *
     * @param clazz 原类
     * @return 如果是代理类，返回父类，否则返回自身
     */
    public static Class<?> getTargetClassIfProxy(Class<?> clazz) {
        if (clazz == null) {
            return null;
        }
        Class<?> superclass = clazz.getSuperclass();
        if (superclass == null || superclass == Object.class) {
            return clazz;
        }
        boolean isProxy = false;
        if (clazz.getName().contains("$$")) {
            isProxy = true;
        } else {
            for (Class<?> cls : clazz.getInterfaces()) {
                if (PROXY_CLASSES.contains(cls.getName())) {
                    isProxy = true;
                    break;
                }
            }
        }
        return isProxy ? superclass : clazz;
    }

    /**
     * 递归获取父类的信息
     *
     * @param clazz 要查询的类
     * @param func  获取信息的方法
     * @param <T>   方法返回值类型
     * @return func 结果
     */
    public static <T> T getInfoFromSuperclass(Class<?> clazz, Function<Class<?>, T> func) {
        if (clazz == null || clazz == Object.class) {
            return null;
        }
        T result = null;
        Class<?> supperClass = clazz.getSuperclass();
        while (result == null) {
            if (supperClass == null || supperClass == Object.class) {
                break;
            }
            result = func.apply(supperClass);
            supperClass = supperClass.getSuperclass();
        }
        return result;
    }

}
