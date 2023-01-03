package dev.simpleframework.core;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class CommonResponse<T> extends AbstractResponse<T> {

    public static CommonResponse<String> success() {
        return new CommonResponse<>();
    }

    public static <R> CommonResponse<R> success(R data) {
        CommonResponse<R> result = new CommonResponse<>();
        result.setData(data);
        return result;
    }

    public static <R> CommonResponse<R> failure(String code) {
        return failure(code, "");
    }

    public static <R> CommonResponse<R> failure(String code, String msg) {
        CommonResponse<R> result = new CommonResponse<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

}
