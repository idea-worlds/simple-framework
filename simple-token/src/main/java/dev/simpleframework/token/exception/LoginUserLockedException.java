package dev.simpleframework.token.exception;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class LoginUserLockedException extends SimpleTokenException {

    public LoginUserLockedException() {
    }

    public LoginUserLockedException(String message) {
        super(message);
    }

}
