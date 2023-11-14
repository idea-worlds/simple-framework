package dev.simpleframework.token.exception;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class LoginUserDisabledException extends SimpleTokenException {

    public LoginUserDisabledException() {
    }

    public LoginUserDisabledException(String message) {
        super(message);
    }

}
