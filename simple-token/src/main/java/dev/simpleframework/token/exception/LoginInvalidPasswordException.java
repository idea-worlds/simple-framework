package dev.simpleframework.token.exception;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class LoginInvalidPasswordException extends SimpleTokenException {

    public LoginInvalidPasswordException() {
    }

    public LoginInvalidPasswordException(String message) {
        super(message);
    }

}
