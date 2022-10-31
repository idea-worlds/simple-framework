package dev.simpleframework.crud.exception;

import dev.simpleframework.crud.core.DatasourceType;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class DatasourceNotRegisteredException extends SimpleCrudException {

    public DatasourceNotRegisteredException(DatasourceType type) {
        super("Datasource is not registered: " + type);
    }

}
