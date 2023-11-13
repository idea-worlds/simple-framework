package dev.simpleframework.core.util;

import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.ref.WeakReference;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 反射工具类
 *
 * @author loyayz (loyayz@foxmail.com)
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
        if (clazz.isInterface()) {
            throw new IllegalArgumentException("Failed to instantiate [" + clazz.getName() + "]:" +
                    " Specified class is an interface");
        }
        Constructor<T> ctor = Optional.ofNullable(CONSTRUCTORS.get(clazz)).map(WeakReference::get).orElse(null);
        if (ctor == null) {
            try {
                ctor = clazz.getDeclaredConstructor();
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("Failed to instantiate [" + clazz.getName() + "]:" +
                        " No default constructor found", e);
            } catch (LinkageError e) {
                throw new IllegalArgumentException("Failed to instantiate [" + clazz.getName() + "]:" +
                        " Unresolvable class definition", e);
            }
            if ((!Modifier.isPublic(ctor.getModifiers()) || !Modifier.isPublic(ctor.getDeclaringClass().getModifiers())) && !ctor.isAccessible()) {
                ctor.setAccessible(true);
            }
            CONSTRUCTORS.put(clazz, new WeakReference<>(ctor));
        }
        return ctor.newInstance();
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
                .collect(Collectors.toMap(Field::getName, f -> f, (o1, o2) -> o1, LinkedHashMap::new));
        for (Field field : superFields) {
            String fieldName = field.getName();
            if (fields.containsKey(fieldName)) {
                continue;
            }
            fields.put(fieldName, field);
        }
        return fields.values().stream()
                .filter(fieldFilter)
                .collect(Collectors.toList());
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
     * 递归获取自身或父类的信息
     *
     * @param clazz  要查询的类
     * @param action 获取信息的方法
     * @param <T>    方法返回值类型
     * @return action 结果
     */
    public static <T> T findInfo(Class<?> clazz, Function<Class<?>, T> action) {
        if (clazz == null || clazz == Object.class) {
            return null;
        }
        T result;
        do {
            result = action.apply(clazz);
            clazz = clazz.getSuperclass();
        } while (result == null && clazz != null && clazz != Object.class);
        return result;
    }

    /**
     * 递归获取自身或父类或接口的注解
     *
     * @param clazz          要查询的类
     * @param annotationType 注解类
     * @return 注解集合
     */
    public static <T extends Annotation> Map<Class<?>, List<T>> findAnnotations(Class<?> clazz, Class<T> annotationType) {
        if (annotationType == null || clazz == null || clazz == Object.class) {
            return Collections.emptyMap();
        }
        Map<Class<?>, List<T>> result = new HashMap<>();
        T[] annotations = clazz.getDeclaredAnnotationsByType(annotationType);
        if (annotations.length > 0) {
            result.put(clazz, Arrays.asList(annotations));
        }
        for (Class<?> interfaceType : clazz.getInterfaces()) {
            result.putAll(findAnnotations(interfaceType, annotationType));
        }
        Class<?> superclass = clazz.getSuperclass();
        result.putAll(findAnnotations(superclass, annotationType));
        return result;
    }

}
