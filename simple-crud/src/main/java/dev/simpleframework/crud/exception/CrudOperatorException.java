package dev.simpleframework.crud.exception;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class CrudOperatorException extends SimpleCrudException {

    public CrudOperatorException() {
    }

    public CrudOperatorException(String operator) {
        super("Not support operator: " + operator);
    }

}
