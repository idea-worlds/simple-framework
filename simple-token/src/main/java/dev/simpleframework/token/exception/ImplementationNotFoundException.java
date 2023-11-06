package dev.simpleframework.token.exception;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class ImplementationNotFoundException extends SimpleTokenException {

    public ImplementationNotFoundException() {
    }

    public ImplementationNotFoundException(Class<?> clazz, Class<?> manager) {
        super("Can not found the implementation for " + clazz.getName() +
                ". Please register in " + manager.getName());
    }

}
