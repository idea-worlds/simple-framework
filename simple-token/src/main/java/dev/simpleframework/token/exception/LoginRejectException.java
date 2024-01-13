package dev.simpleframework.token.exception;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class LoginRejectException extends AbstractLoginException {

    public LoginRejectException(String message) {
        super("Login reject cause: " + message);
    }

}
