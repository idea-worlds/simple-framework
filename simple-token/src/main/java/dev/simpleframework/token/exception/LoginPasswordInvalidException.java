package dev.simpleframework.token.exception;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class LoginPasswordInvalidException extends SimpleTokenException {

    public LoginPasswordInvalidException() {
    }

    public LoginPasswordInvalidException(String message) {
        super(message);
    }

}
