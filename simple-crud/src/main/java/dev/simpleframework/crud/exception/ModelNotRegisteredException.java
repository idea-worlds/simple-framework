package dev.simpleframework.crud.exception;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class ModelNotRegisteredException extends SimpleCrudException {

    public ModelNotRegisteredException(String modelName) {
        super("Model is not registered: " + modelName);
    }

    public ModelNotRegisteredException(Class<?> modelClass) {
        super("Class is not registered: " + modelClass);
    }

}
