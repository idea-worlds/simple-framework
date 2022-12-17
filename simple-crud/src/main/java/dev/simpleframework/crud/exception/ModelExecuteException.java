package dev.simpleframework.crud.exception;

public class ModelExecuteException extends SimpleCrudException {

    public ModelExecuteException(String e) {
        super(e);
    }

    public ModelExecuteException(Class<?> modelClass, Exception e) {
        super("Model [" + modelClass + "] execute error.", e);
    }

    public ModelExecuteException(Class<?> modelClass, String e) {
        super("Model [" + modelClass + "] execute error. " + e);
    }

    public ModelExecuteException(String modelName, String e) {
        super("Model [" + modelName + "] execute error. " + e);
    }

}
