package dev.simpleframework.token.exception;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public final class ExceptionManager {
    private static final Map<Class<? extends Throwable>, Function<? extends Throwable, ExceptionResponse>>
            CACHE_HANDLERS = new HashMap<>(16);
    private static final Map<Class<? extends Throwable>, Function<? extends Throwable, ExceptionResponse>>
            LOOKUP_HANDLERS = new ConcurrentHashMap<>(16);

    public static <T extends Throwable> void registerExceptionHandler(
            Class<T> clazz, Function<? extends Throwable, ExceptionResponse> func) {
        CACHE_HANDLERS.put(clazz, func);
        LOOKUP_HANDLERS.clear();
    }

    public static ExceptionResponse getResponseData(Throwable exception) {
        Function<Throwable, ExceptionResponse> func = resolve(exception);
        if (func == null) {
            return new ExceptionResponse(500, "500", exception.getMessage());
        }
        ExceptionResponse result = func.apply(exception);
        int status = result.status();
        if (status < 100 || status > 999) {
            status = 500;
        }
        String code = result.errCode();
        if (code == null) {
            code = String.valueOf(status);
        }
        return new ExceptionResponse(status, code, result.errMsg());
    }

    private static Function<Throwable, ExceptionResponse> resolve(Throwable exception) {
        Function<Throwable, ExceptionResponse> result = null;
        while (exception != null) {
            result = resolve(exception.getClass());
            if (result != null) {
                break;
            }
            exception = exception.getCause();
        }
        return result;
    }

    private static Function<Throwable, ExceptionResponse> resolve(Class<? extends Throwable> type) {
        Function<Throwable, ExceptionResponse> result = cast(LOOKUP_HANDLERS.get(type));
        if (result == null) {
            result = getCacheHandler(type);
            if (result != null) {
                LOOKUP_HANDLERS.put(type, result);
            }
        }
        return result;
    }

    private static Function<Throwable, ExceptionResponse> getCacheHandler(Class<? extends Throwable> type) {
        Function<Throwable, ExceptionResponse> result = cast(CACHE_HANDLERS.get(type));
        if (result != null) {
            return result;
        }
        List<Class<? extends Throwable>> matches = new ArrayList<>();
        for (Class<? extends Throwable> e : CACHE_HANDLERS.keySet()) {
            if (e.isAssignableFrom(type)) {
                matches.add(e);
            }
        }
        if (matches.isEmpty()) {
            return null;
        }
        matches.sort(new ExceptionDepthComparator(type));
        return cast(CACHE_HANDLERS.get(matches.get(0)));
    }

    @SuppressWarnings("unchecked")
    private static Function<Throwable, ExceptionResponse> cast(Function<? extends Throwable, ExceptionResponse> func) {
        if (func == null) {
            return null;
        }
        return (Function<Throwable, ExceptionResponse>) func;
    }

    /**
     * copy from org.springframework.core.ExceptionDepthComparator
     */
    public static class ExceptionDepthComparator implements Comparator<Class<? extends Throwable>> {

        private final Class<? extends Throwable> targetException;

        public ExceptionDepthComparator(Class<? extends Throwable> exceptionType) {
            this.targetException = exceptionType;
        }

        public int compare(Class<? extends Throwable> o1, Class<? extends Throwable> o2) {
            int depth1 = this.getDepth(o1, this.targetException, 0);
            int depth2 = this.getDepth(o2, this.targetException, 0);
            return depth1 - depth2;
        }

        private int getDepth(Class<?> declaredException, Class<?> exceptionToMatch, int depth) {
            if (exceptionToMatch.equals(declaredException)) {
                return depth;
            } else {
                return exceptionToMatch == Throwable.class ? Integer.MAX_VALUE : this.getDepth(declaredException, exceptionToMatch.getSuperclass(), depth + 1);
            }
        }

    }


}
