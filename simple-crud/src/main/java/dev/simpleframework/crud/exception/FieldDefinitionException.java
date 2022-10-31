package dev.simpleframework.crud.exception;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class FieldDefinitionException extends SimpleCrudException {

    public FieldDefinitionException(String name, String cause) {
        super("Invalid field definition: " + name + ". Cause: " + cause);
    }

}
