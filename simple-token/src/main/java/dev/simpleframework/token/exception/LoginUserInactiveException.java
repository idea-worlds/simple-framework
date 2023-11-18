package dev.simpleframework.token.exception;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class LoginUserInactiveException extends SimpleTokenException {

    public LoginUserInactiveException() {
    }

    public LoginUserInactiveException(String message) {
        super(message);
    }

}
