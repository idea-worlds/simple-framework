package dev.simpleframework.token.exception;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class LoginUserDisabledException extends AbstractLoginException {

    public LoginUserDisabledException(String message) {
        super(message);
    }

}
