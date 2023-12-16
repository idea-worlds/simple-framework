package dev.simpleframework.token.exception;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class UserDisabledException extends SimpleTokenException {

    public UserDisabledException() {
    }

    public UserDisabledException(String message) {
        super(message);
    }

}
