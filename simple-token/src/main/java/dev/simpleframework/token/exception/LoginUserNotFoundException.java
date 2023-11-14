package dev.simpleframework.token.exception;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class LoginUserNotFoundException extends SimpleTokenException {

    public LoginUserNotFoundException() {
    }

    public LoginUserNotFoundException(String message) {
        super(message);
    }

}
