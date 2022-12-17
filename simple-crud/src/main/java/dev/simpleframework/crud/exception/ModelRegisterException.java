package dev.simpleframework.crud.exception;

import dev.simpleframework.crud.core.DatasourceType;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class ModelRegisterException extends SimpleCrudException {

    public ModelRegisterException(DatasourceType type) {
        super("Datasource is not registered: " + type);
    }

    public ModelRegisterException(Class<?> modelClass, DatasourceType type) {
        super("Model [" + modelClass + "] register error. Datasource is not support: " + type);
    }

    public ModelRegisterException(Class<?> modelClass, Exception e) {
        super("Model [" + modelClass + "] register error.", e);
    }

    public ModelRegisterException(Class<?> modelClass, String e) {
        super("Model [" + modelClass + "] register error. " + e);
    }

    public ModelRegisterException(String modelName, String e) {
        super("Model [" + modelName + "] register error. " + e);
    }

}
