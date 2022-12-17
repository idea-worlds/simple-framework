package dev.simpleframework.crud.exception;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public abstract class SimpleCrudException extends RuntimeException {

    public SimpleCrudException() {
    }

    public SimpleCrudException(String message) {
        super(message);
    }

    public SimpleCrudException(String message, Throwable cause) {
        super(message, cause);
    }

}
