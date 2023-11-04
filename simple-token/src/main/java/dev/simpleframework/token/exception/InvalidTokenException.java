package dev.simpleframework.token.exception;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class InvalidTokenException extends SimpleTokenException {

    public InvalidTokenException() {
    }

    public InvalidTokenException(String message) {
        super("Invalid token cause: " + message);
    }

}
