package dev.simpleframework.token.exception;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class LoginAccountNotFoundException extends SimpleTokenException {

    public LoginAccountNotFoundException() {
    }

    public LoginAccountNotFoundException(String message) {
        super(message);
    }

}
