package dev.simpleframework.token.exception;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class UserNotFoundException extends SimpleTokenException {

    public UserNotFoundException() {
    }

    public UserNotFoundException(String message) {
        super(message);
    }

}
