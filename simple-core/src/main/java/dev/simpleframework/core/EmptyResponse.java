package dev.simpleframework.core;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class EmptyResponse extends AbstractResponse<String> {
    private static final EmptyResponse INSTANCE = new EmptyResponse();

    public static EmptyResponse of() {
        return INSTANCE;
    }

    @Override
    public void setCode(String code) {
        throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    public void setMsg(String msg) {
        throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    public void setData(String data) {
        throw new UnsupportedOperationException("Unsupported");
    }

}
